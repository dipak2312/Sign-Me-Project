package com.app.signme.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_file")
data class MediaFileEntity(
    @PrimaryKey
    @ColumnInfo(name = "file_id")
    val fileId: Long ,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "status")
    val status: String
)