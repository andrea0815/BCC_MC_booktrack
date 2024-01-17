package com.vajo.booktrack.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vajo.booktrack.data.model.BookItem
import androidx.room.TypeConverters

@Database(entities = [BookItem::class], version = 2)

abstract class BooksDatabase: RoomDatabase() {
    abstract val dao: BookDao
}