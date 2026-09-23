package com.ngonim.autokeep.domain.model

data class AddCustomItem(
    val vehicleId: Long,
    val name: String,
    val category: ServiceCategory = ServiceCategory.OTHER,
    val intervalDistance: Int,
    val intervalMonths: Int? = null,
)
