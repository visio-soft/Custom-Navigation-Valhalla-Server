package org.maplibre.navigation.android.navigation.v5.location

import android.location.Location

open class LocationValidator(private val accuracyThreshold: Int) {
    private var lastValidLocation: Location? = null

    fun isValidUpdate(location: Location): Boolean {
        return checkLastValidLocation(location) || location.accuracy < accuracyThreshold
    }

    /**
     * On the first location update, the last valid location will be null.
     *
     *
     * So set the last valid location and return true.  On the next update, there
     * will be a last update to compare against.
     *
     * @param location new location update
     * @return true if last valid location null, false otherwise
     */
    private fun checkLastValidLocation(location: Location): Boolean {
        if (lastValidLocation == null) {
            lastValidLocation = location
            return true
        }
        return false
    }
}
