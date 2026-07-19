package buzz.delena.forgecity.assistant.rewrite

import java.io.ByteArrayOutputStream
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class AgentPortalRewriteClient(
    private val connectTimeoutMs: Int = 5_000,
    private val readTimeoutMs: Int = 30_000,
    private val maxResponseBytes: Int = 16 * 1024,
) : AutoCloseable {
    private val connectionLock = Any()
    private var activeConnection: HttpsURLConnection? = null
    private var activeKey: String? = null
    @Volatile
    private var closed = false

    fun rewrite(endpoint: String, apiKey: String, request: RewriteRequest): RewriteResult {
        if (closed || apiKey.isBlank()) return RewriteResult.Unavailable
        val url = validatedEndpoint(endpoint) ?: return RewriteResult.Unavailable
        var connection: HttpsURLConnection? = null
        return try {
            connection = (url.openConnection() as? HttpsURLConnection)
                ?: return RewriteResult.Unavailable
            synchronized(connectionLock) {
                if (closed || activeConnection != null) return RewriteResult.Unavailable
                activeConnection = connection
                activeKey = request.notificationKey
            }
            connection.instanceFollowRedirects = false
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Cache-Control", "no-store")
            connection.setRequestProperty("X-ForgeCity-Key", apiKey)

            val payload = request.toJson().toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(payload.size)
            connection.outputStream.use { it.write(payload) }

            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    if (!connection.contentType.orEmpty().substringBefore(';')
                            .equals("application/json", ignoreCase = true)
                    ) {
                        return RewriteResult.Malformed
                    }
                    val response = readBounded(connection.inputStream)
                        ?: return RewriteResult.Malformed
                    RewriteResponseParser.parse(response.toString(Charsets.UTF_8))
                }
                HttpURLConnection.HTTP_UNAUTHORIZED,
                HttpURLConnection.HTTP_FORBIDDEN,
                -> RewriteResult.Unauthorized
                HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> RewriteResult.Timeout
                else -> RewriteResult.Unavailable
            }
        } catch (_: SocketTimeoutException) {
            RewriteResult.Timeout
        } catch (_: InterruptedIOException) {
            if (closed) RewriteResult.Cancelled else RewriteResult.Timeout
        } catch (_: Exception) {
            if (closed) RewriteResult.Cancelled else RewriteResult.Unavailable
        } finally {
            synchronized(connectionLock) {
                if (activeConnection === connection) {
                    activeConnection = null
                    activeKey = null
                }
            }
            connection?.disconnect()
        }
    }

    fun cancel(notificationKey: String) {
        val connection = synchronized(connectionLock) {
            if (activeKey != notificationKey) null
            else activeConnection.also {
                activeConnection = null
                activeKey = null
            }
        }
        connection?.disconnect()
    }

    private fun cancelActive() {
        val connection = synchronized(connectionLock) {
            activeConnection.also {
                activeConnection = null
                activeKey = null
            }
        }
        connection?.disconnect()
    }

    override fun close() {
        closed = true
        cancelActive()
    }

    private fun readBounded(input: java.io.InputStream): ByteArray? {
        input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(2_048)
            var total = 0
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                total += count
                if (total > maxResponseBytes) return null
                output.write(buffer, 0, count)
            }
            return output.toByteArray()
        }
    }

    private fun validatedEndpoint(raw: String): URL? = runCatching {
        val url = URL(raw.trim())
        require(url.protocol.equals("https", ignoreCase = true))
        require(url.host.isNotBlank())
        require(url.userInfo == null)
        require(url.query == null && url.ref == null)
        require(url.path.isNotBlank() && url.path != "/")
        url
    }.getOrNull()
}
