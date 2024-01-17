package com.vajo.booktrack.ui.view_model

import com.vajo.booktrack.data.model.BookItem
import com.vajo.booktrack.ui.view.Screen

data class MainViewState(
    val books: List<BookItem> = emptyList(),
    // Provide default values suitable for the types
    val editBook: BookItem = BookItem(
        title = "",
        author = "",
        date = "", // Current date as default, or another default date
        rating = 3, // Default rating, assuming an integer scale
        imgPath = "",
        isEbook = false, // Default boolean value
        id = 0 // Default ID, typically 0 for a new item
    ),
    val selectedScreen: Screen = Screen.ListScreen,
    val openDialog: Boolean = false,
    val originatingScreenForCamera: Screen? = null
)