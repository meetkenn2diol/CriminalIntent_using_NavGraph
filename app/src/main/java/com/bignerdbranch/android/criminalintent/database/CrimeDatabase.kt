package com.bignerdbranch.android.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdbranch.android.criminalintent.Crime

@Database(entities = [Crime::class], version = 3, exportSchema = false)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {
    abstract fun crimeDao(): CrimeDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''")
    }
}
val migration_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
   /*     database.execSQL("CREATE TABLE IF NOT EXISTS temp_table(id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL,date INTEGER NOT NULL,isSolved INTEGER NOT NULL,suspect TEXT NOT NULL,suspectPhoneNumber TEXT NOT NULL)")
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspectPhoneNumber TEXT NOT NULL DEFAULT ''")
        database.execSQL("INSERT INTO Crime (suspectPhoneNumber) SELECT suspectPhoneNumber FROM temp_table")*/
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspectPhoneNumber TEXT NOT NULL DEFAULT ''")

    }
}
val migration_1_3 = object : Migration(1, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspectPhoneNumber TEXT NOT NULL DEFAULT ''")
    }
}