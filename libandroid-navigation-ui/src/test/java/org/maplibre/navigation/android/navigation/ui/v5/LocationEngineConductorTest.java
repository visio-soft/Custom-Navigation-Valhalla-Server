package org.maplibre.navigation.android.navigation.ui.v5;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.annotation.NonNull;

import org.maplibre.android.location.engine.LocationEngine;
import org.maplibre.navigation.android.navigation.v5.location.replay.ReplayRouteLocationEngine;

import org.junit.Test;

public class LocationEngineConductorTest extends BaseTest {

  @Test
  public void sanity() {
    LocationEngineConductor locationEngineConductor = new LocationEngineConductor();

    assertNotNull(locationEngineConductor);
  }

  @Test
  public void onInitWithSimulation_replayEngineIsProvided() {
    LocationEngineConductor locationEngineConductor = new LocationEngineConductor();
    boolean simulateRouteEnabled = true;
    locationEngineConductor.initializeLocationEngine(createMockContext(), null, simulateRouteEnabled);

    LocationEngine locationEngine = locationEngineConductor.obtainLocationEngine();

    assertTrue(locationEngine instanceof ReplayRouteLocationEngine);
  }

  @Test
  public void onInitWithSimulationAndCustomEngine_customEngineIsProvided() {
    LocationEngineConductor locationEngineConductor = new LocationEngineConductor();
    LocationEngine customEngine = mock(LocationEngine.class);
    boolean simulateRouteEnabled = true;
    locationEngineConductor.initializeLocationEngine(createMockContext(), customEngine, simulateRouteEnabled);

    LocationEngine locationEngine = locationEngineConductor.obtainLocationEngine();

    assertEquals(customEngine, locationEngine);
  }

  @NonNull
  private Context createMockContext() {
    Context mockContext = mock(Context.class);
    LocationManager mockLocationManager = mock(LocationManager.class);
    when(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLocationManager);
    when(mockContext.getPackageManager()).thenReturn(mock(PackageManager.class));
    when(mockContext.getApplicationContext()).thenReturn(mock(Context.class));
    return mockContext;
  }
}
