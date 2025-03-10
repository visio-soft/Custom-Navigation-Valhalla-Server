# Consumer proguard rules for libandroid-navigation

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames classPublicSuffixDatabase

# --- Java ---
-dontwarn java.awt.Color

# --- com.mapbox.api.directions.v5.MapboxDirections ---
-dontwarn com.sun.xml.internal.ws.spi.db.BindingContextFactory

# --- AutoValue ---
# AutoValue annotations are retained but dependency is compileOnly.
-dontwarn com.google.auto.value.**
-dontwarn com.ryanharter.auto.value.**
-keep class org.maplibre.navigation.android.navigation.v5.models.ManeuverModifier$Type { *; }
-keep class com.maplibre.** { *; }
-keep class com.mapbox.** { *; }