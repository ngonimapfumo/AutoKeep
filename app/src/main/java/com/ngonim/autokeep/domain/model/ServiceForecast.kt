package com.ngonim.autokeep.domain.model

data class ServiceForecast(
    val item: ServiceItem,
    val status: HealthStatus,
    val remainingDistance: Int?,
    val remainingDays: Int?,
    val nextMileage: Int?,
    val nextEpochDay: Int?,
)
