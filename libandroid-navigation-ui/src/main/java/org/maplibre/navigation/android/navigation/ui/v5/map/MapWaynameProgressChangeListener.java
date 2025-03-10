package org.maplibre.navigation.android.navigation.ui.v5.map;

import android.location.Location;

import org.maplibre.navigation.android.navigation.v5.routeprogress.ProgressChangeListener;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

class MapWaynameProgressChangeListener implements ProgressChangeListener {

  private final MapWayName mapWayName;

  MapWaynameProgressChangeListener(MapWayName mapWayName) {
    this.mapWayName = mapWayName;
  }

  @Override
  public void onProgressChange(Location location, RouteProgress routeProgress) {
    mapWayName.updateProgress(location, routeProgress.getCurrentStepPoints());
  }
}
