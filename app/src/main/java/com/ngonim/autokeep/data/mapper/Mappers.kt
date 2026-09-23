package com.ngonim.autokeep.data.mapper

import com.ngonim.autokeep.data.local.entity.ExpenseEntity
import com.ngonim.autokeep.data.local.entity.ServiceItemEntity
import com.ngonim.autokeep.data.local.entity.VehicleEntity
import com.ngonim.autokeep.data.local.relation.ServiceVisitWithItems
import com.ngonim.autokeep.domain.model.Expense
import com.ngonim.autokeep.domain.model.ServiceItem
import com.ngonim.autokeep.domain.model.ServiceVisit
import com.ngonim.autokeep.domain.model.Vehicle

fun VehicleEntity.toDomain(): Vehicle =
    Vehicle(
        id = id,
        nickname = nickname,
        make = make,
        model = model,
        year = year,
        engineSize = engineSize,
        powertrain = powertrain,
        transmission = transmission,
        currentMileage = currentMileage,
        mileageUnit = mileageUnit,
        photoUri = photoUri,
        currencyCode = currencyCode,
    )

fun ServiceItemEntity.toDomain(): ServiceItem =
    ServiceItem(
        id = id,
        vehicleId = vehicleId,
        name = name,
        category = category,
        catalogKey = catalogKey,
        intervalDistance = intervalDistance,
        intervalMonths = intervalMonths,
        lastDoneMileage = lastDoneMileage,
        lastDoneAtEpochDay = lastDoneAtEpochDay,
        sortOrder = sortOrder,
    )

fun ServiceVisitWithItems.toDomain(): ServiceVisit {
    val ordered = items.sortedWith(compareBy(ServiceItemEntity::sortOrder, ServiceItemEntity::name))
    return ServiceVisit(
        id = visit.id,
        vehicleId = visit.vehicleId,
        mileage = visit.mileage,
        performedAtEpochDay = visit.performedAtEpochDay,
        itemIds = ordered.map { it.id },
        itemNames = ordered.map { it.name },
        costMinor = visit.costMinor,
        workshop = visit.workshop,
        notes = visit.notes,
        receiptUri = visit.receiptUri,
    )
}

fun ExpenseEntity.toDomain(): Expense =
    Expense(
        id = id,
        vehicleId = vehicleId,
        title = title,
        category = category,
        costMinor = costMinor,
        performedAtEpochDay = performedAtEpochDay,
        notes = notes,
    )
