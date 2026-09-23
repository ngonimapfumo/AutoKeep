package com.ngonim.autokeep.domain.model

data class ServiceItem(
    val id: Long,
    val vehicleId: Long,
    val name: String,
    val category: ServiceCategory,
    val catalogKey: String?,
    val intervalDistance: Int,
    val intervalMonths: Int?,
    val lastDoneMileage: Int?,
    val lastDoneAtEpochDay: Int?,
    val sortOrder: Int,
) {
    val isCustom: Boolean get() = catalogKey == null
}
