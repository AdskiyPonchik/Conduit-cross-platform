package io.realworld.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realworld.android.data.UserRepo
import io.realworld.api.models.entities.User
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    fun getCurrentUser(token: String) = viewModelScope.launch {
        val user = UserRepo.getCurrentUser(token)
        _user.postValue(user)
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _loginError.postValue(null)
        val user = UserRepo.login(email, password)
        if (user != null) {
            _user.postValue(user)
        } else {
            _loginError.postValue("Login fehlgeschlagen. Bitte E-Mail und Passwort prüfen.")
        }
    }

    fun signup(username: String, email: String, password: String) = viewModelScope.launch {
        _loginError.postValue(null)
        val user = UserRepo.signup(username, email, password)
        if (user != null) {
            _user.postValue(user)
        } else {
            _loginError.postValue("Registrierung fehlgeschlagen. Bitte die Eingaben prüfen.")
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