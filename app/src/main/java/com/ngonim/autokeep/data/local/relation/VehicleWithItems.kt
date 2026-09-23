package com.ngonim.autokeep.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ngonim.autokeep.data.local.entity.ServiceItemEntity
import com.ngonim.autokeep.data.local.entity.VehicleEntity

data class VehicleWithItems(
    @Embedded val vehicle: VehicleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "vehicle_id",
    )
    val items: List<ServiceItemEntity>,
)
