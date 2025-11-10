# Mantener Activities y Services
  -keep public class * extends android.app.Activity
  -keep public class * extends android.app.Application
  -keep public class * extends android.appwidget.AppWidgetProvider

  # Mantener clases de AndroidX
  -keep class androidx.** { *; }
  -dontwarn androidx.**

  # Mantener Material Design
  -keep class com.google.android.material.** { *; }
  -dontwarn com.google.android.material.**

  # Mantener data classes (para no romper tus HistoryItem, etc)
  -keepclassmembers class ** {
      @kotlin.jvm.JvmField public <fields>;
  }

  # Mantener atributos para stack traces legibles
  -keepattributes SourceFile,LineNumberTable
  -renamesourcefileattribute SourceFile

  # Mantener enums (para InfusionType, ViewMode, etc)
  -keepclassmembers enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
  }

  # Mantener Parcelables si los usas en el futuro
  -keepclassmembers class * implements android.os.Parcelable {
      public static final android.os.Parcelable$Creator *;
  }