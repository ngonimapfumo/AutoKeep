package com.ngonim.autokeep.domain.model

data class AddVehicle(
    val make: String,
    val model: String,
    val year: Int? = null,
    val engineSize: String? = null,
    val powertrain: Powertrain,
    val transmission: Transmission,
    val currentMileage: Int,
    val mileageUnit: MileageUnit = MileageUnit.KILOMETERS,
    val nickname: String? = null,
    val currencyCode: String? = null,
    val photoUri: String? = null,
)
