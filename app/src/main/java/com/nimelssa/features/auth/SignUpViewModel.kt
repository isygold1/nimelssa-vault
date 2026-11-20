package com.nimelssa.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * SignUpViewModel - Handles sign-up with Firestore validation per handoff rules:
 * - users/{uid} creation requires: role=student, approved=false, valid matric (:59)
 * - access_requests/{uid} creation requires: verified token email (:141)
 * - rep_requests requires: uid + status
 */
class SignUpViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Sign-up state
    data class SignUpState(
        val email: String = "",
        val password: String = "",
        val name: String = "",
        val matric: String = "",
        val level: String = "",
        val error: String? = null,
        val isLoading: Boolean = false
    )

    private val _state = MutableFlow(SignUpState())
    val state = _state.asFlow()

    // Sign up user with Firestore profile creation
    fun signUp(state: SignUpState) {
        viewModelScope.launch {
            this.state.value = this.state.value.copy(isLoading = true, error = null)

            // Create auth user
            auth.createUserWithEmailAndPassword(state.email, state.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser!!
                        val uid = user.uid

                        // Create Firestore user profile with required fields:
                        // role=student, approved=false, valid matric
                        firestore.collection("users").document(uid).set(
                            mapOf(
                                "role" to "student",
                                "approved" to false,
                                "matric" to state.matric,
                                "name" to state.name,
                                "level" to state.level,
                                "createdAt" to java.lang.System.currentTimeMillis()
                            )
                        ).addOnSuccessListener {
                            // After user creation and email verification, allow access request
                            // access_requests/{uid} creation requires verified token email
                            // This is handled separately after email verification
                        }.addOnFailureListener { e ->
                            this.state.value = this.state.value.copy(
                                error = "Failed to create profile: ${e.message}"
                            )
                        }
                    } else {
                        this.state.value = this.state.value.copy(
                            error = "Sign up failed: ${task.exception?.message}"
                        )
                    }
                    this.state.value = this.state.value.copy(isLoading = false)
                }
        }
    }

    // Request access after email verification
    fun requestAccess(uid: String) {
        viewModelScope.launch {
            if (isActive) {
                firestore.collection("access_requests").document(uid).set(
                    mapOf(
                        "status" to "pending",
                        "requestedAt" to java.lang.System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    // Access request submitted, pending approval
                }.addOnFailureListener { e ->
                    // Handle error
                }
            }
        }
    }

    // Check pending approval status
    fun checkApprovalStatus(uid: String): String? {
        return firestore.collection("users").document(uid).get()
            ?.getString("approved")
            ?: null
    }

    // Sign in function
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign-in success - check approval status
                    }
                }
        }
    }
}