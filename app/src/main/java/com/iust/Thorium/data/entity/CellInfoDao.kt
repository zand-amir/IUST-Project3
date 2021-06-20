package com.iust.Thorium.data.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.iust.thorium.data.model.CellInfo

@Dao
interface CellInfoDao {
    @Query("SELECT * FROM CellInfo")
    fun getAll(): List<CellInfo>

    @Query("SELECT * FROM CellPower WHERE id IN (:infoIds)")
    fun loadAllByIds(infoIds: IntArray): List<CellInfo>

    @Insert
    fun insert(vararg info: CellInfo)

    @Delete
    fun delete(power: CellInfo)
}
