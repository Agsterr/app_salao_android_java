# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preservar informações de linha para debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Manter classes do Material Design
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Manter classes do projeto
-keep class com.focodevsistemas.gerenciamento.** { *; }

# Manter classes do SQLite
-keep class android.database.sqlite.** { *; }
-keep class * extends android.database.sqlite.SQLiteOpenHelper { *; }

# Manter classes de Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends android.app.Activity

# Manter classes de BroadcastReceiver
-keep public class * extends android.content.BroadcastReceiver

# Manter classes de ContentProvider
-keep public class * extends android.content.ContentProvider

# Manter classes de FileProvider
-keep class androidx.core.content.FileProvider { *; }

# Manter classes do Billing
-keep class com.android.billingclient.** { *; }

# Manter classes do WorkManager
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Manter classes de Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Manter classes Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}