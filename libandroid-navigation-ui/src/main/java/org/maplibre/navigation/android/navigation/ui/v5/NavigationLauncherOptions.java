package org.maplibre.navigation.android.navigation.ui.v5;

import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.android.camera.CameraPosition;

@AutoValue
public abstract class NavigationLauncherOptions extends NavigationUiOptions {

  @Nullable
  public abstract CameraPosition initialMapCameraPosition();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder directionsRoute(DirectionsRoute directionsRoute);

    public abstract Builder lightThemeResId(Integer lightThemeResId);

    public abstract Builder darkThemeResId(Integer darkThemeResId);

    public abstract Builder shouldSimulateRoute(boolean shouldSimulateRoute);

    public abstract Builder waynameChipEnabled(boolean waynameChipEnabled);

    public abstract Builder initialMapCameraPosition(@Nullable CameraPosition initialMapCameraPosition);

    public abstract NavigationLauncherOptions build();
  }

  public static NavigationLauncherOptions.Builder builder() {
    return new AutoValue_NavigationLauncherOptions.Builder()
      .shouldSimulateRoute(false)
      .waynameChipEnabled(true);
  }
}
