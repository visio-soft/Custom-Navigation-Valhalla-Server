# Consumer proguard rules for libandroid-navigation-ui

# --- Picasso ---
-dontwarn com.squareup.okhttp.**

# --- com.mapbox.api.directions.v5.MapboxDirections ---
-dontwarn com.sun.xml.internal.ws.spi.db.BindingContextFactory

# --- com.amazonaws.util.json.JacksonFactory ---
-dontwarn com.fasterxml.jackson.core.**

# --- MapLibre ---
-dontwarn org.maplibre.navigation.android.ui.v5.**
-keep class org.maplibre.navigation.android.navigation.v5.models.ManeuverModifier$Type { *; }
-keep class okhttp3.internal.publicsuffix.PublicSuffixDatabase { *; }
-keep class okhttp3.** { *; }

