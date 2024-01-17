package com.vajo.booktrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "books")
data class BookItem(
    val title: String,
    val author: String,
    val date: String,
    val rating: Int,
    val imgPath: String,
    val isEbook: Boolean,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)