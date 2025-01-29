package org.maplibre.navigation.android.navigation.ui.v5;

import static com.google.common.base.Charsets.UTF_8;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions;
import org.maplibre.geojson.Point;
import org.maplibre.navigation.android.navigation.v5.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


class TestRouteBuilder {

    private static final String DIRECTIONS_PRECISION_6 = "directions_v5_precision_6.json";
    private static final String ACCESS_TOKEN = "pk.XXX";

    String loadJsonFixture(String filename) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(filename);
        Scanner scanner = new Scanner(inputStream, UTF_8.name()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    DirectionsRoute buildTestDirectionsRoute(@Nullable String fixtureName) throws IOException {
        fixtureName = checkNullFixtureName(fixtureName);
        String body = loadJsonFixture(fixtureName);
        DirectionsResponse response = DirectionsResponse.fromJson(body);
        DirectionsRoute route = response.getRoutes().get(0);
        return buildRouteWithOptions(route);
    }

    private DirectionsRoute buildRouteWithOptions(DirectionsRoute route) throws IOException {
        List<Point> coordinates = new ArrayList<>();
        RouteOptions routeOptionsWithoutVoiceInstructions = new RouteOptions.Builder(
            Constants.BASE_API_URL,
            "user",
            "profile",
            coordinates,
            ACCESS_TOKEN,
            "uuid"
        )
            .withGeometries("mocked_geometries")
            .withVoiceInstructions(true)
            .withBannerInstructions(true)
            .withVoiceInstructions(true)
            .withBannerInstructions(true)
            .build();

        return route.toBuilder().withRouteOptions(routeOptionsWithoutVoiceInstructions).build();
    }

    @NonNull
    private String checkNullFixtureName(@Nullable String fixtureName) {
        if (fixtureName == null) {
            fixtureName = DIRECTIONS_PRECISION_6;
        }
        return fixtureName;
    }
}
