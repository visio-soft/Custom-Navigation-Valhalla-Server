package org.maplibre.navigation.android.navigation.v5.models

import kotlinx.serialization.Serializable

/**
 * Visual instruction information related to a particular [LegStep] useful for making UI
 * elements inside your application such as banners. To receive this information, your request must
 * <tt>MapboxDirections.Builder#bannerInstructions()</tt> have set to true.
 *
 * @since 3.0.0
 */
@Serializable
data class BannerInstructions(

    /**
     * Distance in meters from the beginning of the step at which the visual instruction should be
     * visible.
     *
     * @since 3.0.0
     */
    val distanceAlongGeometry: Double,

    /**
     * A plain text representation stored inside a [BannerText] object.
     *
     * @since 3.0.0
     */
    val primary: BannerText,

    /**
     * Ancillary visual information about the [LegStep].
     *
     * @since 3.0.0
     */
    val secondary: BannerText?,

    /**
     * Additional information that is included if we feel the driver needs a heads up about something.
     * Can include information about the next maneuver (the one after the upcoming one),
     * if the step is short - can be null, or can be lane information.
     * If we have lane information, that trumps information about the next maneuver.
     *
     * @since 3.2.0
     */
    val sub: BannerText?,

    /**
     * Optional image to display for an upcoming maneuver. Used to provide a visual
     * for complex junctions and maneuver. If the step is short the image should be displayed
     * for the duration of the step, otherwise it is shown as you approach the maneuver.
     *
     * @since 5.0.0
     */
    val view: BannerView?,
)
