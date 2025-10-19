package com.example.drugstore.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.drugstore.data.model.Patient
import com.example.drugstore.data.model.Pharmacist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

sealed class UserProfile {
    data class PatientProfile(val patient: Patient) : UserProfile()
    data class PharmacistProfile(val pharmacist: Pharmacist) : UserProfile()
    object Loading : UserProfile()
    object Error : UserProfile()
}

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // LiveData to hold the user profile state
    private val _userProfile = MutableLiveData<UserProfile>(UserProfile.Loading)
    val userProfile: LiveData<UserProfile> = _userProfile

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _userProfile.value = UserProfile.Error
            return
        }

        // First, check if the user is a patient
        val patientRef = database.getReference("patients").child(userId)
        patientRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val patient = snapshot.getValue(Patient::class.java)
                    if (patient != null) {
                        _userProfile.value = UserProfile.PatientProfile(patient)
                        return
                    }
                }

                // If not found as a patient, check if they are a pharmacist
                val pharmacistRef = database.getReference("pharmacists").child(userId)
                pharmacistRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(pharmacistSnapshot: DataSnapshot) {
                        if (pharmacistSnapshot.exists()) {
                            val pharmacist = pharmacistSnapshot.getValue(Pharmacist::class.java)
                            if (pharmacist != null) {
                                _userProfile.value = UserProfile.PharmacistProfile(pharmacist)
                            } else {
                                _userProfile.value = UserProfile.Error
                            }
                        } else {
                            _userProfile.value = UserProfile.Error // User not found in either role
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _userProfile.value = UserProfile.Error
                        Log.e("ProfileViewModel", "Failed to fetch pharmacist data: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                _userProfile.value = UserProfile.Error
                Log.e("ProfileViewModel", "Failed to fetch patient data: ${error.message}")
            }
        })
    }

    fun logout() {
        auth.signOut()
    }
}
