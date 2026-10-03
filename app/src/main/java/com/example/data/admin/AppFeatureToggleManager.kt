package com.example.data.admin

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppFeatureToggleManager {
    private val _features = MutableStateFlow<List<AppFeatureItem>>(
        listOf(
            AppFeatureItem("AUDIO_CALLING", "ফ্রি অডিও কলিং (Free Voice Calls)", "লো-ব্যান্ডউইথ ওপাস কোডেক ভিত্তিক ক্রিস্টাল ক্লিয়ার কল", true, "CALLS"),
            AppFeatureItem("VOICE_MESSAGES", "ভয়েস মেসেজিং ও নোটস (Voice Notes)", "আল্ট্রা-কম্প্রেসড অডিও রেকর্ড ও প্লেব্যাক", true, "MEDIA"),
            AppFeatureItem("END_TO_END_ENCRYPTION", "এন্ড-টু-এন্ড এনক্রিপশন (E2EE)", "হার্ডওয়্যার কিস্টোর এনক্রিপশন ও সিকিউরিটি", true, "SECURITY"),
            AppFeatureItem("DATA_SAVER_MODE", "২জি লো-ইন্টারনেট অপটিমাইজেশন", "স্বয়ংক্রিয় কম ব্যান্ডউইথ মোড ও দ্রুত ডেলিভারি", true, "GENERAL"),
            AppFeatureItem("AI_CHAT_ASSISTANT", "স্মার্ট এআই অটো-রেসপন্ডার ও ট্রান্সলেটর", "বাংলা ভাষা প্রসেসিং ও তাৎক্ষণিক সহায়তা", true, "AI"),
            AppFeatureItem("DRIVER_DISPATCH_PANEL", "ড্রাইভার ও ডেলিভারি প্যানেল", "রাইডারদের সাথে সরাসরি লোকেশন ও যোগাযোগ সিস্টেম", true, "GENERAL"),
            AppFeatureItem("MEDIA_AUTO_DOWNLOAD", "অটো মিডিয়া ডাউনলোড", "ওয়াইফাই বা মোবাইলে অটো মিডিয়া নামানো", false, "MEDIA"),
            AppFeatureItem("ADMIN_LIVE_MONITORING", "রিয়েলটাইম সিস্টেম মনিটরিং", "লাইভ কানেকশন ও ট্রাফিক অডিট কনসোল", true, "SECURITY")
        )
    )
    val features: StateFlow<List<AppFeatureItem>> = _features.asStateFlow()

    fun toggleFeature(key: String, isEnabled: Boolean) {
        _features.value = _features.value.map {
            if (it.featureKey == key) it.copy(isEnabled = isEnabled) else it
        }

        try {
            val db = FirebaseDatabase.getInstance()
            db.getReference("appFeatures").child(key).setValue(isEnabled)
        } catch (e: Exception) {
            // Log or fallback
        }
    }

    fun isFeatureEnabled(key: String): Boolean {
        return _features.value.find { it.featureKey == key }?.isEnabled ?: true
    }

    fun addCustomFeature(key: String, nameBn: String, descBn: String, category: String = "GENERAL") {
        val newItem = AppFeatureItem(key, nameBn, descBn, true, category)
        _features.value = _features.value + newItem

        try {
            val db = FirebaseDatabase.getInstance()
            db.getReference("appFeatures").child(key).setValue(true)
        } catch (e: Exception) {
            // Log or fallback
        }
    }
}
