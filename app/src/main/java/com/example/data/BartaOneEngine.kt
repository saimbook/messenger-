package com.example.data

import android.content.Context
import android.util.Log

object BartaOneEngine {
    private const val TAG = "BartaOneEngine"

    fun onUserAuthenticated(context: Context, uid: String, name: String, email: String) {
        Log.d(TAG, "BARTA One Engine authenticated user: $name ($uid)")
    }
}
