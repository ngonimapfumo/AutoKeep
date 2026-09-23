package com.ngonim.autokeep.domain.schedule

import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.Powertrain
import com.ngonim.autokeep.domain.model.ServiceCategory
import com.ngonim.autokeep.domain.model.Transmission

data class SeedServiceItem(
    val catalogKey: String,
    val name: String,
    val category: ServiceCategory,
    val intervalDistance: Int,
    val intervalMonths: Int?,
    val sortOrder: Int,
)

object DefaultMaintenanceSchedule {
    const val ENGINE_OIL = "ENGINE_OIL"
    const val OIL_FILTER = "OIL_FILTER"
    const val AIR_FILTER = "AIR_FILTER"
    const val CABIN_FILTER = "CABIN_FILTER"
    const val CVT_FLUID = "CVT_FLUID"
    const val TRANSMISSION_FLUID = "TRANSMISSION_FLUID"
    const val GEAR_OIL = "GEAR_OIL"
    const val COOLANT = "COOLANT"
    const val BRAKE_FLUID = "BRAKE_FLUID"
    const val SPARK_PLUGS = "SPARK_PLUGS"
    const val TYRES = "TYRES"
    const val BATTERY = "BATTERY"

    fun itemsFor(
        powertrain: Powertrain,
        transmission: Transmission,
        mileageUnit: MileageUnit,
    ): List<SeedServiceItem> =
        catalog
            .filter { it.applies(powertrain, transmission) }
            .mapIndexed { index, entry ->
                SeedServiceItem(
                    catalogKey = entry.key,
                    name = entry.name,
                    category = entry.category,
                    intervalDistance = entry.interval(mileageUnit),
                    intervalMonths = entry.intervalMonths,
                    sortOrder = index,
                )
            }

    private data class CatalogEntry(
        val key: String,
        val name: String,
        val category: ServiceCategory,
        val intervalKm: Int,
        val intervalMiles: Int,
        val intervalMonths: Int?,
        val applies: (Powertrain, Transmission) -> Boolean,
    ) {
        fun interval(unit: MileageUnit): Int =
            if (unit == MileageUnit.MILES) intervalMiles else intervalKm
    }

    private val hasEngine: (Powertrain, Transmission) -> Boolean =
        { powertrain, _ -> powertrain != Powertrain.ELECTRIC }

    private val sparkIgnition: (Powertrain, Transmission) -> Boolean =
        { powertrain, _ -> powertrain == Powertrain.GASOLINE || powertrain == Powertrain.HYBRID }

    private val catalog = listOf(
        CatalogEntry(ENGINE_OIL, "Engine Oil", ServiceCategory.ENGINE, 8_000, 5_000, 12, hasEngine),
        CatalogEntry(OIL_FILTER, "Oil Filter", ServiceCategory.ENGINE, 8_000, 5_000, 12, hasEngine),
        CatalogEntry(AIR_FILTER, "Air Filter", ServiceCategory.ENGINE, 20_000, 12_000, 24, hasEngine),
        CatalogEntry(CABIN_FILTER, "Cabin Filter", ServiceCategory.OTHER, 15_000, 10_000, 12) { _, _ -> true },
        CatalogEntry(CVT_FLUID, "CVT Fluid", ServiceCategory.TRANSMISSION, 40_000, 25_000, 48) { _, transmission ->
            transmission == Transmission.CVT
        },
        CatalogEntry(TRANSMISSION_FLUID, "Transmission Fluid", ServiceCategory.TRANSMISSION, 40_000, 25_000, 48) { _, transmission ->
            transmission == Transmission.AUTOMATIC
        },
        CatalogEntry(GEAR_OIL, "Gear Oil", ServiceCategory.TRANSMISSION, 40_000, 25_000, 48) { _, transmission ->
            transmission == Transmission.MANUAL
        },
        CatalogEntry(COOLANT, "Coolant", ServiceCategory.COOLING, 40_000, 25_000, 24) { _, _ -> true },
        CatalogEntry(BRAKE_FLUID, "Brake Fluid", ServiceCategory.BRAKES, 40_000, 25_000, 24) { _, _ -> true },
        CatalogEntry(SPARK_PLUGS, "Spark Plugs", ServiceCategory.ENGINE, 40_000, 25_000, 48, sparkIgnition),
        CatalogEntry(TYRES, "Tyres", ServiceCategory.TYRES, 40_000, 25_000, 60) { _, _ -> true },
        CatalogEntry(BATTERY, "Battery", ServiceCategory.ELECTRICAL, 50_000, 30_000, 36) { _, _ -> true },
    )
}
