# ONNX Runtime
-keep class com.microsoft.onnxruntime.** { *; }
-keepclassmembers class com.microsoft.onnxruntime.** { *; }

# Android Lifecycle
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent <methods>;
}

# Fragment
-keep class androidx.fragment.** { *; }

# Material Design
-keep class com.google.android.material.** { *; }
-keepclassmembers class com.google.android.material.** { *; }

# ViewBinding (Disable for now - not needed in Proguard)
-keepclassmembers class *.databinding.* {
    public <init>(...);
    public static *.inflate(...);
}

# Models & Data Classes
-keep class com.example.gocheck.model.** { *; }
-keepclassmembers class com.example.gocheck.model.** {
    <init>(...);
    public <fields>;
}

# General
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-verbose
