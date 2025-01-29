package org.maplibre.navigation.android.navigation.ui.v5;

import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.List;
import javax.annotation.processing.Generated;
import org.maplibre.android.location.engine.LocationEngine;
import org.maplibre.navigation.android.navigation.ui.v5.listeners.BannerInstructionsListener;
import org.maplibre.navigation.android.navigation.ui.v5.listeners.InstructionListListener;
import org.maplibre.navigation.android.navigation.ui.v5.listeners.NavigationListener;
import org.maplibre.navigation.android.navigation.ui.v5.listeners.RouteListener;
import org.maplibre.navigation.android.navigation.ui.v5.listeners.SpeechAnnouncementListener;
import org.maplibre.navigation.android.navigation.ui.v5.voice.SpeechPlayer;
import org.maplibre.navigation.android.navigation.v5.milestone.Milestone;
import org.maplibre.navigation.android.navigation.v5.milestone.MilestoneEventListener;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigationOptions;
import org.maplibre.navigation.android.navigation.v5.routeprogress.ProgressChangeListener;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_NavigationViewOptions extends NavigationViewOptions {

  private final DirectionsRoute directionsRoute;

  @Nullable
  private final Integer lightThemeResId;

  @Nullable
  private final Integer darkThemeResId;

  private final boolean shouldSimulateRoute;

  private final boolean waynameChipEnabled;

  private final MapLibreNavigationOptions navigationOptions;

  @Nullable
  private final RouteListener routeListener;

  @Nullable
  private final NavigationListener navigationListener;

  @Nullable
  private final ProgressChangeListener progressChangeListener;

  @Nullable
  private final MilestoneEventListener milestoneEventListener;

  @Nullable
  private final List<Milestone> milestones;

  @Nullable
  private final BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;

  @Nullable
  private final InstructionListListener instructionListListener;

  @Nullable
  private final SpeechAnnouncementListener speechAnnouncementListener;

  @Nullable
  private final BannerInstructionsListener bannerInstructionsListener;

  @Nullable
  private final SpeechPlayer speechPlayer;

  @Nullable
  private final LocationEngine locationEngine;

  private AutoValue_NavigationViewOptions(
      DirectionsRoute directionsRoute,
      @Nullable Integer lightThemeResId,
      @Nullable Integer darkThemeResId,
      boolean shouldSimulateRoute,
      boolean waynameChipEnabled,
      MapLibreNavigationOptions navigationOptions,
      @Nullable RouteListener routeListener,
      @Nullable NavigationListener navigationListener,
      @Nullable ProgressChangeListener progressChangeListener,
      @Nullable MilestoneEventListener milestoneEventListener,
      @Nullable List<Milestone> milestones,
      @Nullable BottomSheetBehavior.BottomSheetCallback bottomSheetCallback,
      @Nullable InstructionListListener instructionListListener,
      @Nullable SpeechAnnouncementListener speechAnnouncementListener,
      @Nullable BannerInstructionsListener bannerInstructionsListener,
      @Nullable SpeechPlayer speechPlayer,
      @Nullable LocationEngine locationEngine) {
    this.directionsRoute = directionsRoute;
    this.lightThemeResId = lightThemeResId;
    this.darkThemeResId = darkThemeResId;
    this.shouldSimulateRoute = shouldSimulateRoute;
    this.waynameChipEnabled = waynameChipEnabled;
    this.navigationOptions = navigationOptions;
    this.routeListener = routeListener;
    this.navigationListener = navigationListener;
    this.progressChangeListener = progressChangeListener;
    this.milestoneEventListener = milestoneEventListener;
    this.milestones = milestones;
    this.bottomSheetCallback = bottomSheetCallback;
    this.instructionListListener = instructionListListener;
    this.speechAnnouncementListener = speechAnnouncementListener;
    this.bannerInstructionsListener = bannerInstructionsListener;
    this.speechPlayer = speechPlayer;
    this.locationEngine = locationEngine;
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

  @Override
  public MapLibreNavigationOptions navigationOptions() {
    return navigationOptions;
  }

  @Nullable
  @Override
  public RouteListener routeListener() {
    return routeListener;
  }

  @Nullable
  @Override
  public NavigationListener navigationListener() {
    return navigationListener;
  }

  @Nullable
  @Override
  public ProgressChangeListener progressChangeListener() {
    return progressChangeListener;
  }

  @Nullable
  @Override
  public MilestoneEventListener milestoneEventListener() {
    return milestoneEventListener;
  }

  @Nullable
  @Override
  public List<Milestone> milestones() {
    return milestones;
  }

  @Nullable
  @Override
  public BottomSheetBehavior.BottomSheetCallback bottomSheetCallback() {
    return bottomSheetCallback;
  }

  @Nullable
  @Override
  public InstructionListListener instructionListListener() {
    return instructionListListener;
  }

  @Nullable
  @Override
  public SpeechAnnouncementListener speechAnnouncementListener() {
    return speechAnnouncementListener;
  }

  @Nullable
  @Override
  public BannerInstructionsListener bannerInstructionsListener() {
    return bannerInstructionsListener;
  }

  @Nullable
  @Override
  public SpeechPlayer speechPlayer() {
    return speechPlayer;
  }

  @Nullable
  @Override
  public LocationEngine locationEngine() {
    return locationEngine;
  }

  @Override
  public String toString() {
    return "NavigationViewOptions{"
        + "directionsRoute=" + directionsRoute + ", "
        + "lightThemeResId=" + lightThemeResId + ", "
        + "darkThemeResId=" + darkThemeResId + ", "
        + "shouldSimulateRoute=" + shouldSimulateRoute + ", "
        + "waynameChipEnabled=" + waynameChipEnabled + ", "
        + "navigationOptions=" + navigationOptions + ", "
        + "routeListener=" + routeListener + ", "
        + "navigationListener=" + navigationListener + ", "
        + "progressChangeListener=" + progressChangeListener + ", "
        + "milestoneEventListener=" + milestoneEventListener + ", "
        + "milestones=" + milestones + ", "
        + "bottomSheetCallback=" + bottomSheetCallback + ", "
        + "instructionListListener=" + instructionListListener + ", "
        + "speechAnnouncementListener=" + speechAnnouncementListener + ", "
        + "bannerInstructionsListener=" + bannerInstructionsListener + ", "
        + "speechPlayer=" + speechPlayer + ", "
        + "locationEngine=" + locationEngine
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof NavigationViewOptions) {
      NavigationViewOptions that = (NavigationViewOptions) o;
      return this.directionsRoute.equals(that.directionsRoute())
          && (this.lightThemeResId == null ? that.lightThemeResId() == null : this.lightThemeResId.equals(that.lightThemeResId()))
          && (this.darkThemeResId == null ? that.darkThemeResId() == null : this.darkThemeResId.equals(that.darkThemeResId()))
          && this.shouldSimulateRoute == that.shouldSimulateRoute()
          && this.waynameChipEnabled == that.waynameChipEnabled()
          && this.navigationOptions.equals(that.navigationOptions())
          && (this.routeListener == null ? that.routeListener() == null : this.routeListener.equals(that.routeListener()))
          && (this.navigationListener == null ? that.navigationListener() == null : this.navigationListener.equals(that.navigationListener()))
          && (this.progressChangeListener == null ? that.progressChangeListener() == null : this.progressChangeListener.equals(that.progressChangeListener()))
          && (this.milestoneEventListener == null ? that.milestoneEventListener() == null : this.milestoneEventListener.equals(that.milestoneEventListener()))
          && (this.milestones == null ? that.milestones() == null : this.milestones.equals(that.milestones()))
          && (this.bottomSheetCallback == null ? that.bottomSheetCallback() == null : this.bottomSheetCallback.equals(that.bottomSheetCallback()))
          && (this.instructionListListener == null ? that.instructionListListener() == null : this.instructionListListener.equals(that.instructionListListener()))
          && (this.speechAnnouncementListener == null ? that.speechAnnouncementListener() == null : this.speechAnnouncementListener.equals(that.speechAnnouncementListener()))
          && (this.bannerInstructionsListener == null ? that.bannerInstructionsListener() == null : this.bannerInstructionsListener.equals(that.bannerInstructionsListener()))
          && (this.speechPlayer == null ? that.speechPlayer() == null : this.speechPlayer.equals(that.speechPlayer()))
          && (this.locationEngine == null ? that.locationEngine() == null : this.locationEngine.equals(that.locationEngine()));
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
    h$ ^= navigationOptions.hashCode();
    h$ *= 1000003;
    h$ ^= (routeListener == null) ? 0 : routeListener.hashCode();
    h$ *= 1000003;
    h$ ^= (navigationListener == null) ? 0 : navigationListener.hashCode();
    h$ *= 1000003;
    h$ ^= (progressChangeListener == null) ? 0 : progressChangeListener.hashCode();
    h$ *= 1000003;
    h$ ^= (milestoneEventListener == null) ? 0 : milestoneEventListener.hashCode();
    h$ *= 1000003;
    h$ ^= (milestones == null) ? 0 : milestones.hashCode();
    h$ *= 1000003;
    h$ ^= (bottomSheetCallback == null) ? 0 : bottomSheetCallback.hashCode();
    h$ *= 1000003;
    h$ ^= (instructionListListener == null) ? 0 : instructionListListener.hashCode();
    h$ *= 1000003;
    h$ ^= (speechAnnouncementListener == null) ? 0 : speechAnnouncementListener.hashCode();
    h$ *= 1000003;
    h$ ^= (bannerInstructionsListener == null) ? 0 : bannerInstructionsListener.hashCode();
    h$ *= 1000003;
    h$ ^= (speechPlayer == null) ? 0 : speechPlayer.hashCode();
    h$ *= 1000003;
    h$ ^= (locationEngine == null) ? 0 : locationEngine.hashCode();
    return h$;
  }

  static final class Builder extends NavigationViewOptions.Builder {
    private DirectionsRoute directionsRoute;
    private Integer lightThemeResId;
    private Integer darkThemeResId;
    private boolean shouldSimulateRoute;
    private boolean waynameChipEnabled;
    private MapLibreNavigationOptions navigationOptions;
    private RouteListener routeListener;
    private NavigationListener navigationListener;
    private ProgressChangeListener progressChangeListener;
    private MilestoneEventListener milestoneEventListener;
    private List<Milestone> milestones;
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
    private InstructionListListener instructionListListener;
    private SpeechAnnouncementListener speechAnnouncementListener;
    private BannerInstructionsListener bannerInstructionsListener;
    private SpeechPlayer speechPlayer;
    private LocationEngine locationEngine;
    private byte set$0;
    Builder() {
    }
    @Override
    public NavigationViewOptions.Builder directionsRoute(DirectionsRoute directionsRoute) {
      if (directionsRoute == null) {
        throw new NullPointerException("Null directionsRoute");
      }
      this.directionsRoute = directionsRoute;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder lightThemeResId(Integer lightThemeResId) {
      this.lightThemeResId = lightThemeResId;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder darkThemeResId(Integer darkThemeResId) {
      this.darkThemeResId = darkThemeResId;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder shouldSimulateRoute(boolean shouldSimulateRoute) {
      this.shouldSimulateRoute = shouldSimulateRoute;
      set$0 |= (byte) 1;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder waynameChipEnabled(boolean waynameChipEnabled) {
      this.waynameChipEnabled = waynameChipEnabled;
      set$0 |= (byte) 2;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder navigationOptions(MapLibreNavigationOptions navigationOptions) {
      if (navigationOptions == null) {
        throw new NullPointerException("Null navigationOptions");
      }
      this.navigationOptions = navigationOptions;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder routeListener(RouteListener routeListener) {
      this.routeListener = routeListener;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder navigationListener(NavigationListener navigationListener) {
      this.navigationListener = navigationListener;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder progressChangeListener(ProgressChangeListener progressChangeListener) {
      this.progressChangeListener = progressChangeListener;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder milestoneEventListener(MilestoneEventListener milestoneEventListener) {
      this.milestoneEventListener = milestoneEventListener;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder milestones(List<Milestone> milestones) {
      this.milestones = milestones;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder bottomSheetCallback(BottomSheetBehavior.BottomSheetCallback bottomSheetCallback) {
      this.bottomSheetCallback = bottomSheetCallback;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder instructionListListener(InstructionListListener instructionListListener) {
      this.instructionListListener = instructionListListener;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder speechAnnouncementListener(SpeechAnnouncementListener speechAnnouncementListener) {
      this.speechAnnouncementListener = speechAnnouncementListener;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder bannerInstructionsListener(BannerInstructionsListener bannerInstructionsListener) {
      this.bannerInstructionsListener = bannerInstructionsListener;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder speechPlayer(SpeechPlayer speechPlayer) {
      this.speechPlayer = speechPlayer;
      return this;
    }
    @Override
    public NavigationViewOptions.Builder locationEngine(LocationEngine locationEngine) {
      this.locationEngine = locationEngine;
      return this;
    }
    @Override
    public NavigationViewOptions build() {
      if (set$0 != 3
          || this.directionsRoute == null
          || this.navigationOptions == null) {
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
        if (this.navigationOptions == null) {
          missing.append(" navigationOptions");
        }
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_NavigationViewOptions(
          this.directionsRoute,
          this.lightThemeResId,
          this.darkThemeResId,
          this.shouldSimulateRoute,
          this.waynameChipEnabled,
          this.navigationOptions,
          this.routeListener,
          this.navigationListener,
          this.progressChangeListener,
          this.milestoneEventListener,
          this.milestones,
          this.bottomSheetCallback,
          this.instructionListListener,
          this.speechAnnouncementListener,
          this.bannerInstructionsListener,
          this.speechPlayer,
          this.locationEngine);
    }
  }

}
