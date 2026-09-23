package com.ngonim.autokeep.domain.model

data class Dashboard(
    val vehicle: Vehicle,
    val goodCount: Int,
    val dueSoonCount: Int,
    val overdueCount: Int,
    val unknownCount: Int,
    val needsAttention: List<ServiceForecast>,
    val items: List<ServiceForecast>,
    val reminders: List<MaintenanceReminder>,
)

data class MaintenanceReminder(
    val vehicleName: String,
    val itemName: String,
    val remainingDistance: Int,
    val mileageUnit: MileageUnit,
)
