package com.example.ks1compose.domain.utils

import android.app.Application
import com.example.ks1compose.data.datasource.remote.TokenManager

class KS1Application : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}