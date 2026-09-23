package com.ngonim.autokeep.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ngonim.autokeep.data.local.dao.ExpenseDao
import com.ngonim.autokeep.data.local.dao.ServiceItemDao
import com.ngonim.autokeep.data.local.dao.ServiceVisitDao
import com.ngonim.autokeep.data.local.dao.VehicleDao
import com.ngonim.autokeep.data.local.entity.ExpenseEntity
import com.ngonim.autokeep.data.local.entity.ServiceItemEntity
import com.ngonim.autokeep.data.local.entity.ServiceVisitEntity
import com.ngonim.autokeep.data.local.entity.ServiceVisitItemEntity
import com.ngonim.autokeep.data.local.entity.VehicleEntity
import com.ngonim.autokeep.data.local.view.ServiceHealthView

@Database(
    entities = [
        VehicleEntity::class,
        ServiceItemEntity::class,
        ServiceVisitEntity::class,
        ServiceVisitItemEntity::class,
        ExpenseEntity::class,
    ],
    views = [ServiceHealthView::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AutoKeepDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun serviceItemDao(): ServiceItemDao
    abstract fun serviceVisitDao(): ServiceVisitDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val NAME = "autokeep.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN powertrain TEXT NOT NULL DEFAULT 'GASOLINE'")
                db.execSQL("ALTER TABLE vehicles ADD COLUMN transmission TEXT NOT NULL DEFAULT 'AUTOMATIC'")
                db.execSQL("ALTER TABLE service_items ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER'")
                db.execSQL("ALTER TABLE service_items ADD COLUMN catalog_key TEXT")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `service_visits` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `vehicle_id` INTEGER NOT NULL,
                        `mileage` INTEGER NOT NULL,
                        `performed_at_epoch_day` INTEGER NOT NULL,
                        `cost_minor` INTEGER,
                        `workshop` TEXT,
                        `notes` TEXT,
                        `receipt_uri` TEXT,
                        `created_at_epoch_ms` INTEGER NOT NULL,
                        FOREIGN KEY(`vehicle_id`) REFERENCES `vehicles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_service_visits_vehicle_id` ON `service_visits` (`vehicle_id`)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_service_visits_vehicle_id_performed_at_epoch_day` ON `service_visits` (`vehicle_id`, `performed_at_epoch_day`)",
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `service_visit_items` (
                        `visit_id` INTEGER NOT NULL,
                        `service_item_id` INTEGER NOT NULL,
                        PRIMARY KEY(`visit_id`, `service_item_id`),
                        FOREIGN KEY(`visit_id`) REFERENCES `service_visits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`service_item_id`) REFERENCES `service_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_service_visit_items_visit_id` ON `service_visit_items` (`visit_id`)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_service_visit_items_service_item_id` ON `service_visit_items` (`service_item_id`)",
                )

                db.execSQL(
                    """
                    INSERT INTO service_visits (
                        id, vehicle_id, mileage, performed_at_epoch_day,
                        cost_minor, workshop, notes, receipt_uri, created_at_epoch_ms
                    )
                    SELECT
                        id, vehicle_id, mileage, performed_at_epoch_day,
                        cost_minor, workshop, notes, receipt_uri, created_at_epoch_ms
                    FROM service_logs
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO service_visit_items (visit_id, service_item_id)
                    SELECT id, service_item_id FROM service_logs
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE IF EXISTS service_logs")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN engine_size TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `expenses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `vehicle_id` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `cost_minor` INTEGER NOT NULL,
                        `performed_at_epoch_day` INTEGER NOT NULL,
                        `notes` TEXT,
                        `created_at_epoch_ms` INTEGER NOT NULL,
                        FOREIGN KEY(`vehicle_id`) REFERENCES `vehicles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_vehicle_id` ON `expenses` (`vehicle_id`)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_expenses_vehicle_id_performed_at_epoch_day` ON `expenses` (`vehicle_id`, `performed_at_epoch_day`)",
                )
            }
        }

        fun create(context: Context): AutoKeepDatabase =
            Room.databaseBuilder(context.applicationContext, AutoKeepDatabase::class.java, NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}
