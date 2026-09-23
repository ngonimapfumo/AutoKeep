package com.ngonim.autokeep.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ngonim.autokeep.data.local.entity.ServiceItemEntity
import com.ngonim.autokeep.data.local.entity.ServiceVisitEntity
import com.ngonim.autokeep.data.local.entity.ServiceVisitItemEntity

data class ServiceItemWithVisits(
    @Embedded val item: ServiceItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ServiceVisitItemEntity::class,
            parentColumn = "service_item_id",
            entityColumn = "visit_id",
        ),
    )
    val visits: List<ServiceVisitEntity>,
)
