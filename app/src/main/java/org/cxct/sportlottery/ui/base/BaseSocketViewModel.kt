package org.cxct.sportlottery.ui.base

import org.cxct.sportlottery.repository.LoginRepository

abstract class BaseSocketViewModel(val loginRepository: LoginRepository) : BaseViewModel()