package buzz.delena.forgecity.ui.lot

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/** Live Production House PROD (v0.1.0) — film-lot web experience inside HomeMode. */
const val PRODUCTION_HOUSE_URL = "https://production-house.delena.buzz"

/**
 * WebView host for the Production House lot. JS + DOM storage required for R3F.
 * Shows a non-blank fallback if the page fails to load (offline / HTTP error).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ProductionHouseWebSurface(
    modifier: Modifier = Modifier,
    url: String = PRODUCTION_HOUSE_URL,
) {
    var loadFailed by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    webViewClient = object : WebViewClient() {
                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?,
                        ) {
                            if (request?.isForMainFrame == true) {
                                loadFailed = true
                            }
                        }

                        override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                            if (!loadFailed) {
                                loadFailed = false
                            }
                        }
                    }
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url && !loadFailed) {
                    webView.loadUrl(url)
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.destroy()
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (loadFailed) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF16202E))
                    .padding(24.dp),
            ) {
                Text(
                    text = "Lot offline — check network",
                    color = Color(0xFFFFF6F0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(Color(0xCC201828), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}
