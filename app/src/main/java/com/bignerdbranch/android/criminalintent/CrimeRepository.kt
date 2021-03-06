package com.bignerdbranch.android.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdbranch.android.criminalintent.database.CrimeDatabase
import com.bignerdbranch.android.criminalintent.database.migration_1_2
import com.bignerdbranch.android.criminalintent.database.migration_1_3
import com.bignerdbranch.android.criminalintent.database.migration_2_3
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database.db"

class CrimeRepository private constructor(context: Context) {

    //INITIALIZE THE DATABASE REPOSITORY
    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2, migration_2_3, migration_1_3).build()

    //CREATE THE REFERENCE TO THE DAO CLASS
    //IMPLEMENT ALL THE METHODS IN THE DAO HERE FOR EASY ACCESS
    private val crimeDao = database.crimeDao()
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir
    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }
    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)
    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    //CREATE THE INSTANCE FOR ACCESING THE SINGLETON OF THIS CLASS
    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                //copyDatabase(context)
                INSTANCE = CrimeRepository(context)
            }
        }

        fun getInstance(): CrimeRepository {
            return INSTANCE
                ?: throw IllegalStateException("CrimeRepository has not been initialized")
        }
    }
}


