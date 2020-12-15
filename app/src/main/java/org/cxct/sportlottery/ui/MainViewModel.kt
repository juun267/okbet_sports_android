package org.cxct.sportlottery.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.cxct.sportlottery.repository.LoginRepository

class MainViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    val token: LiveData<String?> by lazy {
        loginRepository.token
    }

    init {
    }

    fun logout() {
        loginRepository.logout()
    }

    override fun onCleared() {
        super.onCleared()
    }
}