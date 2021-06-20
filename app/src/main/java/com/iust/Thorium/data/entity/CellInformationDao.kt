package com.iust.thorium.data.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.iust.thorium.data.model.CellInformation

@Dao
interface CellInformationDao {
    @Query("SELECT * FROM CellInformation")
    fun getAll(): List<CellInformation>

    @Query("SELECT * FROM CellInformation WHERE id IN (:infoIds)")
    fun loadAllByIds(infoIds: IntArray): List<CellInformation>

//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): CellPower

    @Insert
    fun insert(vararg info: CellInformation)

    @Delete
    fun delete(information: CellInformation)
}
