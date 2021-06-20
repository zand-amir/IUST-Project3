package com.iust.thorium.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iust.thorium.data.entity.CellInformationDao
import com.iust.thorium.data.model.CellInformation


@Database(entities = arrayOf(CellInformation::class), version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cellPowerDao(): CellInformationDao

    companion object {
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null){
                synchronized(AppDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "thorium")
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase(){
            INSTANCE = null
        }
    }
}