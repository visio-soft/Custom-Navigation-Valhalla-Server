package org.maplibre.navigation.android.navigation.ui.v5;

import androidx.annotation.Nullable;
import javax.annotation.processing.Generated;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_NavigationLauncherOptions extends NavigationLauncherOptions {

  private final DirectionsRoute directionsRoute;

  @Nullable
  private final Integer lightThemeResId;

  @Nullable
  private final Integer darkThemeResId;

  private final boolean shouldSimulateRoute;

  private final boolean waynameChipEnabled;

  @Nullable
  private final CameraPosition initialMapCameraPosition;

  private AutoValue_NavigationLauncherOptions(
      DirectionsRoute directionsRoute,
      @Nullable Integer lightThemeResId,
      @Nullable Integer darkThemeResId,
      boolean shouldSimulateRoute,
      boolean waynameChipEnabled,
      @Nullable CameraPosition initialMapCameraPosition) {
    this.directionsRoute = directionsRoute;
    this.lightThemeResId = lightThemeResId;
    this.darkThemeResId = darkThemeResId;
    this.shouldSimulateRoute = shouldSimulateRoute;
    this.waynameChipEnabled = waynameChipEnabled;
    this.initialMapCameraPosition = initialMapCameraPosition;
  }

  @Override
  public DirectionsRoute directionsRoute() {
    return directionsRoute;
  }

  @Nullable
  @Override
  public Integer lightThemeResId() {
    return lightThemeResId;
  }

  @Nullable
  @Override
  public Integer darkThemeResId() {
    return darkThemeResId;
  }

  @Override
  public boolean shouldSimulateRoute() {
    return shouldSimulateRoute;
  }

  @Override
  public boolean waynameChipEnabled() {
    return waynameChipEnabled;
  }

  @Nullable
  @Override
  public CameraPosition initialMapCameraPosition() {
    return initialMapCameraPosition;
  }

  @Override
  public String toString() {
    return "NavigationLauncherOptions{"
        + "directionsRoute=" + directionsRoute + ", "
        + "lightThemeResId=" + lightThemeResId + ", "
        + "darkThemeResId=" + darkThemeResId + ", "
        + "shouldSimulateRoute=" + shouldSimulateRoute + ", "
        + "waynameChipEnabled=" + waynameChipEnabled + ", "
        + "initialMapCameraPosition=" + initialMapCameraPosition
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof NavigationLauncherOptions) {
      NavigationLauncherOptions that = (NavigationLauncherOptions) o;
      return this.directionsRoute.equals(that.directionsRoute())
          && (this.lightThemeResId == null ? that.lightThemeResId() == null : this.lightThemeResId.equals(that.lightThemeResId()))
          && (this.darkThemeResId == null ? that.darkThemeResId() == null : this.darkThemeResId.equals(that.darkThemeResId()))
          && this.shouldSimulateRoute == that.shouldSimulateRoute()
          && this.waynameChipEnabled == that.waynameChipEnabled()
          && (this.initialMapCameraPosition == null ? that.initialMapCameraPosition() == null : this.initialMapCameraPosition.equals(that.initialMapCameraPosition()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= directionsRoute.hashCode();
    h$ *= 1000003;
    h$ ^= (lightThemeResId == null) ? 0 : lightThemeResId.hashCode();
    h$ *= 1000003;
    h$ ^= (darkThemeResId == null) ? 0 : darkThemeResId.hashCode();
    h$ *= 1000003;
    h$ ^= shouldSimulateRoute ? 1231 : 1237;
    h$ *= 1000003;
    h$ ^= waynameChipEnabled ? 1231 : 1237;
    h$ *= 1000003;
    h$ ^= (initialMapCameraPosition == null) ? 0 : initialMapCameraPosition.hashCode();
    return h$;
  }

  static final class Builder extends NavigationLauncherOptions.Builder {
    private DirectionsRoute directionsRoute;
    private Integer lightThemeResId;
    private Integer darkThemeResId;
    private boolean shouldSimulateRoute;
    private boolean waynameChipEnabled;
    private CameraPosition initialMapCameraPosition;
    private byte set$0;
    Builder() {
    }
    @Override
    public NavigationLauncherOptions.Builder directionsRoute(DirectionsRoute directionsRoute) {
      if (directionsRoute == null) {
        throw new NullPointerException("Null directionsRoute");
      }
      this.directionsRoute = directionsRoute;
      return this;
    }
    @Override
    public NavigationLauncherOptions.Builder lightThemeResId(Integer lightThemeResId) {
      this.lightThemeResId = lightThemeResId;
      return this;
    }
    @Override
    public NavigationLauncherOptions.Builder darkThemeResId(Integer darkThemeResId) {
      this.darkThemeResId = darkThemeResId;
      return this;
    }
    @Override
    public NavigationLauncherOptions.Builder shouldSimulateRoute(boolean shouldSimulateRoute) {
      this.shouldSimulateRoute = shouldSimulateRoute;
      set$0 |= (byte) 1;
      return this;
    }
    @Override
    public NavigationLauncherOptions.Builder waynameChipEnabled(boolean waynameChipEnabled) {
      this.waynameChipEnabled = waynameChipEnabled;
      set$0 |= (byte) 2;
      return this;
    }
    @Override
    public NavigationLauncherOptions.Builder initialMapCameraPosition(@Nullable CameraPosition initialMapCameraPosition) {
      this.initialMapCameraPosition = initialMapCameraPosition;
      return this;
    }
    @Override
    public NavigationLauncherOptions build() {
      if (set$0 != 3
          || this.directionsRoute == null) {
        StringBuilder missing = new StringBuilder();
        if (this.directionsRoute == null) {
          missing.append(" directionsRoute");
        }
        if ((set$0 & 1) == 0) {
          missing.append(" shouldSimulateRoute");
        }
        if ((set$0 & 2) == 0) {
          missing.append(" waynameChipEnabled");
        }
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_NavigationLauncherOptions(
          this.directionsRoute,
          this.lightThemeResId,
          this.darkThemeResId,
          this.shouldSimulateRoute,
          this.waynameChipEnabled,
          this.initialMapCameraPosition);
    }
  }

}
