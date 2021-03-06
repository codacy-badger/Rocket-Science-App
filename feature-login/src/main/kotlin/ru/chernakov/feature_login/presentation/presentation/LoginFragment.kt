package ru.chernakov.feature_login.presentation.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import ru.chernakov.core_base.util.RequestCodeGenerator
import ru.chernakov.core_base.util.lifecycle.SafeObserver
import ru.chernakov.core_ui.extension.android.app.hideKeyboard
import ru.chernakov.core_ui.extension.android.widget.addTextChangedListener
import ru.chernakov.core_ui.presentation.fragment.BaseFragment
import ru.chernakov.core_ui.presentation.viewmodel.BaseViewModel
import ru.chernakov.core_ui.util.animation.BounceInterpolator
import ru.chernakov.feature_login.R
import ru.chernakov.feature_login.presentation.navigation.LoginNavigation
import java.util.concurrent.TimeUnit

class LoginFragment : BaseFragment() {
    private val loginViewModel: LoginViewModel by viewModel()
    private val navigation: LoginNavigation by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            runStartAnimation()
        }
        btGoogleSign.setOnClickListener { v ->
            context?.let {
                val bounce = AnimationUtils.loadAnimation(it, R.anim.bounce).apply {
                    interpolator = BounceInterpolator(GOOGLE_SIGN_AMPLITUDE, GOOGLE_SIGN_FREQUENCY)
                }
                v.startAnimation(bounce)
            }
            googleSignIn()
        }
        btRegister.setOnClickListener {
            goToRegister()
        }
        btLogin.setOnClickListener { onLogin() }
        tvForgotPassword.setOnClickListener { onResetPasswordClick() }
        titEmail.addTextChangedListener {
            afterTextChanged {
                it?.let {
                    if (loginViewModel.isEmailValid(it)) {
                        tilEmail.error = null
                    }
                }
            }
        }
        titPassword.addTextChangedListener {
            afterTextChanged {
                it?.let {
                    if (loginViewModel.isPasswordValid(it)) {
                        tilPassword.error = null
                    }
                }
            }
        }
        observeEvents()
    }

    private fun observeEvents() {
        loginViewModel.signInEvent.observe(viewLifecycleOwner, SafeObserver {
            onAuthResult(it)
        })
        loginViewModel.authErrorEvent.observe(viewLifecycleOwner, SafeObserver {
            val authErrorMessage = when (it) {
                is FirebaseAuthInvalidUserException -> getString(R.string.msg_error_auth_user)
                is FirebaseAuthEmailException -> getString(R.string.msg_error_auth_email)
                is FirebaseAuthInvalidCredentialsException -> getString(R.string.msg_error_auth_credentials)
                else -> it.localizedMessage
            }
            showMessage(authErrorMessage)
        })
        loginViewModel.resetPasswordEvent.observe(viewLifecycleOwner, SafeObserver {
            val message = if (it) {
                getString(R.string.msg_reset_pass_email_sended)
            } else {
                getString(R.string.msg_reset_pass_error)
            }
            showMessage(message)
        })
        loginViewModel.resendEmailAccessEvent.observe(viewLifecycleOwner, SafeObserver {
            tvForgotPassword.isClickable = it
        })
        loginViewModel.resendEmailTimerEvent.observe(viewLifecycleOwner, SafeObserver {
            val remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(it).toInt()
            tvForgotPassword.text = if (remainingSeconds > 0) {
                resources.getQuantityString(
                    R.plurals.resend_in_x_seconds, remainingSeconds, remainingSeconds
                )
            } else {
                getString(R.string.msg_forgot_password)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account?.let { authWithGoogle(it) }
        }
    }

    private fun onLogin() {
        val isEmailValid = loginViewModel.isEmailValid(titEmail.editableText)
        val isPasswordValid = loginViewModel.isPasswordValid(titPassword.editableText)
        if (isEmailValid && isPasswordValid) {
            activity?.hideKeyboard()
            loginViewModel.signInWithEmailAndPassword(
                titEmail.text.toString().trim(),
                titPassword.text.toString().trim()
            )
        } else {
            if (!isEmailValid) {
                titEmail.requestFocus()
                tilEmail.error = getString(R.string.msg_error_email)
            }
            if (!isPasswordValid) {
                titPassword.requestFocus()
                tilPassword.error = getString(R.string.msg_error_password)
            }
        }
    }

    private fun googleSignIn() {
        startActivityForResult(
            loginViewModel.getGoogleSignInIntent(),
            RC_SIGN_IN
        )
    }

    private fun authWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        activity?.let {
            loginViewModel.firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { onAuthResult(it.isSuccessful) }
        }
    }

    private fun onAuthResult(isLogged: Boolean) {
        if (isLogged) goToNextScreen() else showMessage()
    }

    private fun onResetPasswordClick() {
        val isEmailValid = loginViewModel.isEmailValid(titEmail.editableText)
        if (isEmailValid) {
            loginViewModel.resetPassword(titEmail.editableText.toString().trim())
        } else {
            titEmail.requestFocus()
            tilEmail.error = getString(R.string.msg_error_email)
        }
    }

    private fun runStartAnimation() {
        vgHeader.animation = AnimationUtils.loadAnimation(context, R.anim.up_to_down)
        vgFooter.animation = AnimationUtils.loadAnimation(context, R.anim.down_to_up)
        tilEmail.animation = AnimationUtils.loadAnimation(context, R.anim.left_to_right)
        tilPassword.animation = AnimationUtils.loadAnimation(context, R.anim.right_to_left)
    }

    private fun showMessage(message: String? = null) {
        val mes = message ?: getString(R.string.msg_error_auth)
        view?.let { Snackbar.make(it, mes, Snackbar.LENGTH_LONG).show() }
    }

    private fun goToNextScreen() {
        navigation.fromLoginToAppFeatures()
    }

    private fun goToRegister() {
        navigation.fromLoginToRegister()
    }

    override fun getLayout(): Int = R.layout.fragment_login

    override fun obtainViewModel(): BaseViewModel = loginViewModel

    companion object {
        private const val GOOGLE_SIGN_AMPLITUDE = 0.2
        private const val GOOGLE_SIGN_FREQUENCY = 20.0
        private val RC_SIGN_IN = RequestCodeGenerator.next
    }
}