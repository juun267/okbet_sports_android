package org.cxct.sportlottery.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.login.NAME_LOGIN

class MainViewModel(application: Application) : ViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

    private val loginRepository by lazy {
        LoginRepository(
            application.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
        )
    }

    init {
    }

    fun logout() {
        loginRepository.logout()
    }

    override fun onCleared() {
        super.onCleared()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalAccessException("Unable to construct view model")
        }
    }
}