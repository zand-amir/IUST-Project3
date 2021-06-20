package com.iust.Thorium.data.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.iust.Thorium.data.model.CellPower

@Dao
interface CellPowerDao {
    @Query("SELECT * FROM CellPower")
    fun getAll(): List<CellPower>

    @Query("SELECT * FROM CellPower WHERE id IN (:infoIds)")
    fun loadAllByIds(infoIds: IntArray): List<CellPower>

    @Insert
    fun insert(vararg info: CellPower)

    @Delete
    fun delete(power: CellPower)
}
