package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DataMode(val titleBn: String, val descBn: String) {
    SAVER("ডেটা সাশ্রয়ী (Data Saver)", "অল্প মেগাবাইটে দ্রুত মেসেজিং ও অপটিমাইজড অডিও"),
    STANDARD("স্ট্যান্ডার্ড (Standard)", "স্বাভাবিক গতি ও ব্যালেন্সড ফাইল কোয়ালিটি"),
    PRO("প্রো মোড (HD Quality)", "ফুল রেজোলিউশন মিডিয়া ও হাই-বিটরেট কল")
}

enum class NetworkQuality(val labelBn: String) {
    GOOD("অনলাইন (3G/4G/Wi-Fi)"),
    NORMAL("সাধারণ সংযোগ"),
    WEAK("দুর্বল নেটওয়ার্ক (2G)"),
    VERY_WEAK("খুব ধীরগতির নেটওয়ার্ক"),
    DISCONNECTED("অফলাইন")
}

object AppSettingsManager {
    private val _displayName = MutableStateFlow("BARTA ব্যবহারকারী")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _phoneNumber = MutableStateFlow("info.bappy1996@gmail.com")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _aboutStatus = MutableStateFlow("বার্তা (BARTA) ব্যবহার করছি!")
    val aboutStatus: StateFlow<String> = _aboutStatus.asStateFlow()

    private val _twoStepVerification = MutableStateFlow(false)
    val twoStepVerification: StateFlow<Boolean> = _twoStepVerification.asStateFlow()

    fun setDisplayName(name: String) {
        if (name.isNotBlank()) _displayName.value = name
    }

    fun setPhoneNumber(phoneOrEmail: String) {
        if (phoneOrEmail.isNotBlank()) _phoneNumber.value = phoneOrEmail
    }

    fun setAboutStatus(status: String) {
        if (status.isNotBlank()) _aboutStatus.value = status
    }

    fun setTwoStepVerification(enabled: Boolean) {
        _twoStepVerification.value = enabled
    }
}
