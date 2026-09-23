package com.ngonim.autokeep.domain.model

data class RecordService(
    val vehicleId: Long,
    val serviceItemIds: Set<Long>,
    val mileage: Int,
    val performedAtEpochDay: Int,
    val costMinor: Long? = null,
    val workshop: String? = null,
    val notes: String? = null,
    val receiptUri: String? = null,
)
