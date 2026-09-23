package com.ngonim.autokeep.domain.model

data class ServiceVisit(
    val id: Long,
    val vehicleId: Long,
    val mileage: Int,
    val performedAtEpochDay: Int,
    val itemIds: List<Long>,
    val itemNames: List<String>,
    val costMinor: Long?,
    val workshop: String?,
    val notes: String?,
    val receiptUri: String?,
)
