package com.vajo.booktrack.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vajo.booktrack.data.model.BookItem
import kotlinx.coroutines.flow.Flow


@Dao
interface BookDao {
    @Insert
    suspend fun insertBook(book: BookItem)

    @Update
    suspend fun updateBook(book: BookItem)

    @Delete
    suspend fun deleteBook(book: BookItem)

    @Query("SELECT * FROM books")
    fun getBooks(): Flow<List<BookItem>>

}