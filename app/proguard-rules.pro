# Add project-specific ProGuard rules here.
# For more details, see:
# https://developer.android.com/studio/build/shrink-code

# Keep only the methods and classes used in Firebase and Google services, obfuscate everything else
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

-keepclassmembers class com.example.tbus.Passenger {
    public <init>();
}

# Keep Firebase Database Model Annotations (if using Firebase Realtime Database)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.database.** { *; }

# Firebase Firestore
-keepnames class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }
-dontwarn com.google.firebase.analytics.**

# Firebase Cloud Messaging
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.messaging.**

# Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**

# Safeguard against code removal for Google Play services
-keep class com.google.android.** { *; }
-dontwarn com.google.android.**

# General Rules to Avoid Breaking Code
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Annotation

# Prevent warnings related to Kotlin metadata
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keep class kotlin.jvm.** { *; }
-keepclassmembers class kotlin.jvm.internal.** {
    *;
}

# Rules for Play Integrity API (if used)
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# Keep class members and methods that might be used by reflection or dynamic class loading
-keepclassmembers class * {
    @com.google.firebase.database.IgnoreExtraProperties <fields>;
    public *;
    public static *;
}

# Keep interfaces used in Firebase Realtime Database or Firestore
-keep class com.google.firebase.firestore.EventListener { *; }

# Firebase Dynamic Links (if used)
-keep class com.google.firebase.dynamiclinks.** { *; }
-dontwarn com.google.firebase.dynamiclinks.**

# Google Sign-In (Ensure obfuscation doesn't break this)
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.gms.auth.api.identity.** { *; }
-dontwarn com.google.android.gms.auth.api.signin.**
-dontwarn com.google.android.gms.auth.api.identity.**

# Obfuscate all other classes and methods to enhance security, but keep necessary ones (Firebase, Google APIs)
# Obfuscate everything except for what's explicitly kept.
-keep class * extends java.lang.Exception { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }

# Enforce code shrinking and removal of unused code to reduce exposure
-dontwarn okhttp3.**
-dontwarn com.squareup.okhttp3.**

# Avoid obfuscating any classes needed for reflection (e.g., Firebase, Gson)
-keep class com.google.gson.** { *; }
-keep class com.google.firebase.firestore.model.** { *; }

# Hide method signatures and class names, but keep methods for reflection and serialization
-keepclassmembers class * {
    public *;
    private *;
}

# Securely obfuscate class and method names
# Obfuscate all methods except the necessary ones for reflection and access
-keepclassmembers class * {
    public <methods>;
    private <methods>;
    protected <methods>;
}

# To hide internal methods, avoid including them in stack traces
-dontobfuscate class * {
    @android.annotation.SuppressLint <methods>;
}