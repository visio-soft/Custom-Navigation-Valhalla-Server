package org.maplibre.navigation.android.navigation.ui.v5.route;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Handler;

import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;

import org.junit.Test;
import org.maplibre.navigation.android.navigation.ui.v5.route.OnPrimaryRouteUpdatedCallback;
import org.maplibre.navigation.android.navigation.ui.v5.route.PrimaryRouteUpdateTask;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

public class PrimaryRouteUpdateTaskTest {

  @SuppressWarnings("DoNotCall")
  @Test
  public void run_onPrimaryRouteUpdatedIsCalled() {
    int primaryRouteIndex = 0;
    FeatureCollection mockedFeatureCollection = mock(FeatureCollection.class);
    List<FeatureCollection> routeFeatureCollections = new ArrayList<>();
    List<Feature> mockedFeatures = new ArrayList<>();
    Feature mockedFeature = mock(Feature.class);
    mockedFeatures.add(mockedFeature);
    when(mockedFeatureCollection.features()).thenReturn(mockedFeatures);
    routeFeatureCollections.add(mockedFeatureCollection);
    OnPrimaryRouteUpdatedCallback callback = mock(OnPrimaryRouteUpdatedCallback.class);
    Handler mockedHandler = mock(Handler.class);
    ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
    PrimaryRouteUpdateTask task = new PrimaryRouteUpdateTask(primaryRouteIndex, routeFeatureCollections, callback, mockedHandler);

    task.run();

    verify(mockedHandler).post(runnable.capture());
    runnable.getValue().run();
    verify(callback).onPrimaryRouteUpdated(eq(routeFeatureCollections));
  }
}