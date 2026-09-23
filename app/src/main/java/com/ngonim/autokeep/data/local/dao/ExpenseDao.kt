package com.ngonim.autokeep.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ngonim.autokeep.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query(
        """
        SELECT * FROM expenses
        WHERE vehicle_id = :vehicleId
        ORDER BY performed_at_epoch_day DESC, created_at_epoch_ms DESC
        """,
    )
    fun observeForVehicle(vehicleId: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(expense: ExpenseEntity): Long

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query(
        """
        SELECT COALESCE(SUM(cost_minor), 0) FROM expenses
        WHERE vehicle_id = :vehicleId
        """,
    )
    fun observeTotalCostMinor(vehicleId: Long): Flow<Long>
}
