package ru.chernakov.feature_register.presentation

import android.text.Editable
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import ru.chernakov.core_base.util.lifecycle.SingleLiveEvent
import ru.chernakov.core_ui.presentation.viewmodel.BaseViewModel

class RegisterViewModel(private val firebaseAuth: FirebaseAuth) : BaseViewModel() {

    var registerSuccessEvent = SingleLiveEvent<Boolean>()
    var registerErrorEvent = SingleLiveEvent<Exception>()

    fun isEmailValid(email: Editable) = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isPasswordValid(password: Editable) = password.length >= PASSWORD_MIN_LENGTH

    fun isPasswordConfirmValid(password: Editable, confirm: Editable) = password.toString() == confirm.toString()

    fun registerUser(email: String, password: String) {
        loading.postValue(true)
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                loading.postValue(false)
                registerSuccessEvent.postValue(it.isSuccessful)
            }
            .addOnFailureListener {
                loading.postValue(false)
                registerErrorEvent.postValue(it)
            }
    }

    companion object {
        private const val PASSWORD_MIN_LENGTH = 8
    }
}