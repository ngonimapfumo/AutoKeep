package com.ngonim.autokeep.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ngonim.autokeep.domain.model.ExpenseCategory

@Entity(
    tableName = "expenses",
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
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    val title: String,
    val category: ExpenseCategory,
    @ColumnInfo(name = "cost_minor")
    val costMinor: Long,
    @ColumnInfo(name = "performed_at_epoch_day")
    val performedAtEpochDay: Int,
    val notes: String? = null,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
)
