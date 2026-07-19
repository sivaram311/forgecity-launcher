package buzz.delena.forgecity.city

/** Pure favorite dock policy — max 6 pins. */
object FavoritePolicy {
    const val MAX_FAVORITES = 6

    fun canPin(currentFavoriteCount: Int, currentlyFavorite: Boolean): Boolean =
        currentlyFavorite || currentFavoriteCount < MAX_FAVORITES
}
