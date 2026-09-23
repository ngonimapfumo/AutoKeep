package com.ngonim.autokeep.data.local

import androidx.room.TypeConverter
import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.Powertrain
import com.ngonim.autokeep.domain.model.ExpenseCategory
import com.ngonim.autokeep.domain.model.ServiceCategory
import com.ngonim.autokeep.domain.model.Transmission

class Converters {
    @TypeConverter
    fun mileageUnitToString(value: MileageUnit): String = value.name

    @TypeConverter
    fun stringToMileageUnit(value: String): MileageUnit = MileageUnit.valueOf(value)

    @TypeConverter
    fun powertrainToString(value: Powertrain): String = value.name

    @TypeConverter
    fun stringToPowertrain(value: String): Powertrain = Powertrain.valueOf(value)

    @TypeConverter
    fun transmissionToString(value: Transmission): String = value.name

    @TypeConverter
    fun stringToTransmission(value: String): Transmission = Transmission.valueOf(value)

    @TypeConverter
    fun serviceCategoryToString(value: ServiceCategory): String = value.name

    @TypeConverter
    fun stringToServiceCategory(value: String): ServiceCategory = ServiceCategory.valueOf(value)

    @TypeConverter
    fun expenseCategoryToString(value: ExpenseCategory): String = value.name

    @TypeConverter
    fun stringToExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
}
