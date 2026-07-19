package buzz.delena.forgecity.city

data class IsoPoint(val x: Float, val y: Float)

object IsoMath {
    /** Convert grid coordinates to isometric screen space. */
    fun gridToScreen(col: Float, row: Float, tileWidth: Float, tileHeight: Float): IsoPoint {
        val x = (col - row) * (tileWidth / 2f)
        val y = (col + row) * (tileHeight / 2f)
        return IsoPoint(x, y)
    }
}
