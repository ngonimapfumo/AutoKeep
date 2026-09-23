package com.ngonim.autokeep.domain.model

data class Vehicle(
    val id: Long,
    val nickname: String?,
    val make: String,
    val model: String,
    val year: Int?,
    val engineSize: String?,
    val powertrain: Powertrain,
    val transmission: Transmission,
    val currentMileage: Int,
    val mileageUnit: MileageUnit,
    val photoUri: String?,
    val currencyCode: String?,
) {
    val displayName: String
        get() = nickname?.takeIf { it.isNotBlank() } ?: "$make $model"
}
