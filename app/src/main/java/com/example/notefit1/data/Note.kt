package com.example.notefit1.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long,

    // 📌 PIN SUPPORT
    val isPinned: Boolean = false,
    val reminderTime: Long? = null
) : Parcelable
