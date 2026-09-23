package com.ngonim.autokeep.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ngonim.autokeep.data.local.entity.ServiceItemEntity
import com.ngonim.autokeep.data.local.entity.ServiceVisitEntity
import com.ngonim.autokeep.data.local.entity.ServiceVisitItemEntity

data class ServiceVisitWithItems(
    @Embedded val visit: ServiceVisitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ServiceVisitItemEntity::class,
            parentColumn = "visit_id",
            entityColumn = "service_item_id",
        ),
    )
    val items: List<ServiceItemEntity>,
)
