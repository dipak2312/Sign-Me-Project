package com.app.signme.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.signme.db.entity.MediaFileEntity

@Dao
interface MediaFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(mediaFileEntity: MediaFileEntity)

    @Query("SELECT * FROM media_file WHERE file_id=:fileId")
    fun getByFileId(fileId: Long?): MediaFileEntity


    @Query("UPDATE  media_file SET status=:status WHERE file_id=:fileId")
    fun changeStatus(fileId: Long, status: String): Int

    @Query("SELECT * FROM media_file WHERE user_id=:userId")
    fun getByPostId(userId: String?): List<MediaFileEntity>

    @Query("Delete FROM media_file WHERE file_id=:fileId")
    fun delete(fileId: Long?)

    @Query("SELECT * FROM media_file WHERE user_id=:userId & status=:status")
    fun getCount(userId: String?, status: String): List<MediaFileEntity>
}
