package org.maplibre.navigation.android.navigation.ui.v5;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsWaypoint;
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions;
import org.maplibre.geojson.Point;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;

import org.junit.Test;
import org.maplibre.navigation.android.navigation.ui.v5.route.MapLibreRouteFetcher;
import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationRoute;
import org.maplibre.navigation.android.navigation.v5.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NavigationViewRouterTest extends BaseTest {

    private static final String DIRECTIONS_PRECISION_6 = "directions_v5_precision_6.json";

    @Test
    public void sanity() {
        ViewRouteListener routeEngineListener = mock(ViewRouteListener.class);
        NavigationViewRouter routeEngine = buildRouteEngine(routeEngineListener);

        assertNotNull(routeEngine);
    }

    @Test
    public void onExtractOptionsWithRoute_routeUpdateCallbackIsCalled() throws Exception {
        ViewRouteListener routeEngineListener = mock(ViewRouteListener.class);
        NavigationViewRouter routeEngine = buildRouteEngine(routeEngineListener);
        NavigationViewOptions options = buildNavigationViewOptionsWithRoute();
        DirectionsRoute directionsRoute = options.directionsRoute();

        routeEngine.extractRouteOptions(options);

        verify(routeEngineListener).onRouteUpdate(directionsRoute);
    }

    @Test
    public void onExtractOptionsWithRoute_destinationCallbackIsCalled() throws Exception {
        ViewRouteListener routeEngineListener = mock(ViewRouteListener.class);
        NavigationViewRouter routeEngine = buildRouteEngine(routeEngineListener);
        NavigationViewOptions options = buildNavigationViewOptionsWithRoute();
        Point destination = findDestinationPoint(options);

        routeEngine.extractRouteOptions(options);

        verify(routeEngineListener).onDestinationSet(destination);
    }

    @Test
    public void onRouteResponseReceived_routeUpdateCallbackIsCalled() throws Exception {
        ViewRouteListener routeEngineListener = mock(ViewRouteListener.class);
        NavigationViewRouter routeEngine = buildRouteEngine(routeEngineListener);
        DirectionsResponse response = buildDirectionsResponse();
        DirectionsRoute route = response.getRoutes().get(0);
        RouteProgress routeProgress = mock(RouteProgress.class);

        routeEngine.onResponseReceived(response, routeProgress);

        verify(routeEngineListener).onRouteUpdate(route);
    }

    @Test
    public void onErrorReceived_errorListenerIsTriggered() {
        ViewRouteListener routeEngineListener = mock(ViewRouteListener.class);
        NavigationViewRouter routeEngine = buildRouteEngine(routeEngineListener);
        Throwable throwable = mock(Throwable.class);
        when(throwable.getMessage()).thenReturn("error");

        routeEngine.onErrorReceived(throwable);

        verify(routeEngineListener).onRouteRequestError(eq("error"));
    }

    @Test
    public void findRouteFrom_fastConnectionGoesToOnline() {
        MapLibreRouteFetcher onlineRouter = mock(MapLibreRouteFetcher.class);
        NavigationRoute.Builder builder = mock(NavigationRoute.Builder.class);
        when(onlineRouter.buildRequest(any(Location.class), any(RouteProgress.class))).thenReturn(builder);
        ConnectivityStatusProvider status = mock(ConnectivityStatusProvider.class);
        when(status.isConnectedFast()).thenReturn(true);
        NavigationViewRouter router = new NavigationViewRouter(
            onlineRouter,
            status,
            mock(RouteComparator.class),
            mock(ViewRouteListener.class),
            mock(RouteCallStatus.class)
        );
        router.updateLocation(mock(Location.class));

        router.findRouteFrom(mock(RouteProgress.class));

        verify(onlineRouter).findRouteWith(builder);
    }


    @Test
    public void findRouteFrom_secondRequestIgnored() {
        MapLibreRouteFetcher onlineRouter = mock(MapLibreRouteFetcher.class);
        NavigationRoute.Builder builder = mock(NavigationRoute.Builder.class);
        when(onlineRouter.buildRequest(any(Location.class), any(RouteProgress.class))).thenReturn(builder);
        ConnectivityStatusProvider status = mock(ConnectivityStatusProvider.class);
        when(status.isConnectedFast()).thenReturn(false);
        NavigationViewRouter router = new NavigationViewRouter(
            onlineRouter,
            status,
            mock(RouteComparator.class),
            mock(ViewRouteListener.class),
            mock(RouteCallStatus.class)
        );
        router.updateLocation(mock(Location.class));

        router.findRouteFrom(mock(RouteProgress.class));
        router.findRouteFrom(mock(RouteProgress.class));
    }

    @Test
    public void onDestroy_clearsListeners() {
        MapLibreRouteFetcher onlineRouter = mock(MapLibreRouteFetcher.class);
        NavigationViewRouter router = new NavigationViewRouter(
            onlineRouter,
            mock(ConnectivityStatusProvider.class),
            mock(RouteComparator.class),
            mock(ViewRouteListener.class),
            mock(RouteCallStatus.class)
        );

        router.onDestroy();

        verify(onlineRouter).cancelRouteCall();
    }

    @Test
    public void onDestroy_cancelsOnlineRouteCall() {
        MapLibreRouteFetcher onlineRouter = mock(MapLibreRouteFetcher.class);
        NavigationViewRouter router = new NavigationViewRouter(
            onlineRouter,
            mock(ConnectivityStatusProvider.class),
            mock(RouteComparator.class),
            mock(ViewRouteListener.class),
            mock(RouteCallStatus.class)
        );

        router.onDestroy();

        verify(onlineRouter).clearListeners();
    }

    @NonNull
    private NavigationViewRouter buildRouteEngine(ViewRouteListener routeEngineListener) {
        return new NavigationViewRouter(mock(MapLibreRouteFetcher.class), mock(ConnectivityStatusProvider.class),
            routeEngineListener);
    }

    private NavigationViewOptions buildNavigationViewOptionsWithRoute() throws IOException {
        return NavigationViewOptions.builder()
            .directionsRoute(buildDirectionsRoute())
            .build();
    }

    private Point findDestinationPoint(NavigationViewOptions options) {
        List<Point> coordinates = options.directionsRoute().getRouteOptions().getCoordinates();
        return coordinates.get(coordinates.size() - 1);
    }

    private DirectionsRoute buildDirectionsRoute() throws IOException {
        String body = loadJsonFixture(DIRECTIONS_PRECISION_6);
        DirectionsResponse response = DirectionsResponse.fromJson(body);
        RouteOptions options = buildRouteOptionsWithCoordinates(response);
        return response.getRoutes().get(0).toBuilder().withRouteOptions(options).build();
    }

    private DirectionsResponse buildDirectionsResponse() throws IOException {
        String body = loadJsonFixture(DIRECTIONS_PRECISION_6);
        return DirectionsResponse.fromJson(body);
    }

    private RouteOptions buildRouteOptionsWithCoordinates(DirectionsResponse response) {
        List<Point> coordinates = new ArrayList<>();
        for (DirectionsWaypoint waypoint : response.getWaypoints()) {
            coordinates.add(waypoint.getLocation());
        }
        return new RouteOptions.Builder(
            Constants.BASE_API_URL,
            "user",
            "profile",
            coordinates,
            ACCESS_TOKEN,
            "uuid"
        )
            .withGeometries("mocked_geometries")
            .build();
    }
}
