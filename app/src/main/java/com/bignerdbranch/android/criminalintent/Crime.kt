package com.bignerdbranch.android.criminalintent

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID
import java.util.Date

/**
 * Crime: The class Used by the android Room database to create the Table
 */
@Entity
@Parcelize
data class Crime(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var suspect: String = "",
    var suspectPhoneNumber: String = ""
) : Parcelable{
    val photoFileName get() = "IMG_${id}.jpg"
}






