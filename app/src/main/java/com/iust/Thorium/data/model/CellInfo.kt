package com.iust.thorium.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CellInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val latitude : Double,
    val longitude : Double,
    val cell_identity : String?,
    val plmn : String?,
    val net_type : String?,
    val LAC : String?=null,
    val TAC : String?=null,
    val status : String?=null,
    val type : Int?=null
)