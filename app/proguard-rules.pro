# ==================== Core ====================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ==================== Kotlin ====================
-dontwarn kotlin.**
-keepclassmembers,allowobfuscation class * {
    @kotlin.Metadata ***;
}
-dontwarn kotlin.Unit

# ==================== Hilt ====================
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.component.HiltAndroidApp class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.components.SingletonComponent class *
-keep,allowobfuscation,allowshrinking @dagger.Module class *
-keep,allowobfuscation,allowshrinking @dagger.Provides class *
-keep,allowobfuscation @dagger.Provides abstract class *
-keep,allowobfuscation @dagger.Component class *
-keep,allowobfuscation @dagger.Binds class *

# Hilt ViewModel
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.lifecycle.HiltViewModel class *
-keep,allowobfuscation class * implements dagger.hilt.android.lifecycle.HiltViewModel

# Hilt fields (reflective access)
-keepclassmembers,allowobfuscation,allowshrinking @dagger.hilt.android.internal.managers.ComponentReference final class * {
    *** m***;
}

# ==================== Room ====================
-keepclassmembers,allowobfuscation,allowshrinking class * implements androidx.room.EntityDeletionOrUpdateAdapter {
    <init>(androidx.sqlite.db.SupportSQLiteDatabase);
}
-keep class androidx.room.** { *; }
-keep class * implements androidx.room.RoomDatabase

# ==================== Coil (Image Loading) ====================
-keep class coil3.** { *; }
-keep class * extends coil3.image.ImageLoader { *; }
-keep class * implements coil3.request.ImageLoaderComponent { *; }

# ==================== Media3 (ExoPlayer) ====================
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.exoplayer.** { *; }

# ==================== zip4j (Compression) ====================
-keep class net.lingala.zip4j.** { *; }
-dontwarn net.lingala.zip4j.**

# ==================== DataStore ====================
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ==================== Serialization ====================
-keep class kotlinx.** { *; }
-dontwarn kotlinx.**
-keepclassmembers class **$$serializer {
    static ** MODULE$;
}

# ==================== General ====================
# Keep Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}
# Keep data classes
-keep class com.filemanager.app.domain.model.** { *; }
-keep class com.filemanager.app.data.local.** { *; }
