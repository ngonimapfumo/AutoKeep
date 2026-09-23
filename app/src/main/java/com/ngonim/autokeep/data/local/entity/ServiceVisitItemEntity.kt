package com.ngonim.autokeep.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "service_visit_items",
    primaryKeys = ["visit_id", "service_item_id"],
    foreignKeys = [
        ForeignKey(
            entity = ServiceVisitEntity::class,
            parentColumns = ["id"],
            childColumns = ["visit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ServiceItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["service_item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("visit_id"),
        Index("service_item_id"),
    ],
)
data class ServiceVisitItemEntity(
    @ColumnInfo(name = "visit_id")
    val visitId: Long,
    @ColumnInfo(name = "service_item_id")
    val serviceItemId: Long,
)
