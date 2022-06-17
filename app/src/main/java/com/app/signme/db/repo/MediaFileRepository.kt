package com.app.signme.db.repo

import android.content.Context
import android.util.Log
import com.app.signme.db.LocalDataBase
import com.app.signme.db.dao.MediaFileDao
import com.app.signme.db.entity.MediaFileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MediaFileRepository(
    var mDao: MediaFileDao? = null,
    var mIoExecutor: ExecutorService? = null
) {

    private val TAG: String ="MediaFileRepository"

    companion object {
        @Volatile
        private var sInstance: MediaFileRepository? = null

        fun getInstance(application: Context): MediaFileRepository? {
            if (sInstance == null) {
                synchronized(MediaFileRepository::class.java) {
                    if (sInstance == null) {
                        val database: LocalDataBase? =
                            LocalDataBase.getInstance(application)
                        sInstance = MediaFileRepository(
                            database?.mediaFileDao(),
                            Executors.newSingleThreadExecutor()
                        )
                    }
                }
            }
            return sInstance
        }
    }


    //  delete method of DAO
    fun deleteFile(fileId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "deleteFile: ")
            mIoExecutor?.execute { mDao?.delete(fileId = fileId) }
        }
    }

    // get method of DAO
    fun getByFileId(fileId: Long): MediaFileEntity? {
        Log.i(TAG, "getByFileId: ")
        return mDao?.getByFileId(fileId)
    }

    // update method of DAO
    fun changeStatus(fileId: Long,status: String): Int? {
        Log.i(TAG, "changeStatus: ")
        return mDao?.changeStatus(fileId,status)
    }

    // get method of DAO
    fun getByUserId(userId: String):List<MediaFileEntity>? {
        Log.i(TAG, "getByPostId: ")
        return mDao?.getByPostId(userId)
    }

    // get method of DAO
    fun getCount(userId: String,status: String):Int? {
        Log.i(TAG, "getCount: ")
        return mDao?.getCount(userId,status)!!.size
    }

    // insert method of DAO
    fun insertFile(mediaFileEntity: MediaFileEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "insertFile: ")
            mDao?.insert(mediaFileEntity)
        }
    }
}