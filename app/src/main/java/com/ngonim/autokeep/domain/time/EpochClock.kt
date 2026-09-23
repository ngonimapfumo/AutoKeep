package com.ngonim.autokeep.domain.time

fun interface EpochClock {
    fun todayEpochDay(): Int
}

object SystemEpochClock : EpochClock {
    override fun todayEpochDay(): Int = Dates.todayEpochDay()
}
