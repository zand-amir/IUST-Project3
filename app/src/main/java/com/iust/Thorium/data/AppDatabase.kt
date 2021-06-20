package com.iust.Thorium.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iust.Thorium.data.entity.CellPowerDao
import com.iust.Thorium.data.model.CellPower


@Database(entities = arrayOf(CellPower::class), version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cellPowerDao(): CellPowerDao

    companion object {
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null){
                synchronized(AppDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "signal_strength")
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