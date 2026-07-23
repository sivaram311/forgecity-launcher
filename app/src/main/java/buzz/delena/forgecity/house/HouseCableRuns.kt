package buzz.delena.forgecity.house

/**
 * Droop cable sampling (Production House CableRun parabolic mid-span).
 * Used for tests + optional runtime Path/Cylinder dressing; house_shell bakes chains too.
 */
object HouseCableRuns {
    data class Point(val x: Float, val y: Float, val z: Float)

    /** Parabolic droop along a single span: y -= sag * 4t(1-t). */
    fun droopSpan(
        ax: Float,
        ay: Float,
        az: Float,
        bx: Float,
        by: Float,
        bz: Float,
        sag: Float,
        samples: Int = 10,
    ): List<Point> {
        val n = samples.coerceAtLeast(2)
        return List(n + 1) { i ->
            val t = i / n.toFloat()
            Point(
                x = ax + (bx - ax) * t,
                y = ay + (by - ay) * t - sag * 4f * t * (1f - t),
                z = az + (bz - az) * t,
            )
        }
    }

    /** Hallway → office → workshop control runs matching the GLB generator. */
    fun defaultRuns(): List<List<Point>> = listOf(
        listOf(Point(0.25f, 0.08f, 3.25f), Point(1.6f, 0.06f, 4.5f), Point(2.85f, 0.07f, 5.7f)),
        listOf(Point(2.95f, 0.08f, 3.4f), Point(4.5f, 0.05f, 4.5f), Point(6.4f, 0.07f, 5.6f)),
        listOf(Point(3.2f, 0.08f, 6.15f), Point(4.8f, 0.04f, 7.4f), Point(6.3f, 0.07f, 8.6f)),
    )

    fun sampleRun(waypoints: List<Point>, sag: Float = 0.10f, samplesPerSpan: Int = 8): List<Point> {
        if (waypoints.size < 2) return waypoints
        val out = ArrayList<Point>()
        for (i in 0 until waypoints.lastIndex) {
            val a = waypoints[i]
            val b = waypoints[i + 1]
            val span = droopSpan(a.x, a.y, a.z, b.x, b.y, b.z, sag, samplesPerSpan)
            if (i > 0) out.addAll(span.drop(1)) else out.addAll(span)
        }
        return out
    }
}
