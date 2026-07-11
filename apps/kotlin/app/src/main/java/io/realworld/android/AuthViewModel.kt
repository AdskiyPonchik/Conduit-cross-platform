package io.realworld.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realworld.android.data.UserRepo
import io.realworld.api.models.entities.User
import kotlinx.coroutines.launch

sealed class AuthStatus {
    object Idle : AuthStatus()
    object Success : AuthStatus()
    data class Error(val msg: String) : AuthStatus()
}

class AuthViewModel : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _authStatus = MutableLiveData<AuthStatus>(AuthStatus.Idle)
    val authStatus: LiveData<AuthStatus> = _authStatus

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    fun getCurrentUser(token: String) = viewModelScope.launch {
        UserRepo.getCurrentUser(token)?.let {
            _user.postValue(it)
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        try {
            val user = UserRepo.login(email, password)
            if (user != null) {
                _user.postValue(user)
                _authStatus.postValue(AuthStatus.Success)
                _loginError.postValue(null)
            } else {
                _authStatus.postValue(AuthStatus.Error("Login fehlgeschlagen."))
                _loginError.postValue("Login fehlgeschlagen. Bitte E-Mail und Passwort prüfen.")
            }
        } catch (e: Exception) {
            _authStatus.postValue(AuthStatus.Error(e.message ?: "Unbekannter Fehler"))
            _loginError.postValue(e.message ?: "Unbekannter Fehler")
        }
    }

    fun resetAuthStatus() {
        _authStatus.postValue(AuthStatus.Idle)
    }

    fun signup(username: String, email: String, password: String) = viewModelScope.launch {
        _loginError.postValue(null)
        UserRepo.signup(username, email, password)?.let {
            _user.postValue(it)
        }
    }

    fun logout() {
        _user.postValue(null)
    }

    fun update(
        bio: String?,
        username: String?,
        image: String?,
        email: String?,
        password: String?
    ) = viewModelScope.launch {
        UserRepo.updateUser(bio, username, image, email, password)?.let {
            _user.postValue(it)
        }
    }
}
