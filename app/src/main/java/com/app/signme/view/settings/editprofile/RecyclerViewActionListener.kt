package com.app.signme.view.settings.editprofile

interface RecyclerViewActionListener {
    /**
     * This function is call when user click on recycler view element
     */
    fun onItemClick(viewId: Int, position: Int, childPosition: Int?)

    /**
     * This method call when recycler view reach at last element/position.
     * And want to load more items
     */
    fun onLoadMore(itemCount: Int, nextPage: Int)
}