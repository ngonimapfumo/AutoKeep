package com.ngonim.autokeep.domain.model

data class YearlyCosts(
    val year: Int,
    val totalMinor: Long,
    val byCategory: Map<ServiceCategory, Long>,
)
