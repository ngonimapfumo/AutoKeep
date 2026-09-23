package com.ngonim.autokeep.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One workshop visit. Several checklist items can be done in the same visit
 * and share mileage, cost, workshop, and receipt.
 */
@Entity(
    tableName = "service_visits",
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
        Index(value = ["vehicle_id", "performed_at_epoch_day"]),
    ],
)
data class ServiceVisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    val mileage: Int,
    @ColumnInfo(name = "performed_at_epoch_day")
    val performedAtEpochDay: Int,
    @ColumnInfo(name = "cost_minor")
    val costMinor: Long? = null,
    val workshop: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "receipt_uri")
    val receiptUri: String? = null,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
)
