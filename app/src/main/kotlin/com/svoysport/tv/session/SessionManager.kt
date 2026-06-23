package com.svoysport.tv.session

import androidx.compose.runtime.mutableStateOf

object SessionManager {
    val isLoggedIn = mutableStateOf(false)
    val userEmail  = mutableStateOf("useremail@gmail.com")
}
