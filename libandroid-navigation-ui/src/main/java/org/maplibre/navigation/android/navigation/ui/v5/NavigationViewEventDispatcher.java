package org.maplibre.navigation.android.navigation.ui.v5;


import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;

import org.maplibre.navigation.android.navigation.ui.v5.listeners.BannerInstructionsListener;
import org.maplibre.navigation.android.navigation.ui.v5.listeners.InstructionListListener;
import org.maplibre.navigation.android.navigation.ui.v5.listeners.NavigationListener;
import org.maplibre.navigation.android.navigation.ui.v5.listeners.RouteListener;
import org.maplibre.navigation.android.navigation.ui.v5.listeners.SpeechAnnouncementListener;
import org.maplibre.navigation.android.navigation.ui.v5.voice.SpeechAnnouncement;
import org.maplibre.navigation.android.navigation.v5.models.BannerInstructions;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.geojson.Point;
import org.maplibre.navigation.android.navigation.v5.milestone.MilestoneEventListener;
import org.maplibre.navigation.android.navigation.v5.navigation.MapLibreNavigation;
import org.maplibre.navigation.android.navigation.v5.routeprogress.ProgressChangeListener;

/**
 * In charge of holding any {@link NavigationView} related listeners {@link NavigationListener},
 * {@link RouteListener} and firing listener events when
 * triggered by the {@link NavigationView}.
 */
class NavigationViewEventDispatcher {

  private ProgressChangeListener progressChangeListener;
  private MilestoneEventListener milestoneEventListener;
  private NavigationListener navigationListener;
  private RouteListener routeListener;
  private BottomSheetCallback bottomSheetCallback;
  private InstructionListListener instructionListListener;
  private SpeechAnnouncementListener speechAnnouncementListener;
  private BannerInstructionsListener bannerInstructionsListener;

  /**
   * Initializes the listeners in the dispatcher, as well as the listeners in the {@link MapLibreNavigation}
   *
   * @param navigationViewOptions that contains all listeners to attach
   */
  void initializeListeners(NavigationViewOptions navigationViewOptions, NavigationViewModel navigationViewModel) {
    assignNavigationListener(navigationViewOptions.navigationListener(), navigationViewModel);
    assignRouteListener(navigationViewOptions.routeListener());
    assignBottomSheetCallback(navigationViewOptions.bottomSheetCallback());
    MapLibreNavigation navigation = navigationViewModel.retrieveNavigation();
    assignProgressChangeListener(navigationViewOptions, navigation);
    assignMilestoneEventListener(navigationViewOptions, navigation);
    assignInstructionListListener(navigationViewOptions.instructionListListener());
    assignSpeechAnnouncementListener(navigationViewOptions.speechAnnouncementListener());
    assignBannerInstructionsListener(navigationViewOptions.bannerInstructionsListener());
  }

  void onDestroy(@Nullable MapLibreNavigation navigation) {
    if (navigation != null) {
      removeProgressChangeListener(navigation);
      removeMilestoneEventListener(navigation);
    }
  }

  void assignNavigationListener(@Nullable NavigationListener navigationListener,
                                NavigationViewModel navigationViewModel) {
    this.navigationListener = navigationListener;
    if (navigationListener != null && navigationViewModel.isRunning()) {
      navigationListener.onNavigationRunning();
    }
  }

  void assignRouteListener(@Nullable RouteListener routeListener) {
    this.routeListener = routeListener;
  }

  void assignBottomSheetCallback(@Nullable BottomSheetCallback bottomSheetCallback) {
    this.bottomSheetCallback = bottomSheetCallback;
  }

  void assignInstructionListListener(@Nullable InstructionListListener instructionListListener) {
    this.instructionListListener = instructionListListener;
  }

  void assignSpeechAnnouncementListener(@Nullable SpeechAnnouncementListener speechAnnouncementListener) {
    this.speechAnnouncementListener = speechAnnouncementListener;
  }

  void assignBannerInstructionsListener(@Nullable BannerInstructionsListener bannerInstructionsListener) {
    this.bannerInstructionsListener = bannerInstructionsListener;
  }


  void onNavigationFinished() {
    if (navigationListener != null) {
      navigationListener.onNavigationFinished();
    }
  }

  void onCancelNavigation() {
    if (navigationListener != null) {
      navigationListener.onCancelNavigation();
    }
  }

  void onNavigationRunning() {
    if (navigationListener != null) {
      navigationListener.onNavigationRunning();
    }
  }

  boolean allowRerouteFrom(Point point) {
    return routeListener == null || routeListener.allowRerouteFrom(point);
  }

  void onOffRoute(Point point) {
    if (routeListener != null) {
      routeListener.onOffRoute(point);
    }
  }

  void onRerouteAlong(DirectionsRoute directionsRoute) {
    if (routeListener != null) {
      routeListener.onRerouteAlong(directionsRoute);
    }
  }

  void onFailedReroute(String errorMessage) {
    if (routeListener != null) {
      routeListener.onFailedReroute(errorMessage);
    }
  }

  void onArrival() {
    if (routeListener != null) {
      routeListener.onArrival();
    }
  }

  void onBottomSheetStateChanged(View bottomSheet, int newState) {
    if (bottomSheetCallback != null) {
      bottomSheetCallback.onStateChanged(bottomSheet, newState);
    }
  }

  void onInstructionListVisibilityChanged(boolean shown) {
    if (instructionListListener != null) {
      instructionListListener.onInstructionListVisibilityChanged(shown);
    }
  }

  SpeechAnnouncement onAnnouncement(SpeechAnnouncement announcement) {
    if (speechAnnouncementListener != null) {
      return speechAnnouncementListener.willVoice(announcement);
    }
    return announcement;
  }

  BannerInstructions onBannerDisplay(BannerInstructions instructions) {
    if (bannerInstructionsListener != null) {
      return bannerInstructionsListener.willDisplay(instructions);
    }
    return instructions;
  }

  private void assignProgressChangeListener(NavigationViewOptions navigationViewOptions, MapLibreNavigation navigation) {
    this.progressChangeListener = navigationViewOptions.progressChangeListener();
    if (progressChangeListener != null) {
      navigation.addProgressChangeListener(progressChangeListener);
    }
  }

  private void assignMilestoneEventListener(NavigationViewOptions navigationViewOptions, MapLibreNavigation navigation) {
    this.milestoneEventListener = navigationViewOptions.milestoneEventListener();
    if (milestoneEventListener != null) {
      navigation.addMilestoneEventListener(milestoneEventListener);
    }
  }

  private void removeMilestoneEventListener(MapLibreNavigation navigation) {
    if (milestoneEventListener != null) {
      navigation.removeMilestoneEventListener(milestoneEventListener);
    }
  }

  private void removeProgressChangeListener(MapLibreNavigation navigation) {
    if (progressChangeListener != null) {
      navigation.removeProgressChangeListener(progressChangeListener);
    }
  }
}
