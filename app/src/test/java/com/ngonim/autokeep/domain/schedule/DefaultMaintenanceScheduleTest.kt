package com.ngonim.autokeep.domain.schedule

import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.Powertrain
import com.ngonim.autokeep.domain.model.Transmission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultMaintenanceScheduleTest {
    @Test
    fun hondaGraceHybridCvt_getsJourneyChecklist() {
        val names = DefaultMaintenanceSchedule.itemsFor(
            powertrain = Powertrain.HYBRID,
            transmission = Transmission.CVT,
            mileageUnit = MileageUnit.KILOMETERS,
        ).map { it.name }

        assertEquals(
            listOf(
                "Engine Oil",
                "Oil Filter",
                "Air Filter",
                "Cabin Filter",
                "CVT Fluid",
                "Coolant",
                "Brake Fluid",
                "Spark Plugs",
                "Tyres",
                "Battery",
            ),
            names,
        )
        assertFalse(names.contains("Transmission Fluid"))
        assertEquals(8_000, DefaultMaintenanceSchedule.itemsFor(
            Powertrain.HYBRID,
            Transmission.CVT,
            MileageUnit.KILOMETERS,
        ).first { it.catalogKey == DefaultMaintenanceSchedule.ENGINE_OIL }.intervalDistance)
    }

    @Test
    fun electricCar_skipsEngineServices() {
        val keys = DefaultMaintenanceSchedule.itemsFor(
            powertrain = Powertrain.ELECTRIC,
            transmission = Transmission.AUTOMATIC,
            mileageUnit = MileageUnit.KILOMETERS,
        ).map { it.catalogKey }.toSet()

        assertFalse(keys.contains(DefaultMaintenanceSchedule.ENGINE_OIL))
        assertFalse(keys.contains(DefaultMaintenanceSchedule.SPARK_PLUGS))
        assertFalse(keys.contains(DefaultMaintenanceSchedule.CVT_FLUID))
        assertTrue(keys.contains(DefaultMaintenanceSchedule.TYRES))
        assertTrue(keys.contains(DefaultMaintenanceSchedule.BATTERY))
        assertTrue(keys.contains(DefaultMaintenanceSchedule.CABIN_FILTER))
    }

    @Test
    fun milesUsesImperialIntervals() {
        val oil = DefaultMaintenanceSchedule.itemsFor(
            Powertrain.GASOLINE,
            Transmission.AUTOMATIC,
            MileageUnit.MILES,
        ).first { it.catalogKey == DefaultMaintenanceSchedule.ENGINE_OIL }

        assertEquals(5_000, oil.intervalDistance)
    }
}
