package buzz.delena.forgecity.city

/**
 * Pure screen-space (city-space) hit geometry for isometric building prisms.
 */
data class CityRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    fun contains(x: Float, y: Float): Boolean =
        x in left..right && y in top..bottom
}

object BuildingHitGeometry {
    fun depthKey(col: Int, row: Int): Int = col + row

    fun prismHeight(level: Int): Float =
        IsoLayout.BASE_HEIGHT + level.coerceAtLeast(1) * IsoLayout.HEIGHT_PER_LEVEL

    fun prismBounds(
        point: IsoPoint,
        level: Int,
        pad: Float = IsoLayout.MIN_HIT_PAD,
    ): CityRect {
        val height = prismHeight(level)
        val halfW = IsoLayout.HALF_W + pad
        val halfH = IsoLayout.HALF_H + pad
        return CityRect(
            left = point.x - halfW,
            top = point.y - height - pad,
            right = point.x + halfW,
            bottom = point.y + halfH * 2f + pad,
        )
    }

    fun <T> pickBuilding(
        cityX: Float,
        cityY: Float,
        buildings: List<T>,
        colOf: (T) -> Int,
        rowOf: (T) -> Int,
        levelOf: (T) -> Int,
        tileWidth: Float = IsoLayout.TILE_WIDTH,
        tileHeight: Float = IsoLayout.TILE_HEIGHT,
        pad: Float = IsoLayout.MIN_HIT_PAD,
    ): T? {
        val candidates = buildings.sortedByDescending { depthKey(colOf(it), rowOf(it)) }
        for (building in candidates) {
            val point = IsoMath.gridToScreen(
                colOf(building).toFloat(),
                rowOf(building).toFloat(),
                tileWidth,
                tileHeight,
            )
            if (prismBounds(point, levelOf(building), pad).contains(cityX, cityY)) {
                return building
            }
        }
        // Soft fallback: nearest visual center within expanded radius.
        val soft = pad * 2.5f
        return buildings.minByOrNull { building ->
            val point = IsoMath.gridToScreen(
                colOf(building).toFloat(),
                rowOf(building).toFloat(),
                tileWidth,
                tileHeight,
            )
            val dx = cityX - point.x
            val dy = cityY - (point.y - prismHeight(levelOf(building)) * 0.35f)
            dx * dx + dy * dy
        }?.takeIf { building ->
            val point = IsoMath.gridToScreen(
                colOf(building).toFloat(),
                rowOf(building).toFloat(),
                tileWidth,
                tileHeight,
            )
            val dx = cityX - point.x
            val dy = cityY - (point.y - prismHeight(levelOf(building)) * 0.35f)
            dx * dx + dy * dy <= soft * soft
        }
    }
}
