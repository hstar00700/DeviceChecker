package kr.hstar.devicechecker

import androidx.lifecycle.ViewModel

open class BaseViewModel(): ViewModel() {
    override fun onCleared() {
        super.onCleared()
    }
}