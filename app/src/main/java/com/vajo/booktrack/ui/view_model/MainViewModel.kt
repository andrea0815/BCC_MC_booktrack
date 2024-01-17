package com.vajo.nav.ui.uistates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vajo.booktrack.data.BookDao
import com.vajo.booktrack.data.model.BookItem
import com.vajo.booktrack.ui.view.Screen
import com.vajo.booktrack.ui.view_model.MainViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val dao: BookDao): ViewModel() {
    private val _mainViewState = MutableStateFlow(MainViewState())
    val mainViewState: StateFlow<MainViewState> = _mainViewState.asStateFlow()

    val bookTitle = MutableStateFlow("")
    val bookAuthor = MutableStateFlow("")
    val bookDate = MutableStateFlow("")
    val bookRating = MutableStateFlow(3)
    val bookIsEbook = MutableStateFlow(false)
    val bookImgPath = MutableStateFlow("")

    val bookTitleEdit = MutableStateFlow("")
    val bookAuthorEdit = MutableStateFlow("")
    val bookDateEdit = MutableStateFlow("")
    val bookRatingEdit = MutableStateFlow(3)
    val bookIsEbookEdit = MutableStateFlow(false)
    val bookImgPathEdit = MutableStateFlow("")


    fun selectScreen(screen: Screen){
        _mainViewState.update { it.copy(selectedScreen = screen) }
    }

    fun saveButton(book: BookItem){
        viewModelScope.launch {
            dao.insertBook(book)
        }
    }

    fun getBooks() {
        viewModelScope.launch {
            dao.getBooks().collect() { books ->
                // Sort books based on the current sorting order
                val sortedBooks = when (_sortingOrder.value) {
                    SortingOrder.NewestFirst -> books.sortedByDescending { it.date }
                    SortingOrder.OldestFirst -> books.sortedBy { it.date }
                }
                _mainViewState.update { it.copy(books = sortedBooks) }
            }
        }
    }

    fun clickDelete(book: BookItem){
        viewModelScope.launch {
            dao.deleteBook(book)
            getBooks()
        }
    }

    fun editBook(book: BookItem){
        bookTitleEdit.value = book.title
        bookAuthorEdit.value = book.author
        bookDateEdit.value = book.date
        bookRatingEdit.value = book.rating
        bookIsEbookEdit.value = book.isEbook
        bookImgPathEdit.value = book.imgPath

        _mainViewState.update { it.copy(editBook = book)
        }
    }

    fun saveEditedBook(book: BookItem){
        viewModelScope.launch {
            dao.updateBook(book)
            getBooks()
        }
    }

    fun updateOriginatingScreenForCamera(screen: Screen) {
        _mainViewState.update { currentState ->
            currentState.copy(originatingScreenForCamera = screen)
        }
    }

    // SORTING

    // Enum to represent sorting order
    enum class SortingOrder { NewestFirst, OldestFirst }

    // MutableStateFlow to track current sorting order
    private val _sortingOrder = MutableStateFlow(SortingOrder.NewestFirst)
    val sortingOrder: StateFlow<SortingOrder> = _sortingOrder.asStateFlow()

    // Function to toggle sorting order
    fun toggleSortingOrder() {
        _sortingOrder.value = if (_sortingOrder.value == SortingOrder.NewestFirst) {
            SortingOrder.OldestFirst
        } else {
            SortingOrder.NewestFirst
        }
        // Call the function to fetch and sort books based on the new order
        getBooks()
    }
}

