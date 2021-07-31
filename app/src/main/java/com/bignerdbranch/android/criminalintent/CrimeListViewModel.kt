package com.bignerdbranch.android.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class CrimeListViewModel : ViewModel() {
    private val crimeRepository = CrimeRepository.getInstance()
    val crimeListLiveData: LiveData<List<Crime>> = crimeRepository.getCrimes()

    fun addCrime(crime: Crime){
        crimeRepository.addCrime(crime)
    }
}