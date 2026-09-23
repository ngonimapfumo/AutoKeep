package com.ngonim.autokeep.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ngonim.autokeep.domain.model.ServiceCategory

@Entity(
    tableName = "service_items",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicle_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("vehicle_id"),
        Index(value = ["vehicle_id", "name"], unique = true),
    ],
)
data class ServiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    val name: String,
    val category: ServiceCategory = ServiceCategory.OTHER,
    @ColumnInfo(name = "catalog_key")
    val catalogKey: String? = null,
    @ColumnInfo(name = "interval_distance")
    val intervalDistance: Int,
    @ColumnInfo(name = "interval_months")
    val intervalMonths: Int? = null,
    @ColumnInfo(name = "last_done_mileage")
    val lastDoneMileage: Int? = null,
    @ColumnInfo(name = "last_done_at_epoch_day")
    val lastDoneAtEpochDay: Int? = null,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
)
