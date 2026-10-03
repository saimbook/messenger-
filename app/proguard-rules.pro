# Proguard rules for BARTA Android
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.jetbrains.annotations.Nullable <fields>;
    @org.jetbrains.annotations.NotNull <fields>;
}
-keep class com.example.data.** { *; }
-keep class com.example.data.admin.** { *; }
