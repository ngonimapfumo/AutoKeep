package com.ngonim.autokeep.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.Powertrain
import com.ngonim.autokeep.domain.model.Transmission

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nickname: String? = null,
    val make: String,
    val model: String,
    val year: Int? = null,
    @ColumnInfo(name = "engine_size")
    val engineSize: String? = null,
    val powertrain: Powertrain = Powertrain.GASOLINE,
    val transmission: Transmission = Transmission.AUTOMATIC,
    @ColumnInfo(name = "current_mileage")
    val currentMileage: Int,
    @ColumnInfo(name = "mileage_unit")
    val mileageUnit: MileageUnit = MileageUnit.KILOMETERS,
    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,
    @ColumnInfo(name = "currency_code")
    val currencyCode: String? = null,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long,
)
