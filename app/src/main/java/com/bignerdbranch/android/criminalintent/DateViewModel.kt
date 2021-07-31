package com.bignerdbranch.android.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class DateViewModel : ViewModel() {
    companion object {
        private val mutableSelectedDate = MutableLiveData<Date>()
        val selectedDate: LiveData<Date> get() = mutableSelectedDate

        fun setDate(date: Date) {
            mutableSelectedDate.value = date
        }
    }
}