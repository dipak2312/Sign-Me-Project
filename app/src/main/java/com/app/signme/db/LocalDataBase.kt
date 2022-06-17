package com.app.signme.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.signme.db.dao.MediaFileDao
import com.app.signme.db.entity.MediaFileEntity

@Database(entities = [MediaFileEntity::class], version = 1, exportSchema = false)
abstract class LocalDataBase : RoomDatabase() {
    companion object {
        @Volatile
        private var sInstance: LocalDataBase? = null

        @Synchronized
        fun getInstance(context: Context): LocalDataBase? {
            if (sInstance == null) {
                synchronized(LocalDataBase::class.java) {
                    if (sInstance == null) {
                        sInstance = Room.databaseBuilder(
                            context,
                            LocalDataBase::class.java,
                            "LocalDataBase"
                        )
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return sInstance
        }
    }

    abstract fun mediaFileDao(): MediaFileDao?
}