package org.maplibre.navigation.android.navigation.v5.navigation.camera

import android.location.Location
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress

/**
 * This class holds all information related to a route and a user's progress along the route. It
 * also provides useful information (screen configuration and target distance) which can be used to
 * make additional configuration changes to the map's camera.
 *
 * @since 0.10.0
 */
data class RouteInformation(

    /**
     * The current route the user is navigating along. This value will update when reroutes occur
     * and it will be null if the [RouteInformation] is generated from an update to route
     * progress or from an orientation change.
     * @return current route
     * @since 0.10.0
     */
    val route: DirectionsRoute?,

    /**
     * The user's current location along the route. This value will update when orientation changes
     * occur as well as when progress along a route changes.
     * @return current location
     * @since 0.10.0
     */
    val location: Location?,

    /**
     * The user's current progress along the route.
     * @return current progress along the route.
     * @since 0.10.0
     */
    val routeProgress: RouteProgress?
)