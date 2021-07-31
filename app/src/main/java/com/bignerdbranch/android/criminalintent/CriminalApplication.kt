package com.bignerdbranch.android.criminalintent

import android.app.Application
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

public const val CRIMINAL_INTENT_TAG="CI"
private const val DB_NAME = "crime-database.db"

class CriminalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
      /*  Thread { copyDatabase() }.also {
            it.isDaemon = true
            it.run()
        }*/
        CrimeRepository.initialize(this)
    }


    /*private fun copyDatabase() {
        val dbPath = this.getDatabasePath(DB_NAME)

        // If the database already exists, return
        if (dbPath.exists()) {
            Log.d("Activity", "db Path Exists")
            return
        }

        // Make sure we have a path to the file
        dbPath.parentFile.mkdirs()

        // Try to copy database file
        try {
            val inputStream = this.assets.open(DB_NAME)
            val output: OutputStream = FileOutputStream(dbPath)
            val buffer = ByteArray(8192)
            var length: Int
            while (inputStream.read(buffer, 0, 8192).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
            output.flush()
            output.close()
            inputStream.close()
        } catch (e: IOException) {
            Log.d("Activity", "Failed to open file", e)
            e.printStackTrace()
        }
    }*/

}