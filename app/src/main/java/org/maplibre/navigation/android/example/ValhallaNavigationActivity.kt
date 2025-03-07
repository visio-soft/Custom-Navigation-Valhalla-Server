package org.maplibre.navigation.android.example
import kotlin.system.exitProcess

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.example.databinding.ActivityNavigationUiBinding
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncher
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncherOptions
import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationMapRoute
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale


class ValhallaNavigationActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    MapLibreMap.OnMapClickListener {
    private lateinit var mapLibreMap: MapLibreMap

    // Navigation related variables
    private var language = Locale.getDefault().language
    private var route: DirectionsRoute? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var destination: Point? = null
    private var locationComponent: LocationComponent? = null

    private lateinit var binding: ActivityNavigationUiBinding

    private var simulateRoute = false
    private val mbtilesFileName = "map.mbtiles"


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        binding = ActivityNavigationUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mbtilesFile = copyMbtilesFromAssets(this, mbtilesFileName)

        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@ValhallaNavigationActivity)
        }

        binding.startRouteButton.setOnClickListener {
            route?.let { route ->
                val userLocation = mapLibreMap.locationComponent.lastKnownLocation ?: return@let

                val options = NavigationLauncherOptions.builder()
                    .directionsRoute(route)
                    .shouldSimulateRoute(simulateRoute)
                    .initialMapCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(58.3669087,23.5740837,)).build()
                    )
                    .lightThemeResId(R.style.TestNavigationViewLight)
                    .darkThemeResId(R.style.TestNavigationViewDark)
                    .build()
                NavigationLauncher.startNavigation(this@ValhallaNavigationActivity, options)
            }
        }

        binding.simulateRouteSwitch.setOnCheckedChangeListener { _, checked ->
            simulateRoute = checked
        }

        binding.clearPoints.setOnClickListener {
            if (::mapLibreMap.isInitialized) {
                mapLibreMap.markers.forEach {
                    mapLibreMap.removeMarker(it)
                }
            }
            destination = null
            it.visibility = View.GONE
            binding.startRouteLayout.visibility = View.GONE

            navigationMapRoute?.removeRoute()
        }
    }

    private fun moveRouteLayerToTop(style: Style) {
        val routeLayerId = "mapbox-navigation-route-layer" // Adjust this if needed
        val backgroundLayerId = "omman-raster-layer" // Change based on your style

        val routeLayer = style.getLayer(routeLayerId)
        val backgroundLayer = style.getLayer(backgroundLayerId)

        if (routeLayer != null && backgroundLayer != null) {
            style.removeLayer(routeLayer)
            style.addLayerAbove(routeLayer, backgroundLayerId)
        } else {
        }
    }


    override fun onMapReady(mapLibreMap: MapLibreMap) {
        this.mapLibreMap = mapLibreMap

        val mbtilesUri = "file://${getFileStreamPath(mbtilesFileName).absolutePath}"

        val styleJson = """
        {
            "id": "43f36e14-e3f5-43c1-84c0-50a9c80dc5c7",
            "name": "MapLibre",
            "zoom": 0.8619833357855968,
            "pitch": 0,
            "center": [
                17.65431710431244,
                32.954120326746775
            ],
            "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
            "layers": [
                {
                    "id": "background",
                    "type": "background",
                    "paint": {
                        "background-color": "#D8F2FF"
                    },
                    "filter": [
                        "all"
                    ],
                    "layout": {
                        "visibility": "visible"
                    },
                    "maxzoom": 24
                },
                {
                    "id": "coastline",
                    "type": "line",
                    "paint": {
                        "line-blur": 0.5,
                        "line-color": "#198EC8",
                        "line-width": {
                            "stops": [
                                [
                                    0,
                                    2
                                ],
                                [
                                    6,
                                    6
                                ],
                                [
                                    14,
                                    9
                                ],
                                [
                                    22,
                                    18
                                ]
                            ]
                        }
                    },
                    "filter": [
                        "all"
                    ],
                    "layout": {
                        "line-cap": "round",
                        "line-join": "round",
                        "visibility": "visible"
                    },
                    "source": "maplibre",
                    "maxzoom": 24,
                    "minzoom": 0,
                    "source-layer": "countries"
                },
                {
                    "id": "countries-fill",
                    "type": "fill",
                    "paint": {
                        "fill-color": [
                            "match",
                            [
                                "get",
                                "ADM0_A3"
                            ],
                            [
                                "ARM",
                                "ATG",
                                "AUS",
                                "BTN",
                                "CAN",
                                "COG",
                                "CZE",
                                "GHA",
                                "GIN",
                                "HTI",
                                "ISL",
                                "JOR",
                                "KHM",
                                "KOR",
                                "LVA",
                                "MLT",
                                "MNE",
                                "MOZ",
                                "PER",
                                "SAH",
                                "SGP",
                                "SLV",
                                "SOM",
                                "TJK",
                                "TUV",
                                "UKR",
                                "WSM"
                            ],
                            "#D6C7FF",
                            [
                                "AZE",
                                "BGD",
                                "CHL",
                                "CMR",
                                "CSI",
                                "DEU",
                                "DJI",
                                "GUY",
                                "HUN",
                                "IOA",
                                "JAM",
                                "LBN",
                                "LBY",
                                "LSO",
                                "MDG",
                                "MKD",
                                "MNG",
                                "MRT",
                                "NIU",
                                "NZL",
                                "PCN",
                                "PYF",
                                "SAU",
                                "SHN",
                                "STP",
                                "TTO",
                                "UGA",
                                "UZB",
                                "ZMB"
                            ],
                            "#EBCA8A",
                            [
                                "AGO",
                                "ASM",
                                "ATF",
                                "BDI",
                                "BFA",
                                "BGR",
                                "BLZ",
                                "BRA",
                                "CHN",
                                "CRI",
                                "ESP",
                                "HKG",
                                "HRV",
                                "IDN",
                                "IRN",
                                "ISR",
                                "KNA",
                                "LBR",
                                "LCA",
                                "MAC",
                                "MUS",
                                "NOR",
                                "PLW",
                                "POL",
                                "PRI",
                                "SDN",
                                "TUN",
                                "UMI",
                                "USA",
                                "USG",
                                "VIR",
                                "VUT"
                            ],
                            "#C1E599",
                            [
                                "ARE",
                                "ARG",
                                "BHS",
                                "CIV",
                                "CLP",
                                "DMA",
                                "ETH",
                                "GAB",
                                "GRD",
                                "HMD",
                                "IND",
                                "IOT",
                                "IRL",
                                "IRQ",
                                "ITA",
                                "KOS",
                                "LUX",
                                "MEX",
                                "NAM",
                                "NER",
                                "PHL",
                                "PRT",
                                "RUS",
                                "SEN",
                                "SUR",
                                "TZA",
                                "VAT"
                            ],
                            "#E7E58F",
                            [
                                "AUT",
                                "BEL",
                                "BHR",
                                "BMU",
                                "BRB",
                                "CYN",
                                "DZA",
                                "EST",
                                "FLK",
                                "GMB",
                                "GUM",
                                "HND",
                                "JEY",
                                "KGZ",
                                "LIE",
                                "MAF",
                                "MDA",
                                "NGA",
                                "NRU",
                                "SLB",
                                "SOL",
                                "SRB",
                                "SWZ",
                                "THA",
                                "TUR",
                                "VEN",
                                "VGB"
                            ],
                            "#98DDA1",
                            [
                                "AIA",
                                "BIH",
                                "BLM",
                                "BRN",
                                "CAF",
                                "CHE",
                                "COM",
                                "CPV",
                                "CUB",
                                "ECU",
                                "ESB",
                                "FSM",
                                "GAZ",
                                "GBR",
                                "GEO",
                                "KEN",
                                "LTU",
                                "MAR",
                                "MCO",
                                "MDV",
                                "NFK",
                                "NPL",
                                "PNG",
                                "PRY",
                                "QAT",
                                "SLE",
                                "SPM",
                                "SYC",
                                "TCA",
                                "TKM",
                                "TLS",
                                "VNM",
                                "WEB",
                                "WSB",
                                "YEM",
                                "ZWE"
                            ],
                            "#83D5F4",
                            [
                                "ABW",
                                "ALB",
                                "AND",
                                "ATC",
                                "BOL",
                                "COD",
                                "CUW",
                                "CYM",
                                "CYP",
                                "EGY",
                                "FJI",
                                "GGY",
                                "IMN",
                                "KAB",
                                "KAZ",
                                "KWT",
                                "LAO",
                                "MLI",
                                "MNP",
                                "MSR",
                                "MYS",
                                "NIC",
                                "NLD",
                                "PAK",
                                "PAN",
                                "PRK",
                                "ROU",
                                "SGS",
                                "SVN",
                                "SWE",
                                "TGO",
                                "TWN",
                                "VCT",
                                "ZAF"
                            ],
                            "#B1BBF9",
                            [
                                "ATA",
                                "GRL"
                            ],
                            "#FFFFFF",
                            "#EAB38F"
                        ]
                    },
                    "filter": [
                        "all"
                    ],
                    "layout": {
                        "visibility": "visible"
                    },
                    "source": "maplibre",
                    "maxzoom": 24,
                    "source-layer": "countries"
                },
                {
                    "id": "countries-boundary",
                    "type": "line",
                    "paint": {
                        "line-color": "rgba(255, 255, 255, 1)",
                        "line-width": {
                            "stops": [
                                [
                                    1,
                                    1
                                ],
                                [
                                    6,
                                    2
                                ],
                                [
                                    14,
                                    6
                                ],
                                [
                                    22,
                                    12
                                ]
                            ]
                        },
                        "line-opacity": {
                            "stops": [
                                [
                                    3,
                                    0.5
                                ],
                                [
                                    6,
                                    1
                                ]
                            ]
                        }
                    },
                    "layout": {
                        "line-cap": "round",
                        "line-join": "round",
                        "visibility": "visible"
                    },
                    "source": "maplibre",
                    "maxzoom": 24,
                    "source-layer": "countries"
                },
                {
                    "id": "geolines",
                    "type": "line",
                    "paint": {
                        "line-color": "#1077B0",
                        "line-opacity": 1,
                        "line-dasharray": [
                            3,
                            3
                        ]
                    },
                    "filter": [
                        "all",
                        [
                            "!=",
                            "name",
                            "International Date Line"
                        ]
                    ],
                    "layout": {
                        "visibility": "visible"
                    },
                    "source": "maplibre",
                    "maxzoom": 24,
                    "source-layer": "geolines"
                },
                {
                    "id": "geolines-label",
                    "type": "symbol",
                    "paint": {
                        "text-color": "#1077B0",
                        "text-halo-blur": 1,
                        "text-halo-color": "rgba(255, 255, 255, 1)",
                        "text-halo-width": 1
                    },
                    "filter": [
                        "all",
                        [
                            "!=",
                            "name",
                            "International Date Line"
                        ]
                    ],
                    "layout": {
                        "text-font": [
                            "Open Sans Semibold"
                        ],
                        "text-size": {
                            "stops": [
                                [
                                    2,
                                    12
                                ],
                                [
                                    6,
                                    16
                                ]
                            ]
                        },
                        "text-field": "{name}",
                        "visibility": "visible",
                        "symbol-placement": "line"
                    },
                    "source": "maplibre",
                    "maxzoom": 24,
                    "minzoom": 1,
                    "source-layer": "geolines"
                },
                {
                    "id": "countries-label",
                    "type": "symbol",
                    "paint": {
                        "text-color": "rgba(8, 37, 77, 1)",
                        "text-halo-blur": {
                            "stops": [
                                [
                                    2,
                                    0.2
                                ],
                                [
                                    6,
                                    0
                                ]
                            ]
                        },
                        "text-halo-color": "rgba(255, 255, 255, 1)",
                        "text-halo-width": {
                            "stops": [
                                [
                                    2,
                                    1
                                ],
                                [
                                    6,
                                    1.6
                                ]
                            ]
                        }
                    },
                    "filter": [
                        "all"
                    ],
                    "layout": {
                        "text-font": [
                            "Open Sans Semibold"
                        ],
                        "text-size": {
                            "stops": [
                                [
                                    2,
                                    10
                                ],
                                [
                                    4,
                                    12
                                ],
                                [
                                    6,
                                    16
                                ]
                            ]
                        },
                        "text-field": {
                            "stops": [
                                [
                                    2,
                                    "{ABBREV}"
                                ],
                                [
                                    4,
                                    "{NAME}"
                                ]
                            ]
                        },
                        "visibility": "visible",
                        "text-max-width": 10,
                        "text-transform": {
                            "stops": [
                                [
                                    0,
                                    "uppercase"
                                ],
                                [
                                    2,
                                    "none"
                                ]
                            ]
                        }
                    },
                    "source": "maplibre",
                    "maxzoom": 24,
                    "minzoom": 2,
                    "source-layer": "centroids"
                },
                {
                    "id": "crimea-fill",
                    "type": "fill",
                    "source": "crimea",
                    "paint": {
                        "fill-color": "#D6C7FF"
                    }
                }
            ],
            "bearing": 0,
            "sources": {
                "maplibre": {
                    "url": "https://demotiles.maplibre.org/tiles/tiles.json",
                    "type": "vector"
                },
                "crimea": {
                    "type": "geojson",
                    "data": {
                        "type": "Feature",
                        "geometry": {
                            "type": "Polygon",
                            "coordinates": [
                                [
                                    [
                                        34.00905273547181,
                                        46.55925987559425
                                    ],
                                    [
                                        33.64325260204026,
                                        46.34533545368038
                                    ],
                                    [
                                        33.628682598560204,
                                        46.12569762665683
                                    ],
                                    [
                                        33.64292861730951,
                                        46.10476396128129
                                    ],
                                    [
                                        33.648473474905984,
                                        46.09033047763651
                                    ],
                                    [
                                        33.63876482059936,
                                        46.077976784785335
                                    ],
                                    [
                                        33.62782672238245,
                                        46.06747935719011
                                    ],
                                    [
                                        33.62911357645072,
                                        46.05708111413949
                                    ],
                                    [
                                        33.642686868727424,
                                        46.02192963417187
                                    ],
                                    [
                                        33.6429723910654,
                                        46.01521185644708
                                    ],
                                    [
                                        33.636224138774026,
                                        46.006705833212465
                                    ],
                                    [
                                        33.63052626465907,
                                        45.99692992186792
                                    ],
                                    [
                                        33.63193836679693,
                                        45.988472992911284
                                    ],
                                    [
                                        33.64276684834442,
                                        45.984575360297384
                                    ],
                                    [
                                        33.646928693041986,
                                        45.97981936210982
                                    ],
                                    [
                                        33.638745893564305,
                                        45.96829769147004
                                    ],
                                    [
                                        33.61958133326394,
                                        45.951176418494185
                                    ],
                                    [
                                        33.63181380398527,
                                        45.9445404758078
                                    ],
                                    [
                                        33.638921676216,
                                        45.94737012930554
                                    ],
                                    [
                                        33.64561542516918,
                                        45.95403251372139
                                    ],
                                    [
                                        33.65666403976448,
                                        45.95687114427736
                                    ],
                                    [
                                        33.6825817382811,
                                        45.95878100879199
                                    ],
                                    [
                                        33.738791807037614,
                                        45.94836945227263
                                    ],
                                    [
                                        33.758180142697,
                                        45.94072970008301
                                    ],
                                    [
                                        33.77735917288169,
                                        45.92923970233858
                                    ],
                                    [
                                        33.75927796793485,
                                        45.92241179584471
                                    ],
                                    [
                                        33.72529865009221,
                                        45.91587363154565
                                    ],
                                    [
                                        33.70875012326826,
                                        45.91008760988058
                                    ],
                                    [
                                        33.69378857293381,
                                        45.91480850795665
                                    ],
                                    [
                                        33.69092650243843,
                                        45.89657370898402
                                    ],
                                    [
                                        33.693592356906805,
                                        45.87271465766318
                                    ],
                                    [
                                        33.69226765972388,
                                        45.86041392418218
                                    ],
                                    [
                                        33.6704813511748,
                                        45.8584273836251
                                    ],
                                    [
                                        33.65936345808916,
                                        45.85944682601249
                                    ],
                                    [
                                        33.653870582376726,
                                        45.86425922279372
                                    ],
                                    [
                                        33.65107345584843,
                                        45.87089907254003
                                    ],
                                    [
                                        33.63067378180233,
                                        45.88040685247182
                                    ],
                                    [
                                        33.61945300059696,
                                        45.88147266102649
                                    ],
                                    [
                                        33.60987421595539,
                                        45.88048951126686
                                    ],
                                    [
                                        33.59906957603934,
                                        45.877610457390375
                                    ],
                                    [
                                        33.57828877687868,
                                        45.86810261756233
                                    ],
                                    [
                                        33.55357394560386,
                                        45.84700625141778
                                    ],
                                    [
                                        33.530220674480375,
                                        45.84221983655459
                                    ],
                                    [
                                        33.5192297395441,
                                        45.84121682367507
                                    ],
                                    [
                                        33.50832088442496,
                                        45.84313067048083
                                    ],
                                    [
                                        33.48901101848409,
                                        45.85268298292175
                                    ],
                                    [
                                        33.482152996405716,
                                        45.854578171799005
                                    ],
                                    [
                                        33.46719955896293,
                                        45.849912739405056
                                    ],
                                    [
                                        33.42447496599681,
                                        45.83075886348303
                                    ],
                                    [
                                        33.40940172404095,
                                        45.82691953557702
                                    ],
                                    [
                                        33.37918350072067,
                                        45.802867525073566
                                    ],
                                    [
                                        33.37362145339398,
                                        45.79619281922518
                                    ],
                                    [
                                        33.33805543634864,
                                        45.78577808972071
                                    ],
                                    [
                                        33.26498872665803,
                                        45.75410774187094
                                    ],
                                    [
                                        33.22887541283427,
                                        45.75131270772724
                                    ],
                                    [
                                        33.19548267281132,
                                        45.7644887297206
                                    ],
                                    [
                                        33.1789202379222,
                                        45.78010311364778
                                    ],
                                    [
                                        33.1688456078636,
                                        45.78470227904205
                                    ],
                                    [
                                        33.161012432811674,
                                        45.77921593899549
                                    ],
                                    [
                                        33.15951585299757,
                                        45.76864464913777
                                    ],
                                    [
                                        33.165962301438725,
                                        45.762685940125465
                                    ],
                                    [
                                        33.1750888126426,
                                        45.759218220695715
                                    ],
                                    [
                                        33.181464829753,
                                        45.75490447884948
                                    ],
                                    [
                                        33.17613930782352,
                                        45.7437961960276
                                    ],
                                    [
                                        33.16369168844906,
                                        45.735912015025065
                                    ],
                                    [
                                        32.93692665480876,
                                        45.662114646778264
                                    ],
                                    [
                                        32.86839112407645,
                                        45.63044340698664
                                    ],
                                    [
                                        32.83803944575723,
                                        45.60834075026611
                                    ],
                                    [
                                        32.82702772424804,
                                        45.59576101516498
                                    ],
                                    [
                                        32.82433467080986,
                                        45.58705137380335
                                    ],
                                    [
                                        32.82563941622885,
                                        45.579605763895614
                                    ],
                                    [
                                        32.82993674258438,
                                        45.56978311819469
                                    ],
                                    [
                                        32.82851940940563,
                                        45.56227808675749
                                    ],
                                    [
                                        32.813310142795274,
                                        45.55930933158257
                                    ],
                                    [
                                        32.80213583657516,
                                        45.560145780074464
                                    ],
                                    [
                                        32.78258622159436,
                                        45.565158335073846
                                    ],
                                    [
                                        32.77333922465823,
                                        45.56689313356526
                                    ],
                                    [
                                        32.758306734735356,
                                        45.565030173463356
                                    ],
                                    [
                                        32.750177256846115,
                                        45.55943726334968
                                    ],
                                    [
                                        32.74340732630185,
                                        45.55261895849793
                                    ],
                                    [
                                        32.73524549539499,
                                        45.54598788110354
                                    ],
                                    [
                                        32.72031700779701,
                                        45.53735927760957
                                    ],
                                    [
                                        32.70536040418847,
                                        45.53169142131733
                                    ],
                                    [
                                        32.68589438933773,
                                        45.52663379187257
                                    ],
                                    [
                                        32.66370583186284,
                                        45.52563107058867
                                    ],
                                    [
                                        32.64312077736798,
                                        45.52188979044979
                                    ],
                                    [
                                        32.525284074162556,
                                        45.45838108691365
                                    ],
                                    [
                                        32.49490411219156,
                                        45.43524910229854
                                    ],
                                    [
                                        32.48107654411925,
                                        45.408986638827514
                                    ],
                                    [
                                        32.48514589713025,
                                        45.39458067125969
                                    ],
                                    [
                                        32.51256939517424,
                                        45.34060655033625
                                    ],
                                    [
                                        32.535915460470335,
                                        45.33777248012882
                                    ],
                                    [
                                        32.57027153843481,
                                        45.32510892683359
                                    ],
                                    [
                                        32.590830644991826,
                                        45.32038723212662
                                    ],
                                    [
                                        32.66380378113439,
                                        45.320421746458976
                                    ],
                                    [
                                        32.67760722618917,
                                        45.32609231279554
                                    ],
                                    [
                                        32.71316246802607,
                                        45.353283572618125
                                    ],
                                    [
                                        32.72817188836078,
                                        45.36074681043402
                                    ],
                                    [
                                        32.750518060251466,
                                        45.36371725645313
                                    ],
                                    [
                                        32.89973931692998,
                                        45.35412322462227
                                    ],
                                    [
                                        32.941197846443885,
                                        45.34245505845169
                                    ],
                                    [
                                        32.97701667405008,
                                        45.32596743563991
                                    ],
                                    [
                                        33.04296090827762,
                                        45.2853982930032
                                    ],
                                    [
                                        33.05274355585479,
                                        45.28154273654923
                                    ],
                                    [
                                        33.06850284417635,
                                        45.27703461892352
                                    ],
                                    [
                                        33.07825272648239,
                                        45.272210805127315
                                    ],
                                    [
                                        33.089426322403455,
                                        45.25656353201492
                                    ],
                                    [
                                        33.09897492343546,
                                        45.247820101667884
                                    ],
                                    [
                                        33.12384611720435,
                                        45.238235755071685
                                    ],
                                    [
                                        33.15767197859745,
                                        45.20755227709648
                                    ],
                                    [
                                        33.172959979330074,
                                        45.19681657531794
                                    ],
                                    [
                                        33.21837666514142,
                                        45.187878368659824
                                    ],
                                    [
                                        33.24017433636709,
                                        45.180191106261134
                                    ],
                                    [
                                        33.248571989896675,
                                        45.16588271012458
                                    ],
                                    [
                                        33.259649216030766,
                                        45.155918961282026
                                    ],
                                    [
                                        33.28309785485047,
                                        45.16064860772312
                                    ],
                                    [
                                        33.31767999550894,
                                        45.17535522412791
                                    ],
                                    [
                                        33.35458473323109,
                                        45.18598673360148
                                    ],
                                    [
                                        33.39725661527919,
                                        45.18973663076909
                                    ],
                                    [
                                        33.41344561756824,
                                        45.18490731877088
                                    ],
                                    [
                                        33.468468576977216,
                                        45.149132412229676
                                    ],
                                    [
                                        33.537128652906205,
                                        45.11719769268973
                                    ],
                                    [
                                        33.56161328289443,
                                        45.094099022711475
                                    ],
                                    [
                                        33.57837628774928,
                                        45.053145935448015
                                    ],
                                    [
                                        33.58247744978442,
                                        45.027377243150454
                                    ],
                                    [
                                        33.5851414316958,
                                        45.01816461606674
                                    ],
                                    [
                                        33.6031021265521,
                                        44.993103583251695
                                    ],
                                    [
                                        33.605922209331794,
                                        44.986905272229734
                                    ],
                                    [
                                        33.60843524291815,
                                        44.97039962759274
                                    ],
                                    [
                                        33.61943161357851,
                                        44.93184946652454
                                    ],
                                    [
                                        33.619484500808824,
                                        44.90819321920554
                                    ],
                                    [
                                        33.61549738593425,
                                        44.88894092276257
                                    ],
                                    [
                                        33.608561183117274,
                                        44.871288478948514
                                    ],
                                    [
                                        33.59889474705494,
                                        44.859790298912856
                                    ],
                                    [
                                        33.55904244709464,
                                        44.850057575124595
                                    ],
                                    [
                                        33.54667558363471,
                                        44.83724531175508
                                    ],
                                    [
                                        33.53701832136994,
                                        44.81871953508235
                                    ],
                                    [
                                        33.5303157846202,
                                        44.798338017069625
                                    ],
                                    [
                                        33.5249116915937,
                                        44.78918633101301
                                    ],
                                    [
                                        33.51669091675143,
                                        44.784809980590666
                                    ],
                                    [
                                        33.524785531609865,
                                        44.77183212449111
                                    ],
                                    [
                                        33.5302902535075,
                                        44.75724515985675
                                    ],
                                    [
                                        33.53710734694323,
                                        44.73034290771247
                                    ],
                                    [
                                        33.54650992495621,
                                        44.70989226909535
                                    ],
                                    [
                                        33.5481286806762,
                                        44.699106546699085
                                    ],
                                    [
                                        33.543995566510915,
                                        44.68230506537358
                                    ],
                                    [
                                        33.53580273994743,
                                        44.6726082589706
                                    ],
                                    [
                                        33.52337411931097,
                                        44.661863083605255
                                    ],
                                    [
                                        33.515320778874354,
                                        44.6491266698327
                                    ],
                                    [
                                        33.516377841582795,
                                        44.63464990118433
                                    ],
                                    [
                                        33.52466971637648,
                                        44.62863961572572
                                    ],
                                    [
                                        33.557474298027785,
                                        44.62473000923737
                                    ],
                                    [
                                        33.5710648827386,
                                        44.620853511273225
                                    ],
                                    [
                                        33.55105839203679,
                                        44.61506440493406
                                    ],
                                    [
                                        33.499905706797676,
                                        44.61452599304897
                                    ],
                                    [
                                        33.48451102966331,
                                        44.60992438254493
                                    ],
                                    [
                                        33.47658499621011,
                                        44.60714391514574
                                    ],
                                    [
                                        33.46705078205747,
                                        44.60616254193252
                                    ],
                                    [
                                        33.44476599234898,
                                        44.607062134677875
                                    ],
                                    [
                                        33.4353466482458,
                                        44.60509936890821
                                    ],
                                    [
                                        33.413591053005575,
                                        44.593500212748125
                                    ],
                                    [
                                        33.40543527945235,
                                        44.59055535193136
                                    ],
                                    [
                                        33.37510958624222,
                                        44.58564691897425
                                    ],
                                    [
                                        33.37074452434078,
                                        44.58851022190515
                                    ],
                                    [
                                        33.372237834990756,
                                        44.576810695127364
                                    ],
                                    [
                                        33.37913003799301,
                                        44.56412673079859
                                    ],
                                    [
                                        33.48759131590526,
                                        44.51024086451031
                                    ],
                                    [
                                        33.50011215135888,
                                        44.50041002882833
                                    ],
                                    [
                                        33.517917009115365,
                                        44.49074142372788
                                    ],
                                    [
                                        33.53836387802215,
                                        44.49164280212756
                                    ],
                                    [
                                        33.56041892763031,
                                        44.4966411022441
                                    ],
                                    [
                                        33.57822378538677,
                                        44.497542389459795
                                    ],
                                    [
                                        33.59062975079095,
                                        44.48975808594983
                                    ],
                                    [
                                        33.619577003408466,
                                        44.46229988129974
                                    ],
                                    [
                                        33.62635433636015,
                                        44.45336293328907
                                    ],
                                    [
                                        33.63175322871038,
                                        44.434828756313124
                                    ],
                                    [
                                        33.645537634715026,
                                        44.42498521035591
                                    ],
                                    [
                                        33.721007257593925,
                                        44.39946630464436
                                    ],
                                    [
                                        33.74168386660085,
                                        44.39560878121904
                                    ],
                                    [
                                        33.80727466517129,
                                        44.39454176175843
                                    ],
                                    [
                                        33.81841706002561,
                                        44.39552670349164
                                    ],
                                    [
                                        33.83909366903248,
                                        44.40143600575672
                                    ],
                                    [
                                        33.85149963444792,
                                        44.40143600575945
                                    ],
                                    [
                                        33.91467816197718,
                                        44.38387049706651
                                    ],
                                    [
                                        33.938111652185,
                                        44.38083293528811
                                    ],
                                    [
                                        33.957065210440874,
                                        44.38272116790142
                                    ],
                                    [
                                        34.06614966692763,
                                        44.42019923628979
                                    ],
                                    [
                                        34.088893936836286,
                                        44.42200415824283
                                    ],
                                    [
                                        34.10279321289039,
                                        44.42487551014821
                                    ],
                                    [
                                        34.135933345669,
                                        44.44163597968952
                                    ],
                                    [
                                        34.14696087047267,
                                        44.44959070749778
                                    ],
                                    [
                                        34.16058918507403,
                                        44.466287285335795
                                    ],
                                    [
                                        34.170123399227776,
                                        44.48186111741296
                                    ],
                                    [
                                        34.182759104731986,
                                        44.49267838558103
                                    ],
                                    [
                                        34.22923417224524,
                                        44.49949719774551
                                    ],
                                    [
                                        34.24301857824986,
                                        44.50744404277697
                                    ],
                                    [
                                        34.263903954150294,
                                        44.53186886058606
                                    ],
                                    [
                                        34.26631622520165,
                                        44.53555362837611
                                    ],
                                    [
                                        34.26631622520165,
                                        44.54153064468656
                                    ],
                                    [
                                        34.27033667695244,
                                        44.545378535987936
                                    ],
                                    [
                                        34.2757355693048,
                                        44.54644280144541
                                    ],
                                    [
                                        34.285384653508004,
                                        44.54562413743594
                                    ],
                                    [
                                        34.299973149863405,
                                        44.54554227040174
                                    ],
                                    [
                                        34.32260254971496,
                                        44.543577427039224
                                    ],
                                    [
                                        34.3308731933177,
                                        44.54546040325087
                                    ],
                                    [
                                        34.340292537420794,
                                        44.55798473830754
                                    ],
                                    [
                                        34.38042135640006,
                                        44.631830317636684
                                    ],
                                    [
                                        34.41495238900856,
                                        44.673669777529994
                                    ],
                                    [
                                        34.424193090575585,
                                        44.68239452736094
                                    ],
                                    [
                                        34.42959198292681,
                                        44.68884644523774
                                    ],
                                    [
                                        34.469399167794535,
                                        44.730194532749294
                                    ],
                                    [
                                        34.47376422969597,
                                        44.73011292571252
                                    ],
                                    [
                                        34.47376422969597,
                                        44.72635887754967
                                    ],
                                    [
                                        34.475142670296464,
                                        44.723502373339585
                                    ],
                                    [
                                        34.499724861011515,
                                        44.74292382044041
                                    ],
                                    [
                                        34.532800295801195,
                                        44.752620844929055
                                    ],
                                    [
                                        34.61217550038418,
                                        44.76534519537847
                                    ],
                                    [
                                        34.65065696715081,
                                        44.777088262503725
                                    ],
                                    [
                                        34.72084256772871,
                                        44.811080759265764
                                    ],
                                    [
                                        34.756796893391225,
                                        44.82094054159748
                                    ],
                                    [
                                        34.82646979041766,
                                        44.81208604604609
                                    ],
                                    [
                                        34.84289620758207,
                                        44.816893835303176
                                    ],
                                    [
                                        34.856910353686715,
                                        44.82373813182468
                                    ],
                                    [
                                        34.889648317948144,
                                        44.817871641692506
                                    ],
                                    [
                                        34.90733830566026,
                                        44.820886440346584
                                    ],
                                    [
                                        34.922960632465504,
                                        44.83050015059965
                                    ],
                                    [
                                        34.92950822531711,
                                        44.83652826953224
                                    ],
                                    [
                                        34.94179932067178,
                                        44.84019370922482
                                    ],
                                    [
                                        34.95282684547897,
                                        44.841415470643284
                                    ],
                                    [
                                        34.98567967978991,
                                        44.840275160795755
                                    ],
                                    [
                                        35.0053224583441,
                                        44.83538786296728
                                    ],
                                    [
                                        35.017958163849414,
                                        44.82219008824552
                                    ],
                                    [
                                        35.02703289780189,
                                        44.80890779582285
                                    ],
                                    [
                                        35.037933245998005,
                                        44.79869792240089
                                    ],
                                    [
                                        35.08073333784134,
                                        44.793525442788905
                                    ],
                                    [
                                        35.1080207326404,
                                        44.824553365795765
                                    ],
                                    [
                                        35.130368105574235,
                                        44.86879838545747
                                    ],
                                    [
                                        35.15485200090768,
                                        44.90071251697748
                                    ],
                                    [
                                        35.17111229780758,
                                        44.90746386008772
                                    ],
                                    [
                                        35.21522068940149,
                                        44.91421441031795
                                    ],
                                    [
                                        35.233163085981715,
                                        44.925728224907715
                                    ],
                                    [
                                        35.25636688416236,
                                        44.95896657181197
                                    ],
                                    [
                                        35.27300098099195,
                                        44.96690119386028
                                    ],
                                    [
                                        35.29748487632534,
                                        44.95605693543271
                                    ],
                                    [
                                        35.30496087491386,
                                        44.96121482614441
                                    ],
                                    [
                                        35.315240372954605,
                                        44.965711070514175
                                    ],
                                    [
                                        35.31935217217088,
                                        44.96941359539801
                                    ],
                                    [
                                        35.36757236298112,
                                        44.94362319076086
                                    ],
                                    [
                                        35.36103086422793,
                                        44.97364475976596
                                    ],
                                    [
                                        35.362152264014156,
                                        44.98593980935419
                                    ],
                                    [
                                        35.374674561627444,
                                        44.997835734117416
                                    ],
                                    [
                                        35.389439658813274,
                                        45.00180049366759
                                    ],
                                    [
                                        35.42270785247763,
                                        45.00087540764923
                                    ],
                                    [
                                        35.43504325012745,
                                        45.00470780964241
                                    ],
                                    [
                                        35.43504325012745,
                                        45.011446929213974
                                    ],
                                    [
                                        35.40631957913584,
                                        45.02015821022701
                                    ],
                                    [
                                        35.40089948016896,
                                        45.025046135473445
                                    ],
                                    [
                                        35.39790908073891,
                                        45.03482073400548
                                    ],
                                    [
                                        35.40052568024015,
                                        45.042216617888045
                                    ],
                                    [
                                        35.40631957913584,
                                        45.051328088783805
                                    ],
                                    [
                                        35.40744097892215,
                                        45.06294640963205
                                    ],
                                    [
                                        35.41734667704213,
                                        45.0708666385693
                                    ],
                                    [
                                        35.469304867139925,
                                        45.10068964922732
                                    ],
                                    [
                                        35.5070260597534,
                                        45.113341616151644
                                    ],
                                    [
                                        35.54758335202416,
                                        45.12019982412133
                                    ],
                                    [
                                        35.59019654390909,
                                        45.11993606213795
                                    ],
                                    [
                                        35.63411803553862,
                                        45.11439677872579
                                    ],
                                    [
                                        35.70669729572677,
                                        45.09480210570922
                                    ],
                                    [
                                        35.771782422456766,
                                        45.06572995732262
                                    ],
                                    [
                                        35.78430472007,
                                        45.057941041321754
                                    ],
                                    [
                                        35.81250040352472,
                                        45.031852200991295
                                    ],
                                    [
                                        35.81941570220667,
                                        45.021152336906454
                                    ],
                                    [
                                        35.82763930064016,
                                        44.99895365027004
                                    ],
                                    [
                                        35.848198296721705,
                                        44.99208088455586
                                    ],
                                    [
                                        35.916977483614176,
                                        45.00172895661731
                                    ],
                                    [
                                        35.99360646900681,
                                        44.997896355361604
                                    ],
                                    [
                                        36.00893226608571,
                                        45.00926125333629
                                    ],
                                    [
                                        36.02539976723364,
                                        45.03288661039673
                                    ],
                                    [
                                        36.047827762958946,
                                        45.048074065419456
                                    ],
                                    [
                                        36.078666257082034,
                                        45.03883000769565
                                    ],
                                    [
                                        36.079137312377895,
                                        45.046610970582435
                                    ],
                                    [
                                        36.135020401727616,
                                        45.02125162210126
                                    ],
                                    [
                                        36.2241716847341,
                                        45.00751061631556
                                    ],
                                    [
                                        36.24398308095806,
                                        45.011474706353084
                                    ],
                                    [
                                        36.24828178013877,
                                        45.01649549321965
                                    ],
                                    [
                                        36.25332807917695,
                                        45.03247980324494
                                    ],
                                    [
                                        36.25743987839326,
                                        45.03842324279259
                                    ],
                                    [
                                        36.267158676549116,
                                        45.043573724415154
                                    ],
                                    [
                                        36.2783726744118,
                                        45.04555455542638
                                    ],
                                    [
                                        36.36740852558336,
                                        45.04833265291825
                                    ],
                                    [
                                        36.44029951169139,
                                        45.06787222615526
                                    ],
                                    [
                                        36.45375630913995,
                                        45.07631970334319
                                    ],
                                    [
                                        36.455251508854985,
                                        45.09202341204062
                                    ],
                                    [
                                        36.44142091149291,
                                        45.10709638287736
                                    ],
                                    [
                                        36.41432041665814,
                                        45.12872568311289
                                    ],
                                    [
                                        36.40852651776157,
                                        45.149160473330085
                                    ],
                                    [
                                        36.409997342308856,
                                        45.171615955386955
                                    ],
                                    [
                                        36.418312796420764,
                                        45.23001671705953
                                    ],
                                    [
                                        36.42672329481775,
                                        45.25186253492981
                                    ],
                                    [
                                        36.43756477765089,
                                        45.27227491599612
                                    ],
                                    [
                                        36.4497132753354,
                                        45.28542626329343
                                    ],
                                    [
                                        36.45905827355429,
                                        45.28753019598713
                                    ],
                                    [
                                        36.4814862692796,
                                        45.28845064200263
                                    ],
                                    [
                                        36.4909554290368,
                                        45.29213135137758
                                    ],
                                    [
                                        36.49637552800283,
                                        45.300940007322055
                                    ],
                                    [
                                        36.49394582846682,
                                        45.305015191082816
                                    ],
                                    [
                                        36.48871262946426,
                                        45.30935296803605
                                    ],
                                    [
                                        36.48460083024801,
                                        45.315924724862185
                                    ],
                                    [
                                        36.489647129296515,
                                        45.336413860372005
                                    ],
                                    [
                                        36.502169426909745,
                                        45.34731734941451
                                    ],
                                    [
                                        36.52104632331191,
                                        45.35033842661815
                                    ],
                                    [
                                        36.544281237819945,
                                        45.34731734942025
                                    ],
                                    [
                                        36.57455903204905,
                                        45.33601971904315
                                    ],
                                    [
                                        36.585399229982954,
                                        45.333917585593355
                                    ],
                                    [
                                        36.59810088537549,
                                        45.334837278577254
                                    ],
                                    [
                                        36.630808379142394,
                                        45.34048649352954
                                    ],
                                    [
                                        36.637536777859964,
                                        45.3511265071989
                                    ],
                                    [
                                        36.63099527910589,
                                        45.3741073632589
                                    ],
                                    [
                                        36.61359545390113,
                                        45.40895280985421
                                    ],
                                    [
                                        36.59845655678569,
                                        45.421547717459106
                                    ],
                                    [
                                        36.58331765967199,
                                        45.42731944465129
                                    ],
                                    [
                                        36.566309762912795,
                                        45.42548305000767
                                    ],
                                    [
                                        36.54836736633254,
                                        45.41210180010589
                                    ],
                                    [
                                        36.53285466928139,
                                        45.4090840212946
                                    ],
                                    [
                                        36.51565987255873,
                                        45.41957994832251
                                    ],
                                    [
                                        36.49117597722616,
                                        45.44279525429408
                                    ],
                                    [
                                        36.47043008117939,
                                        45.4458112314303
                                    ],
                                    [
                                        36.411182792482634,
                                        45.43610707766504
                                    ],
                                    [
                                        36.391371396258705,
                                        45.43991025572652
                                    ],
                                    [
                                        36.35959840231365,
                                        45.45407156049933
                                    ],
                                    [
                                        36.33960010612526,
                                        45.45695583486963
                                    ],
                                    [
                                        36.33025510790637,
                                        45.454464879327446
                                    ],
                                    [
                                        36.32053630976225,
                                        45.44856480887407
                                    ],
                                    [
                                        36.31156511147125,
                                        45.4438443081136
                                    ],
                                    [
                                        36.29885591389362,
                                        45.442795254299995
                                    ],
                                    [
                                        36.3072664122906,
                                        45.46115087970253
                                    ],
                                    [
                                        36.30016421364425,
                                        45.47320989503609
                                    ],
                                    [
                                        36.283717016779036,
                                        45.476355300848866
                                    ],
                                    [
                                        36.267082919949445,
                                        45.46704963343626
                                    ],
                                    [
                                        36.25213092279836,
                                        45.46115087970253
                                    ],
                                    [
                                        36.13681364478941,
                                        45.46219959214511
                                    ],
                                    [
                                        36.11700224855986,
                                        45.45721803432335
                                    ],
                                    [
                                        36.097003952371466,
                                        45.441483909606006
                                    ],
                                    [
                                        36.06952965760803,
                                        45.43046741078453
                                    ],
                                    [
                                        36.0655449627526,
                                        45.42553028973455
                                    ],
                                    [
                                        36.05134056545904,
                                        45.39535242162091
                                    ],
                                    [
                                        36.022557970944945,
                                        45.368441166003805
                                    ],
                                    [
                                        35.986486277818386,
                                        45.362926059418186
                                    ],
                                    [
                                        35.94723728529826,
                                        45.372380198658874
                                    ],
                                    [
                                        35.87220216002379,
                                        45.404075760536614
                                    ],
                                    [
                                        35.85388596351393,
                                        45.413916621802144
                                    ],
                                    [
                                        35.84715756479628,
                                        45.426379251448395
                                    ],
                                    [
                                        35.8524047739447,
                                        45.44386497541683
                                    ],
                                    [
                                        35.85950697259193,
                                        45.45933624762881
                                    ],
                                    [
                                        35.857824872912545,
                                        45.469953901705
                                    ],
                                    [
                                        35.83278027768503,
                                        45.47087138287168
                                    ],
                                    [
                                        35.8167068807486,
                                        45.46392436820739
                                    ],
                                    [
                                        35.80362388324218,
                                        45.44963442058864
                                    ],
                                    [
                                        35.79469305616038,
                                        45.42980210462429
                                    ],
                                    [
                                        35.791889556694684,
                                        45.41209230278156
                                    ],
                                    [
                                        35.772265060435046,
                                        45.39214572935421
                                    ],
                                    [
                                        35.767405661361295,
                                        45.38873311015669
                                    ],
                                    [
                                        35.75189296431793,
                                        45.386632934388984
                                    ],
                                    [
                                        35.7481549650407,
                                        45.379938103368545
                                    ],
                                    [
                                        35.746846665290036,
                                        45.369960021421576
                                    ],
                                    [
                                        35.74423006578874,
                                        45.36076812520648
                                    ],
                                    [
                                        35.71619507113218,
                                        45.34040932557082
                                    ],
                                    [
                                        35.69451467527287,
                                        45.32989869277279
                                    ],
                                    [
                                        35.51720627467216,
                                        45.29506847418358
                                    ],
                                    [
                                        35.48038698168983,
                                        45.2979608697527
                                    ],
                                    [
                                        35.33194061536096,
                                        45.371562726652314
                                    ],
                                    [
                                        35.04491375777232,
                                        45.669545248704424
                                    ],
                                    [
                                        35.00230056589345,
                                        45.7290693869553
                                    ],
                                    [
                                        34.70631294999043,
                                        46.024929846739866
                                    ],
                                    [
                                        34.35868883309806,
                                        46.106725558140795
                                    ],
                                    [
                                        34.00905273547181,
                                        46.55925987559425
                                    ]
                                ]
                            ]
                        }
                    }
                }
            },
            "version": 8,
            "metadata": {
                "maptiler:copyright": "This style was generated on MapTiler Cloud. Usage is governed by the license terms in https://github.com/maplibre/demotiles/blob/gh-pages/LICENSE",
                "openmaptiles:version": "3.x"
            }
        }
        """.trimIndent()

        // Load map style from server
        //val styleUrl = getString(R.string.map_style_light)

        mapLibreMap.setStyle(
            Style.Builder().fromJson(styleJson)
        ) { style ->
            enableLocationComponent(style)
            navigationMapRoute = NavigationMapRoute(binding.mapView, mapLibreMap)

            // Ensure navigation route appears above raster layers
            moveRouteLayerToTop(style)

            // Add map click listener
            mapLibreMap.addOnMapClickListener(this)

            // Set initial camera position

          //  val targetLocation = LatLng(28.8103,  41.06978)
            //val zoomLevel = 12.0

            /*mapLibreMap.cameraPosition = CameraPosition.Builder()
                .target(targetLocation)
                .zoom(zoomLevel)
                .build()*/

            Snackbar.make(
                findViewById(R.id.container),
                "Tap map to place destination",
                Snackbar.LENGTH_LONG,
            ).show()
        }
    }

    private fun copyMbtilesFromAssets(context: Context, assetName: String): File {
        val file = File(context.filesDir, assetName)
        if (!file.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        // Get an instance of the component
        locationComponent = mapLibreMap.locationComponent

        locationComponent?.let {
            // Activate with a built LocationComponentActivationOptions object
            it.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, style).build(),
            )

            // Enable to make component visible
            it.isLocationComponentEnabled = true

            // Set the component's camera mode
            it.cameraMode = CameraMode.TRACKING_GPS_NORTH

            // Set the component's render mode
            it.renderMode = RenderMode.NORMAL
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        destination = Point.fromLngLat(point.longitude, point.latitude)

        mapLibreMap.addMarker(MarkerOptions().position(point))

        binding.clearPoints.visibility = View.VISIBLE
        calculateRoute()
        return true
    }

    private fun calculateRoute() {
        binding.startRouteLayout.visibility = View.GONE
        val userLocation = mapLibreMap.locationComponent.lastKnownLocation
        val destination = destination
//        if (userLocation == null) {
//            Timber.d("calculateRoute: User location is null, therefore, origin can't be set.")
//            return
//        }

        if (destination == null) {
            Timber.d("calculateRoute: destination is null, therefore, destination can't be set.")
            return
        }

        val origin = Point.fromLngLat(58.11519054,23.65461481)
        if (TurfMeasurement.distance(origin, destination, TurfConstants.UNIT_METERS) < 50) {
            Timber.d("calculateRoute: distance < 50 m")
            binding.startRouteButton.visibility = View.GONE
            return
        }

        // The full Valhalla API is documented here:
        // https://valhalla.github.io/valhalla/api/turn-by-turn/api-reference/

        // It would be better if there was a proper ValhallaService which uses retrofit to
        // generate the API call similar to the DirectionsService for the Mapbox API:
        // https://github.com/mapbox/mapbox-java/blob/main/services-directions/src/main/java/com/mapbox/api/directions/v5/DirectionsService.java
        // That would allow us to skip adding fake attributes further down as well.
        // But this is the first step to show how the newly added banner_instructions
        // and voice_instructions of Valhalla can be used to generate directions directly:
        val requestBody = mapOf(
            "format" to "osrm",
            "costing" to "auto",
            "banner_instructions" to true,
            "voice_instructions" to true,
            "language" to language,
            "directions_options" to mapOf(
                "units" to "kilometers"
            ),
            "costing_options" to mapOf(
                "auto" to mapOf(
                    "top_speed" to 130
                )
            ),
            "locations" to listOf(
                mapOf(
                    "lon" to origin.longitude(),
                    "lat" to origin.latitude(),
                    "type" to "break"
                ),
                mapOf(
                    "lon" to destination.longitude(),
                    "lat" to destination.latitude(),
                    "type" to "break"
                )
            )
        )

        val requestBodyJson = Gson().toJson(requestBody)
        val client = OkHttpClient()

        // Create request object. Requires valhalla_url to be set in developer-config.xml
        // Don't use the following server in production, it is for demonstration purposes only:
        // <string name="valhalla_url" translatable="false">https://valhalla1.openstreetmap.de/route</string>
       /* val request = Request.Builder()
            .header("User-Agent", "MapLibre Android Navigation SDK Demo App")
            .url(getString(R.string.valhalla_url))
            .post(requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
*/
        Timber.d("calculateRoute enqueued requestBodyJson: %s", requestBodyJson)
        //client.newCall(request).enqueue(object : okhttp3.Callback {

         /*   override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.e(e, "calculateRoute Failed to get route from ValhallaRouting")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
*/
                println("language")
                println(language)
                val responseBodyJson = """
        {"routes":[{"voiceLocale":"tr","weight_name":"auto","weight":4446.103,"duration":4435.883,"distance":102255.922,"legs":[{"via_waypoints":[],"admins":[{"iso_3166_1_alpha3":"TUR","iso_3166_1":"TR"},{"iso_3166_1_alpha3":"TUR","iso_3166_1":"TR"}],"weight":4446.103,"duration":4435.883,"steps":[{"bannerInstructions":[{"primary":{"type":"continue","modifier":"uturn","text":"Yeni Bağdat Caddesi","components":[{"text":"Yeni Bağdat Caddesi","type":"text"}]},"distanceAlongGeometry":278.718}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>Yeni Bağdat Caddesi caddesinde kuzeydoğu yönüne ilerleyin. Sonra Yeni Bağdat Caddesi caddesinde kalmak için sola doğru U dönüşü yapın.<\/speak>","announcement":"Yeni Bağdat Caddesi caddesinde kuzeydoğu yönüne ilerleyin. Sonra Yeni Bağdat Caddesi caddesinde kalmak için sola doğru U dönüşü yapın.","distanceAlongGeometry":278.7},{"ssmlAnnouncement":"<speak>Yeni Bağdat Caddesi caddesinde kalmak için sola doğru U dönüşü yapın.<\/speak>","announcement":"Yeni Bağdat Caddesi caddesinde kalmak için sola doğru U dönüşü yapın.","distanceAlongGeometry":102.8}],"intersections":[{"bearings":[48],"entry":[true],"admin_index":0,"out":0,"geometry_index":0,"location":[29.474324,40.824624]}],"maneuver":{"instruction":"Yeni Bağdat Caddesi caddesinde kuzeydoğu yönüne doğru sürün.","type":"depart","bearing_after":48,"bearing_before":0,"location":[29.474324,40.824624]},"name":"Yeni Bağdat Caddesi","duration":27.119,"distance":278.718,"driving_side":"right","weight":25.763,"mode":"driving","geometry":"_rvzlAg`~ew@uPeYkQ_ZmNuZsXup@iNmb@"},{"bannerInstructions":[{"primary":{"degrees":266,"driving_side":"right","type":"roundabout","modifier":"slight right","text":"Yeni Bağdat Caddesi","components":[{"text":"Yeni Bağdat Caddesi","type":"text"}]},"distanceAlongGeometry":789.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>800 metre boyunca devam edin.<\/speak>","announcement":"800 metre boyunca devam edin.","distanceAlongGeometry":779.0},{"ssmlAnnouncement":"<speak>400 metre sonunda, Döner kavşağa girin ve İkinci çıkıştan Yeni Bağdat Caddesi caddesine doğru çıkın.<\/speak>","announcement":"400 metre sonunda, Döner kavşağa girin ve İkinci çıkıştan Yeni Bağdat Caddesi caddesine doğru çıkın.","distanceAlongGeometry":359.7},{"ssmlAnnouncement":"<speak>Döner kavşağa girin ve İkinci çıkıştan Yeni Bağdat Caddesi caddesine doğru çıkın.<\/speak>","announcement":"Döner kavşağa girin ve İkinci çıkıştan Yeni Bağdat Caddesi caddesine doğru çıkın.","distanceAlongGeometry":102.8}],"intersections":[{"entry":[true,true,false,true],"in":2,"bearings":[61,115,240,292],"duration":9.854,"turn_weight":10.000,"turn_duration":7.350,"admin_index":0,"out":3,"weight":12.317,"geometry_index":5,"location":[29.476980,40.826103]},{"entry":[false,false,true,true],"in":1,"bearings":[61,112,241,293],"duration":12.465,"turn_weight":15.000,"turn_duration":0.887,"admin_index":0,"out":2,"weight":25.710,"geometry_index":6,"location":[29.476805,40.826156]},{"bearings":[54,233,321],"entry":[false,true,true],"in":0,"turn_weight":0.500,"turn_duration":0.007,"admin_index":0,"out":1,"geometry_index":9,"location":[29.475621,40.825579]}],"maneuver":{"modifier":"uturn","instruction":"Yeni Bağdat Caddesi caddesinde kalmak için sola doğru U dönüşü yapın.","type":"continue","bearing_after":241,"bearing_before":60,"location":[29.476980,40.826103]},"name":"Yeni Bağdat Caddesi","duration":85.959,"distance":789.000,"driving_side":"right","weight":98.977,"mode":"driving","geometry":"mnyzlAgfcfw@iB|IvDvLpVfo@vE~JtDbI~Qz]nq@lkAjg@t|@zFvN~Qpc@t[``AzZh~@\\vMi@vJmA`J"},{"bannerInstructions":[{"primary":{"degrees":266,"driving_side":"right","type":"exit roundabout","modifier":"straight","text":"Yeni Bağdat Caddesi","components":[{"text":"Yeni Bağdat Caddesi","type":"text"}]},"distanceAlongGeometry":54.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>Yeni Bağdat Caddesi caddesine doğru döner kavşaktan çıkın.<\/speak>","announcement":"Yeni Bağdat Caddesi caddesine doğru döner kavşaktan çıkın.","distanceAlongGeometry":54.0}],"intersections":[{"entry":[false,false,true],"in":0,"bearings":[104,145,298],"duration":1.426,"turn_weight":5.825,"turn_duration":0.018,"admin_index":0,"out":2,"weight":7.198,"geometry_index":20,"location":[29.469263,40.822440]},{"entry":[false,true,true],"in":0,"bearings":[118,261,322],"duration":5.959,"turn_duration":0.324,"admin_index":0,"out":1,"weight":5.494,"geometry_index":21,"location":[29.469165,40.822479]},{"bearings":[6,53,201],"entry":[false,false,true],"in":1,"turn_duration":0.207,"admin_index":0,"out":2,"geometry_index":25,"location":[29.468795,40.822359]}],"maneuver":{"exit":2,"modifier":"slight right","instruction":"Döner kavşağa girin ve İkinci çıkıştan Yeni Bağdat Caddesi caddesine doğru çıkın.","type":"roundabout","bearing_after":298,"bearing_before":284,"location":[29.469263,40.822440]},"name":"Yeni Bağdat Caddesi","duration":9.001,"distance":54.000,"driving_side":"right","weight":14.066,"mode":"driving","geometry":"oirzlA}ctew@mAbECrEb@nErAtDzBhCtClA"},{"bannerInstructions":[{"primary":{"type":"fork","modifier":"slight left","text":"Yeni Bağdat Caddesi","components":[{"text":"Yeni Bağdat Caddesi","type":"text"}]},"distanceAlongGeometry":1393.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>1.5kilometre boyunca devam edin.<\/speak>","announcement":"1.5kilometre boyunca devam edin.","distanceAlongGeometry":1383.0},{"ssmlAnnouncement":"<speak>300 metre sonunda, Yeni Bağdat Caddesi caddesinde kalmak için solda kalın.<\/speak>","announcement":"300 metre sonunda, Yeni Bağdat Caddesi caddesinde kalmak için solda kalın.","distanceAlongGeometry":311.1},{"ssmlAnnouncement":"<speak>Yeni Bağdat Caddesi caddesinde kalmak için solda kalın.<\/speak>","announcement":"Yeni Bağdat Caddesi caddesinde kalmak için solda kalın.","distanceAlongGeometry":88.9}],"intersections":[{"entry":[false,true,true],"in":0,"bearings":[21,170,198],"duration":2.064,"turn_weight":5.550,"turn_duration":0.021,"admin_index":0,"out":2,"weight":7.542,"geometry_index":26,"location":[29.468756,40.822284]},{"entry":[false,true,true],"in":0,"bearings":[18,206,275],"duration":24.424,"turn_weight":0.600,"turn_duration":0.012,"admin_index":0,"out":1,"weight":25.012,"geometry_index":27,"location":[29.468679,40.822108]},{"entry":[false,true,true],"in":0,"bearings":[34,214,313],"duration":10.132,"turn_weight":0.600,"turn_duration":0.007,"admin_index":0,"out":1,"weight":10.725,"geometry_index":31,"location":[29.467311,40.820461]},{"entry":[false,true,true],"in":0,"bearings":[32,212,297],"duration":10.357,"turn_weight":0.650,"turn_duration":0.007,"admin_index":0,"out":1,"weight":11.259,"geometry_index":33,"location":[29.466725,40.819791]},{"entry":[false,true,true],"in":0,"bearings":[32,213,322],"duration":6.982,"turn_weight":0.650,"turn_duration":0.007,"admin_index":0,"out":1,"weight":7.799,"geometry_index":34,"location":[29.466142,40.819094]},{"entry":[false,true,true,true],"in":0,"bearings":[34,145,214,324],"duration":54.907,"turn_weight":2.800,"turn_duration":0.007,"admin_index":0,"out":2,"weight":60.445,"geometry_index":36,"location":[29.465732,40.818635]},{"entry":[false,true,true],"in":0,"bearings":[52,231,332],"duration":1.032,"turn_weight":0.800,"turn_duration":0.019,"admin_index":0,"out":1,"weight":1.888,"geometry_index":43,"location":[29.462251,40.815165]},{"entry":[false,true,true],"in":0,"bearings":[51,235,320],"duration":9.459,"turn_weight":0.800,"turn_duration":0.009,"admin_index":0,"out":1,"weight":10.959,"geometry_index":44,"location":[29.462170,40.815115]},{"bearings":[55,235,325],"entry":[false,true,true],"in":0,"turn_weight":0.950,"turn_duration":0.007,"admin_index":0,"out":1,"geometry_index":45,"location":[29.461345,40.814684]}],"maneuver":{"modifier":"straight","instruction":"Yeni Bağdat Caddesi caddesine doğru döner kavşaktan çıkın.","type":"exit roundabout","bearing_after":198,"bearing_before":201,"location":[29.468756,40.822284]},"name":"Yeni Bağdat Caddesi","duration":156.490,"distance":1393.000,"driving_side":"right","weight":177.417,"mode":"driving","geometry":"w_rzlAgdsew@~IxCzKdGfQ|Lbi@zc@t\\nYxY`W`NpKpj@lc@zC`CxVpTnPxNppAjfAfx@hp@xRbUdO`RfLpShJhRbB`D|Ypr@jClG~]zw@nZbr@v^|~@|C`HnB`DdC|C"},{"bannerInstructions":[{"primary":{"type":"turn","modifier":"left","text":"sola dönün.","components":[{"text":"sola dönün.","type":"text"}]},"distanceAlongGeometry":648.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>600 metre boyunca devam edin.<\/speak>","announcement":"600 metre boyunca devam edin.","distanceAlongGeometry":638.0},{"ssmlAnnouncement":"<speak>300 metre sonunda, sola dönün.<\/speak>","announcement":"300 metre sonunda, sola dönün.","distanceAlongGeometry":281.9},{"ssmlAnnouncement":"<speak>sola dönün.<\/speak>","announcement":"sola dönün.","distanceAlongGeometry":80.5}],"intersections":[{"entry":[false,true,true],"in":0,"bearings":[45,221,244],"duration":5.649,"turn_weight":0.950,"turn_duration":0.024,"admin_index":0,"out":1,"weight":7.138,"geometry_index":52,"location":[29.458154,40.812968]},{"entry":[false,true],"in":0,"bearings":[41,222],"duration":18.112,"turn_weight":0.950,"admin_index":0,"out":1,"weight":20.874,"geometry_index":53,"location":[29.457765,40.812627]},{"entry":[false,true],"in":0,"bearings":[42,220],"duration":9.434,"turn_weight":1.100,"admin_index":0,"out":1,"weight":11.714,"geometry_index":54,"location":[29.456492,40.811544]},{"entry":[false,false,true],"in":1,"bearings":[34,40,223],"duration":19.250,"turn_weight":3.300,"turn_duration":0.008,"admin_index":0,"out":2,"weight":24.947,"geometry_index":55,"location":[29.455908,40.811021]},{"bearings":[48,144,228,322],"entry":[false,true,true,true],"in":0,"turn_weight":4.400,"turn_duration":0.007,"admin_index":0,"out":2,"geometry_index":59,"location":[29.454631,40.810017]}],"maneuver":{"modifier":"slight left","instruction":"Yeni Bağdat Caddesi caddesinde kalmak için solda kalın.","type":"fork","bearing_after":221,"bearing_before":225,"location":[29.458154,40.812968]},"name":"Yeni Bağdat Caddesi","duration":78.025,"distance":648.000,"driving_side":"right","weight":97.841,"mode":"driving","geometry":"oy_zlAsm~dw@hThWtbApnAt_@nc@x]vd@pLbO`EtFhKfQxL`SrPdX~Q~ZbZxf@"},{"bannerInstructions":[{"primary":{"degrees":205,"driving_side":"right","type":"roundabout","modifier":"slight left","text":"Döner kavşağa girin ve Birinci çıkıştan çıkın.","components":[{"text":"Döner kavşağa girin ve Birinci çıkıştan çıkın.","type":"text"}]},"distanceAlongGeometry":421.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>400 metre boyunca devam edin.<\/speak>","announcement":"400 metre boyunca devam edin.","distanceAlongGeometry":411.0},{"ssmlAnnouncement":"<speak>200 metre sonunda, Döner kavşağa girin ve Birinci çıkıştan çıkın.<\/speak>","announcement":"200 metre sonunda, Döner kavşağa girin ve Birinci çıkıştan çıkın.","distanceAlongGeometry":223.5},{"ssmlAnnouncement":"<speak>Döner kavşağa girin ve Birinci çıkıştan çıkın.<\/speak>","announcement":"Döner kavşağa girin ve Birinci çıkıştan çıkın.","distanceAlongGeometry":63.9}],"intersections":[{"entry":[false,true,true],"in":0,"bearings":[48,132,227],"duration":7.277,"turn_weight":11.250,"turn_duration":6.024,"admin_index":0,"out":1,"weight":12.690,"geometry_index":63,"location":[29.452822,40.808776]},{"entry":[true,true,false,false],"in":3,"bearings":[48,130,227,312],"duration":17.240,"turn_weight":20.000,"turn_duration":0.022,"admin_index":0,"out":1,"weight":39.800,"geometry_index":64,"location":[29.452895,40.808727]},{"bearings":[130,144,310],"entry":[false,true,false],"in":2,"turn_weight":5.625,"turn_duration":0.018,"admin_index":0,"out":1,"geometry_index":65,"location":[29.453905,40.808096]}],"maneuver":{"modifier":"left","instruction":"sola dönün.","type":"turn","bearing_after":130,"bearing_before":228,"location":[29.452822,40.808776]},"name":"","duration":71.960,"distance":421.000,"driving_side":"right","weight":112.655,"mode":"driving","geometry":"oswylAk`tdw@`BqClf@c~@lWaVb_@{h@fNg[pLuYnLwT`JgNjEiC"},{"bannerInstructions":[{"primary":{"degrees":205,"driving_side":"right","type":"exit roundabout","modifier":"slight right","text":"Döner kavşaktan çıkın","components":[{"text":"Döner kavşaktan çıkın","type":"text"}]},"distanceAlongGeometry":26.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>Döner kavşaktan çıkın.<\/speak>","announcement":"Döner kavşaktan çıkın.","distanceAlongGeometry":26.0}],"intersections":[{"bearings":[0,123,325],"entry":[false,true,false],"in":2,"turn_weight":3.750,"turn_duration":0.104,"admin_index":0,"out":1,"geometry_index":72,"location":[29.456484,40.806235]}],"maneuver":{"exit":1,"modifier":"slight left","instruction":"Döner kavşağa girin ve Birinci çıkıştan çıkın.","type":"roundabout","bearing_after":123,"bearing_before":145,"location":[29.456484,40.806235]},"name":"","duration":4.784,"distance":26.000,"driving_side":"right","weight":9.132,"mode":"driving","geometry":"utrylAge{dw@pAy@~@yAd@oBHyBSwB"},{"bannerInstructions":[{"primary":{"degrees":294,"driving_side":"right","type":"rotary","modifier":"slight right","text":"Terminal Caddesi","components":[{"text":"Terminal Caddesi","type":"text"}]},"distanceAlongGeometry":378.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>400 metre boyunca devam edin.<\/speak>","announcement":"400 metre boyunca devam edin.","distanceAlongGeometry":368.0},{"ssmlAnnouncement":"<speak>200 metre sonunda, Hükümet Caddesi caddesine girin veİkinci çıkıştan Terminal Caddesi caddesine doğru çıkın.<\/speak>","announcement":"200 metre sonunda, Hükümet Caddesi caddesine girin veİkinci çıkıştan Terminal Caddesi caddesine doğru çıkın.","distanceAlongGeometry":223.5},{"ssmlAnnouncement":"<speak>Hükümet Caddesi caddesine girin veİkinci çıkıştan Terminal Caddesi caddesine doğru çıkın.<\/speak>","announcement":"Hükümet Caddesi caddesine girin veİkinci çıkıştan Terminal Caddesi caddesine doğru çıkın.","distanceAlongGeometry":63.9}],"intersections":[{"bearings":[38,120,286],"entry":[true,true,false],"in":2,"turn_weight":3.750,"turn_duration":0.018,"admin_index":0,"out":1,"geometry_index":77,"location":[29.456735,40.806148]}],"maneuver":{"modifier":"slight right","instruction":"Döner kavşaktan çıkın","type":"exit roundabout","bearing_after":120,"bearing_before":106,"location":[29.456735,40.806148]},"name":"","duration":59.183,"distance":378.000,"driving_side":"right","weight":70.311,"mode":"driving","geometry":"gorylA}t{dw@pKc\\lV{h@dKwWdKoOzOeLlPqExdAqG"},{"bannerInstructions":[{"primary":{"degrees":169,"driving_side":"right","type":"exit rotary","modifier":"sharp right","text":"Terminal Caddesi","components":[{"text":"Terminal Caddesi","type":"text"}]},"distanceAlongGeometry":67.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>Terminal Caddesi caddesine doğru döner kavşaktan çıkın.<\/speak>","announcement":"Terminal Caddesi caddesine doğru döner kavşaktan çıkın.","distanceAlongGeometry":42.1}],"intersections":[{"entry":[false,true,false],"in":2,"bearings":[63,188,355],"duration":4.253,"turn_weight":8.300,"turn_duration":0.018,"admin_index":0,"out":1,"weight":13.065,"geometry_index":84,"location":[29.458984,40.803516]},{"entry":[false,true,true],"in":0,"bearings":[8,150,207],"duration":1.360,"turn_duration":0.301,"admin_index":0,"out":1,"weight":1.191,"geometry_index":88,"location":[29.458951,40.803347]},{"entry":[true,false],"in":1,"bearings":[119,330],"duration":3.176,"admin_index":0,"out":0,"weight":3.574,"geometry_index":89,"location":[29.458981,40.803307]},{"bearings":[64,234,299],"entry":[true,false,false],"in":2,"turn_duration":1.086,"admin_index":0,"out":0,"geometry_index":92,"location":[29.459136,40.803241]}],"maneuver":{"exit":2,"modifier":"slight right","instruction":"Hükümet Caddesi caddesine girin ve İkinci çıkıştan Terminal Caddesi caddesine çıkın.","type":"rotary","bearing_after":188,"bearing_before":175,"location":[29.458984,40.803516]},"rotary_name":"Hükümet Caddesi","name":"Terminal Caddesi","duration":15.593,"distance":67.000,"driving_side":"right","weight":24.262,"mode":"driving","geometry":"wjmylAoa`ew@jAz@tA`@xA@tA]nA{@`AuAj@iBTuBCaC_@yBy@iBmAoA{Am@"},{"bannerInstructions":[{"primary":{"type":"off ramp","modifier":"slight left","text":"D100","components":[{"text":"D100","type":"text"},{"text":"\/","type":"delimiter"},{"text":"D100","type":"text"}]},"distanceAlongGeometry":529.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>500 metre boyunca devam edin.<\/speak>","announcement":"500 metre boyunca devam edin.","distanceAlongGeometry":519.0},{"ssmlAnnouncement":"<speak>300 metre sonunda, soldaki D100 çıkışından çıkın.<\/speak>","announcement":"300 metre sonunda, soldaki D100 çıkışından çıkın.","distanceAlongGeometry":288.2},{"ssmlAnnouncement":"<speak>soldaki D100 çıkışından çıkın.<\/speak>","announcement":"soldaki D100 çıkışından çıkın.","distanceAlongGeometry":86.9}],"intersections":[{"entry":[true,false,true],"in":1,"bearings":[61,225,349],"duration":15.680,"turn_weight":1.650,"turn_duration":0.020,"admin_index":0,"out":0,"weight":19.268,"geometry_index":97,"location":[29.459378,40.803373]},{"entry":[false,true,false],"in":2,"bearings":[6,186,322],"duration":11.055,"turn_weight":6.650,"turn_duration":0.131,"admin_index":0,"out":1,"weight":18.940,"geometry_index":104,"location":[29.460240,40.803308]},{"entry":[false,true,true],"in":0,"bearings":[6,186,199],"duration":22.848,"turn_weight":1.100,"turn_duration":0.007,"admin_index":0,"out":1,"weight":26.797,"geometry_index":105,"location":[29.460134,40.802525]},{"entry":[true,false,false],"in":2,"bearings":[173,334,351],"duration":1.622,"turn_weight":3.300,"turn_duration":0.008,"admin_index":0,"out":0,"weight":5.116,"geometry_index":110,"location":[29.460214,40.800885]},{"entry":[true,false],"in":1,"bearings":[171,353],"duration":11.048,"turn_weight":6.100,"admin_index":0,"out":0,"weight":18.253,"geometry_index":111,"location":[29.460234,40.800766]},{"bearings":[159,351],"entry":[true,false],"in":1,"turn_weight":6.425,"admin_index":0,"out":0,"geometry_index":112,"location":[29.460406,40.799982]}],"maneuver":{"modifier":"sharp right","instruction":"Terminal Caddesi caddesine doğru döner kavşaktan çıkın.","type":"exit rotary","bearing_after":186,"bearing_before":45,"location":[29.459378,40.803373]},"name":"Terminal Caddesi","duration":69.904,"distance":529.000,"driving_side":"right","weight":103.212,"mode":"driving","geometry":"yamylAcz`ew@qEwNaA}EYcHv@wFrBuEpBgCpCkB|o@rEhD\\fS~@|WF|VeA`[_ElFg@~o@wIhG}BxI{EjMcKJI"},{"voiceInstructions":[{"ssmlAnnouncement":"<speak>700 metre boyunca devam edin.<\/speak>","announcement":"700 metre boyunca devam edin.","distanceAlongGeometry":713.0},{"ssmlAnnouncement":"<speak>400 metre sonunda, KOCAELİ YOLUcaddesine devam edin.<\/speak>","announcement":"400 metre sonunda, KOCAELİ YOLUcaddesine devam edin.","distanceAlongGeometry":403.7},{"ssmlAnnouncement":"<speak>KOCAELİ YOLU caddesine devam edin.<\/speak>","announcement":"KOCAELİ YOLU caddesine devam edin.","distanceAlongGeometry":174.9}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[135,144,327],"duration":21.645,"turn_weight":4.275,"turn_duration":0.045,"admin_index":0,"out":0,"weight":28.035,"geometry_index":116,"location":[29.460778,40.799440]},{"entry":[true,false,false],"in":2,"bearings":[9,184,203],"duration":41.075,"turn_weight":1.425,"turn_duration":0.056,"admin_index":0,"out":0,"weight":45.520,"geometry_index":124,"location":[29.462520,40.799685]},{"entry":[true,false,false],"in":1,"bearings":[81,257,260],"duration":0.393,"turn_weight":24.200,"turn_duration":0.009,"admin_index":0,"out":0,"weight":24.613,"geometry_index":131,"location":[29.466223,40.801009]},{"entry":[true,true,false],"in":2,"bearings":[80,104,261],"duration":5.575,"turn_duration":0.007,"admin_index":0,"out":0,"weight":5.846,"geometry_index":132,"location":[29.466313,40.801020]},{"bearings":[81,235,261],"entry":[true,false,false],"in":2,"turn_duration":0.008,"admin_index":0,"out":0,"geometry_index":134,"location":[29.467672,40.801190]}],"bannerInstructions":[{"primary":{"type":"new name","modifier":"straight","text":"KOCAELİ YOLU","components":[{"text":"KOCAELİ YOLU","type":"text"},{"text":"\/","type":"delimiter"},{"text":"D-100","type":"text"}]},"distanceAlongGeometry":723.000}],"destinations":"D100","maneuver":{"modifier":"slight left","instruction":"soltaki D100 çıkıştan çıkın.","type":"off ramp","bearing_after":135,"bearing_before":147,"location":[29.460778,40.799440]},"name":"","duration":69.895,"distance":723.000,"driving_side":"right","weight":105.274,"mode":"driving","ref":"D100","geometry":"_leylAsqcew@hPaVrDoNb@gJs@qLyAsMsD{H{GoHmSoJwU_D}J}EiHyKwDmMgDaTkJ}kAkOg}AUsDsEcs@_Cy_@gAgQ"},{"bannerInstructions":[{"primary":{"type":"turn","modifier":"slight left","text":"KOCAELİ YOLU","components":[{"text":"KOCAELİ YOLU","type":"text"},{"text":"\/","type":"delimiter"},{"text":"D100","type":"text"}]},"distanceAlongGeometry":2465.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>2.5kilometre boyunca devam edin.<\/speak>","announcement":"2.5kilometre boyunca devam edin.","distanceAlongGeometry":2455.0},{"ssmlAnnouncement":"<speak>700 metre sonunda, D100 caddesine doğru sol yönelin.<\/speak>","announcement":"700 metre sonunda, D100 caddesine doğru sol yönelin.","distanceAlongGeometry":728.8},{"ssmlAnnouncement":"<speak>D100, KOCAELİ YOLU caddesine doğru sol yönelin.<\/speak>","announcement":"D100, KOCAELİ YOLU caddesine doğru sol yönelin.","distanceAlongGeometry":208.2}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[81,90,261],"duration":19.736,"turn_weight":5.000,"turn_duration":0.008,"admin_index":0,"out":0,"weight":25.221,"geometry_index":135,"location":[29.467964,40.801226]},{"entry":[true,false,false],"in":2,"bearings":[73,244,254],"duration":25.927,"turn_weight":1.950,"turn_duration":0.007,"admin_index":0,"out":0,"weight":28.518,"geometry_index":141,"location":[29.472734,40.801992]},{"entry":[true,false],"in":1,"bearings":[75,253],"duration":1.104,"turn_weight":5.000,"admin_index":0,"out":0,"weight":6.132,"geometry_index":144,"location":[29.478874,40.803380]},{"entry":[true,false],"in":1,"bearings":[76,255],"duration":8.688,"admin_index":0,"out":0,"weight":8.688,"geometry_index":145,"location":[29.479134,40.803434]},{"entry":[true,true,false],"in":2,"bearings":[79,94,256],"duration":14.072,"turn_duration":0.008,"admin_index":0,"out":0,"weight":14.064,"geometry_index":147,"location":[29.481217,40.803831]},{"entry":[true,false,false],"in":2,"bearings":[81,251,260],"duration":1.064,"turn_weight":0.600,"turn_duration":0.008,"admin_index":0,"out":0,"weight":1.656,"geometry_index":151,"location":[29.484624,40.804340]},{"entry":[true,false,false],"in":2,"bearings":[79,168,261],"duration":28.915,"turn_duration":0.019,"admin_index":0,"out":0,"weight":28.896,"geometry_index":152,"location":[29.484876,40.804371]},{"entry":[true,false],"classes":["tunnel"],"in":1,"bearings":[80,260],"duration":7.488,"admin_index":0,"out":0,"weight":7.488,"geometry_index":155,"location":[29.491907,40.805310]},{"entry":[true,false],"in":1,"bearings":[79,260],"duration":6.048,"admin_index":0,"out":0,"weight":6.048,"geometry_index":156,"location":[29.493731,40.805548]},{"bearings":[80,124,259],"entry":[true,true,false],"in":2,"turn_duration":0.007,"admin_index":0,"out":0,"geometry_index":157,"location":[29.495201,40.805766]}],"maneuver":{"modifier":"straight","instruction":"KOCAELİ YOLU caddesine devam edin.","type":"new name","bearing_after":81,"bearing_before":81,"location":[29.467964,40.801226]},"name":"KOCAELİ YOLU","duration":118.378,"distance":2465.000,"driving_side":"right","weight":132.039,"mode":"driving","ref":"D-100","geometry":"s{hylAwrqew@oHwlA}Eiq@}Esl@cFwd@aJox@iEe\\kIol@eSoxAew@wwFkBgO{L_hA}Iex@gQw}B}DaZeCo`@mBsX}@wNqA_PeR{pC}c@qsG{M_qBsL{zAqIopA"},{"bannerInstructions":[{"primary":{"type":"on ramp","modifier":"slight right","text":"Rampayı kullanın.","components":[{"text":"Rampayı kullanın.","type":"text"}]},"distanceAlongGeometry":1212.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>1 kilometre boyunca devam edin.<\/speak>","announcement":"1 kilometre boyunca devam edin.","distanceAlongGeometry":1202.0},{"ssmlAnnouncement":"<speak>700 metre sonunda, Rampayı kullanın.<\/speak>","announcement":"700 metre sonunda, Rampayı kullanın.","distanceAlongGeometry":728.7},{"ssmlAnnouncement":"<speak>Rampayı kullanın.<\/speak>","announcement":"Rampayı kullanın.","distanceAlongGeometry":208.3}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[80,99,260],"duration":15.559,"turn_duration":0.007,"admin_index":0,"out":0,"weight":15.552,"geometry_index":158,"location":[29.496505,40.805935]},{"entry":[true,false],"in":1,"bearings":[79,260],"duration":1.200,"admin_index":0,"out":0,"weight":1.200,"geometry_index":160,"location":[29.500286,40.806458]},{"entry":[true,false],"in":1,"bearings":[78,259],"duration":12.624,"turn_weight":5.000,"admin_index":0,"out":0,"weight":17.624,"geometry_index":161,"location":[29.500582,40.806501]},{"entry":[true,true,false],"in":2,"bearings":[68,87,251],"duration":11.062,"turn_duration":0.022,"admin_index":0,"out":0,"weight":11.040,"geometry_index":166,"location":[29.503596,40.807105]},{"bearings":[61,240],"entry":[true,false],"in":1,"admin_index":0,"out":0,"geometry_index":169,"location":[29.506081,40.807958]}],"maneuver":{"modifier":"slight left","instruction":"D100\/KOCAELİ YOLU caddesine doğru sol yönelin.","type":"turn","bearing_after":80,"bearing_before":80,"location":[29.496505,40.805935]},"name":"KOCAELİ YOLU","duration":58.205,"distance":1212.000,"driving_side":"right","weight":62.732,"mode":"driving","ref":"D100","geometry":"}arylAqjigw@cQkaCqM}hBuAoQaBuRcHcz@_Gyi@_Jai@qEuWy[e}AwPml@wEuNsRan@yMy]iz@mzBmNe_@"},{"bannerInstructions":[{"primary":{"type":"fork","modifier":"slight left","text":"Çatalın sol yönünde kalın.","components":[{"text":"Çatalın sol yönünde kalın.","type":"text"}]},"distanceAlongGeometry":407.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>Çatalın solda kalın. Sonra Çatalın solda kalın.<\/speak>","announcement":"Çatalın solda kalın. Sonra Çatalın solda kalın.","distanceAlongGeometry":227.5}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[58,60,238],"duration":14.233,"turn_duration":0.008,"admin_index":0,"out":1,"weight":13.869,"geometry_index":173,"location":[29.509817,40.809705]},{"entry":[true,false,false],"in":2,"bearings":[86,258,264],"duration":2.466,"turn_weight":1.650,"turn_duration":0.007,"admin_index":0,"out":0,"weight":4.047,"geometry_index":180,"location":[29.513381,40.810721]},{"bearings":[89,270],"entry":[true,false],"in":1,"admin_index":0,"out":0,"geometry_index":182,"location":[29.514041,40.810735]}],"maneuver":{"modifier":"slight right","instruction":"Rampayı kullanın.","type":"on ramp","bearing_after":60,"bearing_before":58,"location":[29.509817,40.809705]},"name":"","duration":17.884,"distance":407.000,"driving_side":"right","weight":19.072,"mode":"driving","geometry":"qmyylAqjchw@uXiz@eH}WqI}^uEiZ}Di]}Bo[o@mS]yR@mTIiS"},{"bannerInstructions":[{"primary":{"type":"fork","modifier":"slight left","text":"Çatalın sol yönünde kalın.","components":[{"text":"Çatalın sol yönünde kalın.","type":"text"}]},"distanceAlongGeometry":278.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>Çatalın solda kalın.<\/speak>","announcement":"Çatalın solda kalın.","distanceAlongGeometry":227.3}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[87,92,269],"duration":8.055,"turn_duration":0.021,"admin_index":0,"out":0,"weight":7.833,"geometry_index":183,"location":[29.514366,40.810740]},{"bearings":[93,270,273],"entry":[true,false,false],"in":2,"turn_weight":0.550,"turn_duration":0.007,"admin_index":0,"out":0,"geometry_index":187,"location":[29.516533,40.810731]}],"maneuver":{"modifier":"slight left","instruction":"Çatalın sol yönünde kalın.","type":"fork","bearing_after":87,"bearing_before":89,"location":[29.514366,40.810740]},"name":"","duration":12.232,"distance":278.000,"driving_side":"right","weight":12.450,"mode":"driving","geometry":"gn{ylA{flhw@c@c`@OoOTmm@n@kf@VkQbAis@"},{"bannerInstructions":[{"primary":{"type":"fork","modifier":"slight right","text":"D-100 Gebze Köprülü Kavşağı","components":[{"text":"D-100 Gebze Köprülü Kavşağı","type":"text"}]},"distanceAlongGeometry":349.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>D-100 Gebze Köprülü Kavşağı caddesine girmek için sağda kalın.<\/speak>","announcement":"D-100 Gebze Köprülü Kavşağı caddesine girmek için sağda kalın.","distanceAlongGeometry":69.4}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[93,108,273],"duration":7.075,"turn_duration":0.007,"admin_index":0,"out":0,"weight":6.892,"geometry_index":189,"location":[29.517664,40.810685]},{"bearings":[99,255,277],"entry":[true,false,false],"in":2,"turn_weight":1.100,"turn_duration":0.008,"admin_index":0,"out":0,"geometry_index":192,"location":[29.519570,40.810543]}],"maneuver":{"modifier":"slight left","instruction":"Çatalın sol yönünde kalın.","type":"fork","bearing_after":93,"bearing_before":93,"location":[29.517664,40.810685]},"name":"","duration":34.155,"distance":349.000,"driving_side":"right","weight":34.387,"mode":"driving","geometry":"yj{ylA_urhw@`@i[jCas@lBwe@pO_iC"},{"bannerInstructions":[{"primary":{"type":"fork","modifier":"slight left","text":"Kuzey Marmara Otoyolu","components":[{"text":"Kuzey Marmara Otoyolu","type":"text"},{"text":"\/","type":"delimiter"},{"text":"O-7","type":"text"}]},"distanceAlongGeometry":827.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>600 metre sonunda, O-7 caddesine girmek için solda kalın.<\/speak>","announcement":"600 metre sonunda, O-7 caddesine girmek için solda kalın.","distanceAlongGeometry":563.6},{"ssmlAnnouncement":"<speak>O-7, Kuzey Marmara Otoyolu caddesine girmek için solda kalın.<\/speak>","announcement":"O-7, Kuzey Marmara Otoyolu caddesine girmek için solda kalın.","distanceAlongGeometry":161.1}],"intersections":[{"entry":[true,true,false],"classes":["motorway"],"in":2,"bearings":[101,113,279],"duration":33.597,"turn_duration":0.018,"admin_index":0,"out":1,"weight":32.740,"geometry_index":193,"location":[29.521778,40.810278]},{"entry":[true,false,false],"classes":["motorway"],"in":1,"bearings":[7,179,187],"duration":7.522,"turn_weight":0.550,"turn_duration":0.012,"admin_index":0,"out":0,"weight":7.873,"geometry_index":224,"location":[29.520636,40.809203]},{"entry":[true,false],"classes":["motorway"],"in":1,"bearings":[13,190],"duration":3.910,"admin_index":0,"out":0,"weight":3.813,"geometry_index":226,"location":[29.520844,40.810280]},{"bearings":[18,194],"entry":[true,false],"classes":["motorway"],"in":1,"admin_index":0,"out":0,"geometry_index":228,"location":[29.521017,40.810833]}],"maneuver":{"modifier":"slight right","instruction":"D-100 Gebze Köprülü Kavşağı caddesine girmek için sağda kalın.","type":"fork","bearing_after":113,"bearing_before":99,"location":[29.521778,40.810278]},"name":"D-100 Gebze Köprülü Kavşağı","duration":51.361,"distance":827.000,"driving_side":"right","weight":50.598,"mode":"driving","geometry":"kqzylAcvzhw@dCuJlBqN|BcIlCeGhD{DhD_C~E}AzDk@fFOvJhAbLnC|JbDlJpExLpGfOdM~LnOfDbHxBnHx@lG?tF_@rGiAlFeBfE}CxEoD|C_F|AiFp@mHXoMD}LUsLFq`@gDw`@wF}QsDsNeDou@wU"},{"bannerInstructions":[{"primary":{"type":"fork","modifier":"slight right","text":"E-80: KOCAELİ","components":[{"text":"E-80: KOCAELİ","type":"text"},{"text":"\/","type":"delimiter"},{"text":"O-7","type":"text"}]},"distanceAlongGeometry":9563.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>10kilometre boyunca devam edin.<\/speak>","announcement":"10kilometre boyunca devam edin.","distanceAlongGeometry":9553.0},{"ssmlAnnouncement":"<speak>600 metre sonunda, O-7 caddesinde kalmak için sağda kalın.<\/speak>","announcement":"600 metre sonunda, O-7 caddesinde kalmak için sağda kalın.","distanceAlongGeometry":628.9},{"ssmlAnnouncement":"<speak>O-7 caddesinde kalmak için KOCAELİ işaretine doğru sağda kalın.<\/speak>","announcement":"O-7 caddesinde kalmak için KOCAELİ işaretine doğru sağda kalın.","distanceAlongGeometry":179.7}],"intersections":[{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[21,30,198],"duration":25.808,"turn_weight":15.000,"turn_duration":15.008,"admin_index":0,"out":0,"weight":25.260,"geometry_index":229,"location":[29.521381,40.811705]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":1,"bearings":[32,199,210],"duration":3.478,"turn_weight":14.000,"turn_duration":0.017,"admin_index":0,"out":0,"weight":17.288,"geometry_index":234,"location":[29.522177,40.813148]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"bearings":[39,199,219],"duration":28.819,"turn_weight":1.000,"turn_duration":0.019,"admin_index":0,"out":0,"weight":28.360,"geometry_index":237,"location":[29.522860,40.813883]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[64,243],"duration":2.112,"admin_index":0,"out":0,"weight":2.006,"geometry_index":249,"location":[29.530931,40.818046]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[64,244],"duration":27.415,"toll_collection":{"type":"toll_gantry"},"admin_index":0,"out":0,"weight":26.045,"geometry_index":250,"location":[29.531588,40.818285]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[23,32,204],"duration":43.219,"turn_duration":0.019,"admin_index":0,"out":0,"weight":39.960,"geometry_index":267,"location":[29.538324,40.822991]},{"entry":[false,false,true],"classes":["toll","motorway"],"in":1,"bearings":[162,169,353],"duration":52.417,"turn_duration":0.009,"admin_index":0,"out":2,"weight":48.477,"geometry_index":286,"location":[29.537010,40.833903]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[5,13,185],"duration":35.246,"turn_duration":0.008,"admin_index":0,"out":0,"weight":31.715,"geometry_index":295,"location":[29.537793,40.847461]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"bearings":[4,180,184],"duration":23.926,"turn_duration":0.007,"admin_index":0,"out":0,"weight":21.527,"geometry_index":298,"location":[29.538764,40.856573]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[17,195],"duration":19.661,"toll_collection":{"type":"toll_gantry"},"admin_index":0,"out":0,"weight":17.695,"geometry_index":307,"location":[29.540092,40.862685]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[17,23,198],"duration":14.871,"turn_duration":0.021,"admin_index":0,"out":0,"weight":13.365,"geometry_index":311,"location":[29.542120,40.867553]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[18,198],"duration":20.492,"admin_index":0,"out":0,"weight":18.443,"geometry_index":314,"location":[29.543658,40.871230]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"bearings":[22,191,202],"duration":16.243,"turn_duration":0.008,"admin_index":0,"out":0,"weight":15.017,"geometry_index":319,"location":[29.545817,40.876289]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[32,209],"duration":6.231,"admin_index":0,"out":0,"weight":5.919,"geometry_index":324,"location":[29.548225,40.880088]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[36,212],"duration":21.300,"turn_duration":15.000,"turn_weight":15.000,"toll_collection":{"type":"toll_booth"},"admin_index":0,"out":0,"weight":20.985,"geometry_index":326,"location":[29.549371,40.881456]},{"bearings":[40,216],"entry":[true,false],"classes":["toll","motorway"],"in":1,"turn_weight":15.000,"turn_duration":15.000,"admin_index":0,"out":0,"geometry_index":327,"location":[29.550638,40.882777]}],"maneuver":{"modifier":"slight left","instruction":"O-7\/Kuzey Marmara Otoyolu caddesine girmek için solda kalın.","type":"fork","bearing_after":21,"bearing_before":18,"location":[29.521381,40.811705]},"name":"Kuzey Marmara Otoyolu","duration":380.919,"distance":9563.000,"driving_side":"right","weight":369.893,"mode":"driving","ref":"O-7","geometry":"qj}ylAi}yhw@}PgHeZkOwOkHsKsFuNcFoOyLcKkIiPoQs[k]mNaQwQsWgNaTkUya@kR}a@oQ{a@gMg[_KiZeJo\\_R_o@m`AskD}Mah@gWy`Ay[sbA{Osc@_HePmHkPePg\\sLcTcN}ScMaQaMqOa]q_@cJwIoJ}HkJyI}YeSwLgI{Y}N{SmJ}UoJaYuIuVcFiTcDaTqCcRyAyRkAyNOgOOcTTyV|@aTtAgUpC_ZzE{VdEqnFjyAyi@zJqt@pLcv@pHyw@rEyeAdAcbAyB{dBeIipIyb@ao@iDmw@cEyt@aE{xI_d@muCoOehAeFol@sC{q@sEwh@eEmp@eHg_@uFk}@}N}o@wMu^eJeRyE}g@uO{_@oLqnCyz@{uAwc@kqAq`@kzA{e@aw@uV}gDoeAqiAw\\iH{Cgi@qQcTgJkp@_Zii@eVkc@iVep@c_@e{@{k@_v@cm@o]oXqqAenAinAiwA{bFuzF"},{"voiceInstructions":[{"ssmlAnnouncement":"<speak>44kilometre boyunca devam edin.<\/speak>","announcement":"44kilometre boyunca devam edin.","distanceAlongGeometry":43946.0},{"ssmlAnnouncement":"<speak>1 kilometre sonunda, O-7 caddesine girmek için solda kalın.<\/speak>","announcement":"1 kilometre sonunda, O-7 caddesine girmek için solda kalın.","distanceAlongGeometry":1010.2},{"ssmlAnnouncement":"<speak>O-7, E-80 caddesine girmek için solda kalın.<\/speak>","announcement":"O-7, E-80 caddesine girmek için solda kalın.","distanceAlongGeometry":288.4}],"intersections":[{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[32,48,220],"duration":33.554,"turn_duration":0.012,"admin_index":0,"out":1,"weight":31.027,"geometry_index":329,"location":[29.556078,40.887692]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":1,"bearings":[111,288,293],"duration":295.450,"turn_weight":1.500,"turn_duration":0.008,"admin_index":0,"out":0,"weight":267.398,"geometry_index":351,"location":[29.565794,40.890096]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[110,291],"duration":26.204,"admin_index":0,"out":0,"weight":22.928,"geometry_index":416,"location":[29.664474,40.895950]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[109,290],"duration":86.815,"admin_index":0,"out":0,"weight":75.964,"geometry_index":417,"location":[29.672952,40.893668]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[97,100,274],"duration":58.785,"turn_duration":0.008,"admin_index":0,"out":0,"weight":51.430,"geometry_index":429,"location":[29.702257,40.890359]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"bearings":[90,265,270],"duration":15.065,"turn_duration":0.007,"admin_index":0,"out":0,"weight":13.176,"geometry_index":435,"location":[29.722377,40.889443]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[99,274],"duration":29.112,"admin_index":0,"out":0,"weight":25.473,"geometry_index":437,"location":[29.727537,40.889237]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[103,280],"duration":25.719,"admin_index":0,"out":0,"weight":23.147,"geometry_index":439,"location":[29.737395,40.887992]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[124,127,303],"duration":57.330,"turn_duration":0.007,"admin_index":0,"out":0,"weight":53.024,"geometry_index":446,"location":[29.745268,40.885079]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[122,303],"duration":1.835,"admin_index":0,"out":0,"weight":1.697,"geometry_index":459,"location":[29.760968,40.876135]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[121,302],"duration":40.915,"admin_index":0,"out":0,"weight":37.847,"geometry_index":460,"location":[29.761497,40.875882]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[111,291],"duration":1.419,"admin_index":0,"out":0,"weight":1.313,"geometry_index":466,"location":[29.774040,40.871148]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[110,291],"duration":15.646,"admin_index":0,"out":0,"weight":14.473,"geometry_index":467,"location":[29.774492,40.871014]},{"entry":[true,true,false,false],"classes":["toll","motorway"],"in":3,"bearings":[104,127,273,287],"duration":60.980,"turn_weight":1.500,"turn_duration":0.022,"admin_index":0,"out":0,"weight":56.362,"geometry_index":470,"location":[29.779591,40.869735]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[97,282],"duration":34.581,"admin_index":0,"out":0,"weight":30.258,"geometry_index":478,"location":[29.799730,40.865540]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[96,276],"duration":107.308,"admin_index":0,"out":0,"weight":93.894,"geometry_index":480,"location":[29.811514,40.864511]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[101,280],"duration":28.108,"admin_index":0,"out":0,"weight":24.594,"geometry_index":492,"location":[29.848169,40.862522]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[102,281],"duration":52.062,"admin_index":0,"out":0,"weight":45.554,"geometry_index":493,"location":[29.857653,40.861181]},{"entry":[true,false],"classes":["tunnel","toll","motorway"],"in":1,"bearings":[112,289],"duration":15.819,"admin_index":0,"out":0,"weight":13.842,"geometry_index":500,"location":[29.874912,40.857786]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[21,112,292],"duration":15.792,"turn_duration":0.008,"admin_index":0,"out":1,"weight":13.812,"geometry_index":501,"location":[29.879951,40.856262]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[21,112,292],"duration":16.796,"turn_duration":0.008,"admin_index":0,"out":1,"weight":14.690,"geometry_index":502,"location":[29.884983,40.854741]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[112,292],"duration":52.304,"admin_index":0,"out":0,"weight":45.766,"geometry_index":503,"location":[29.890335,40.853122]},{"entry":[true,false],"classes":["tunnel","toll","motorway"],"in":1,"bearings":[97,281],"duration":33.577,"admin_index":0,"out":0,"weight":29.380,"geometry_index":509,"location":[29.907497,40.849311]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[8,102,275],"duration":89.110,"turn_duration":0.010,"admin_index":0,"out":1,"weight":77.962,"geometry_index":511,"location":[29.918967,40.848532]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[37,123,303],"duration":7.358,"turn_duration":0.019,"admin_index":0,"out":1,"weight":6.421,"geometry_index":515,"location":[29.946515,40.839117]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[37,123,303],"duration":7.358,"turn_duration":0.019,"admin_index":0,"out":1,"weight":6.421,"geometry_index":516,"location":[29.948636,40.838083]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[37,123,303],"duration":9.019,"turn_duration":0.019,"admin_index":0,"out":1,"weight":7.875,"geometry_index":517,"location":[29.950756,40.837053]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[124,303],"duration":2.977,"admin_index":0,"out":0,"weight":2.605,"geometry_index":518,"location":[29.953358,40.835789]},{"entry":[true,false],"classes":["tunnel","toll","motorway"],"in":1,"bearings":[121,304],"duration":13.708,"admin_index":0,"out":0,"weight":11.994,"geometry_index":519,"location":[29.954206,40.835359]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[117,301],"duration":3.704,"admin_index":0,"out":0,"weight":3.333,"geometry_index":520,"location":[29.958214,40.833502]},{"entry":[true,false],"classes":["tunnel","toll","motorway"],"in":1,"bearings":[114,297],"duration":8.723,"admin_index":0,"out":0,"weight":7.851,"geometry_index":521,"location":[29.959348,40.833067]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[25,110,294],"duration":8.920,"turn_duration":0.024,"admin_index":0,"out":1,"weight":8.007,"geometry_index":522,"location":[29.962075,40.832129]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[23,104,290],"duration":9.963,"turn_duration":0.028,"admin_index":0,"out":1,"weight":8.941,"geometry_index":523,"location":[29.964940,40.831345]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[15,104,284],"duration":10.219,"turn_duration":0.007,"admin_index":0,"out":1,"weight":9.190,"geometry_index":524,"location":[29.968250,40.830738]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[15,104,284],"duration":16.830,"turn_duration":0.007,"admin_index":0,"out":1,"weight":15.141,"geometry_index":525,"location":[29.971651,40.830116]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[13,100,280],"duration":22.403,"turn_duration":0.007,"admin_index":0,"out":1,"weight":20.156,"geometry_index":527,"location":[29.977290,40.829221]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[97,281],"duration":7.511,"admin_index":0,"out":0,"weight":6.760,"geometry_index":529,"location":[29.984841,40.828171]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[99,284],"duration":26.308,"admin_index":0,"out":0,"weight":24.335,"geometry_index":532,"location":[29.987365,40.827811]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[134,317],"duration":2.804,"admin_index":0,"out":0,"weight":2.594,"geometry_index":545,"location":[29.995258,40.824790]},{"entry":[true,false],"classes":["tunnel","toll","motorway"],"in":1,"bearings":[135,314],"duration":17.481,"admin_index":0,"out":0,"weight":16.170,"geometry_index":546,"location":[29.995951,40.824280]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[56,151,328],"duration":9.355,"turn_duration":0.008,"admin_index":0,"out":1,"weight":8.645,"geometry_index":549,"location":[29.999837,40.820850]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[65,156,335],"duration":11.950,"turn_duration":0.007,"admin_index":0,"out":1,"weight":11.047,"geometry_index":551,"location":[30.001294,40.818686]},{"entry":[false,true,false],"classes":["tunnel","toll","motorway"],"in":2,"bearings":[63,156,336],"duration":11.292,"turn_duration":0.007,"admin_index":0,"out":1,"weight":10.438,"geometry_index":553,"location":[30.002968,40.815859]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[154,336],"duration":19.073,"admin_index":0,"out":0,"weight":17.643,"geometry_index":554,"location":[30.004555,40.813190]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[153,169,334],"duration":5.073,"turn_duration":0.019,"admin_index":0,"out":0,"weight":4.675,"geometry_index":557,"location":[30.007443,40.808750]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"bearings":[150,320,332],"duration":2.235,"turn_duration":0.019,"admin_index":0,"out":0,"weight":2.049,"geometry_index":559,"location":[30.008265,40.807591]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[147,330],"duration":27.173,"admin_index":0,"out":0,"weight":25.135,"geometry_index":560,"location":[30.008643,40.807097]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[144,325],"duration":3.808,"admin_index":0,"out":0,"weight":3.617,"geometry_index":566,"location":[30.013940,40.801296]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[141,148,324],"duration":48.069,"turn_duration":0.022,"admin_index":0,"out":0,"weight":45.644,"geometry_index":567,"location":[30.014707,40.800492]},{"bearings":[107,284,290],"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"turn_weight":1.000,"turn_duration":0.022,"admin_index":0,"out":0,"geometry_index":582,"location":[30.026851,40.792328]}],"bannerInstructions":[{"primary":{"type":"fork","modifier":"slight left","text":"O-7; E-80; AH1; AH5","components":[{"text":"O-7; E-80; AH1; AH5","type":"text"}]},"distanceAlongGeometry":43956.000}],"destinations":"E-80: KOCAELİ","maneuver":{"modifier":"slight right","instruction":"O-7 caddesinde kalmak için KOCAELİ işaretine doğru sağda kalın.","type":"fork","bearing_after":48,"bearing_before":40,"location":[29.556078,40.887692]},"name":"","duration":1521.881,"distance":43956.000,"driving_side":"right","weight":1366.245,"mode":"driving","ref":"O-7","geometry":"wwq~lA{u}jw@}JgPcfAcmAqZc]cKmMaKeO{HiN}HsOqGuPgGmRkLmh@qDiWsCqZ_Bi[McSIoOb@wTdAoTtBcVdFi^jE{TrJgc@hO_cAdXcwAzPkjAdUecBtMunAlOumBrF{|@bCmr@|DgoAnBspA|@ylAq@wiAeAcjA_DglAaCiu@gDit@eE{s@kF{t@gIu`AcKs`Am_@y`DiUynBq]eyCkKs_Agf@meEce@kcEwSwoBgJa{@wLutAqWm~CiSq_D}Rg|CkNwnBiL{kBkSerCoKsiAiRy~AmSeaB{b@gaDmQolAaZmlBqSwrA}S_qAcWcuA_^y|B{Lgv@yI_|@kImmAaLu}AuI_fB_Gg}A{Bsu@m@cz@Z{w@j@whAj@ckAbAwq@`Dcs@`M}eC~Nq{At]g|CrN{iAdQo~@l]qgBd`@{iCtc@w{BrmC{pOb`A_wFdj@e`Eja@muEfOkjC~DyaBpFqhExDsxApEekBpByrAzDmlDfGi~DfCyxA~KqmC~UugHdLyvErKqsIeCeuDKunAIedAdLi|Fjb@_eHli@caIrIq}@xZiiBrNcn@vQaj@|o@weBfh@c{A`Rce@t|AsuDfhAgxBpUgh@jf@u}@jf@e_Avf@s{@`f@q|@td@i`Alr@gyAhd@q`Alb@maAfd@ahAxKmYxNa`@b_A_kCl}@aqCp|@afD`e@akBbg@wxBp\\_dBjGg[pi@{{CzBiMn`@orC~XsiC~c@}vDrYmmB~b@k|Cf{@gxErh@c~E`Q}wBrO_jBbc@k|Jd[cbJvRelGzEczA|IetDxIw~C~Ay}ALyhKsBipE|AmeDjDecCdLuvCzSkpDv`@agGxrAwoQ|c@qhFzVojDfR{oBv\\mwCp^{eC`i@}_Dv[ooBf~A}yH`~AoyHddBomIb`BydIdl@{cDjQqsAde@ssE`XmxDfMibBrD{y@`j@_qSvy@qoJ~rAevJ|aEoqMvyFo~Nr_AqcCj_AocC~mAsaDzY_t@`sBoyFdZ{eAry@miD~o@arD|d@{mEze@qsEl_@epDnVgnDla@s_Gd^ivEtF_rAnFyc@hF}d@fDsi@nEwh@`Gef@dG}e@xJql@|Kog@fOaf@~Pwd@xXws@xT{e@lPy[zOuUzRyWz^ij@rrAknB~gAirAvx@eo@ddAyp@`aAgh@rcAkg@`kBg_AxeDebBjmCmyAjbAml@vbAsk@zCcBpbAgo@z]sV|bAcy@|{@cw@vz@ux@z}@ix@v_Aoy@hm@ij@fq@}n@hg@_j@dd@_i@rjAcvAnu@aw@zcAwmAfu@_gAbVqb@jVsc@vQc_@nY{o@hT_l@dU_r@nReq@fLsf@nJah@|I_n@tHim@lDi]rDk^tEwe@tFqu@bBuXrAeV"},{"bannerInstructions":[{"secondary":{"text":"Adapazarı","components":[{"text":"Adapazarı","type":"text"}]},"primary":{"type":"off ramp","modifier":"slight right","text":"Adapazarı Köprülü Kavşağı","components":[{"text":"Exit","type":"exit"},{"text":"K-27","type":"exit-number"},{"text":"Adapazarı Köprülü Kavşağı","type":"text"}]},"distanceAlongGeometry":28341.998}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>28kilometre boyunca devam edin.<\/speak>","announcement":"28kilometre boyunca devam edin.","distanceAlongGeometry":28332.0},{"ssmlAnnouncement":"<speak>1 kilometre sonunda, K-27 çıkıştan çıkın.<\/speak>","announcement":"1 kilometre sonunda, K-27 çıkıştan çıkın.","distanceAlongGeometry":981.9},{"ssmlAnnouncement":"<speak>K-27 çıkıştan çıkın.<\/speak>","announcement":"K-27 çıkıştan çıkın.","distanceAlongGeometry":280.6}],"intersections":[{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[96,99,279],"duration":27.092,"turn_duration":0.022,"admin_index":0,"out":0,"weight":25.716,"geometry_index":590,"location":[30.031606,40.791499]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[72,253],"duration":5.227,"admin_index":0,"out":0,"weight":4.966,"geometry_index":610,"location":[30.040794,40.791998]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[71,252],"duration":19.938,"admin_index":0,"out":0,"weight":18.443,"geometry_index":611,"location":[30.042506,40.792410]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"bearings":[71,245,251],"duration":64.715,"turn_weight":1.500,"turn_duration":0.019,"admin_index":0,"out":0,"weight":61.344,"geometry_index":613,"location":[30.048986,40.794059]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[99,103,277],"duration":38.742,"turn_duration":0.008,"admin_index":0,"out":0,"weight":34.861,"geometry_index":638,"location":[30.070679,40.796191]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"bearings":[101,280,283],"duration":5.905,"turn_duration":0.021,"admin_index":0,"out":0,"weight":5.296,"geometry_index":648,"location":[30.083647,40.794062]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[99,281],"duration":15.058,"admin_index":0,"out":0,"weight":13.552,"geometry_index":649,"location":[30.085622,40.793761]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[97,279],"duration":402.577,"admin_index":0,"out":0,"weight":362.319,"geometry_index":652,"location":[30.090721,40.793176]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[46,220],"duration":31.085,"admin_index":0,"out":0,"weight":28.753,"geometry_index":761,"location":[30.203038,40.836913]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[57,235],"duration":18.519,"admin_index":0,"out":0,"weight":17.130,"geometry_index":765,"location":[30.211180,40.842105]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[70,76,242],"duration":19.189,"turn_duration":0.012,"admin_index":0,"out":0,"weight":17.259,"geometry_index":767,"location":[30.216658,40.844529]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"bearings":[76,244,256],"duration":157.440,"turn_duration":0.007,"admin_index":0,"out":0,"weight":145.626,"geometry_index":769,"location":[30.222869,40.846169]},{"entry":[true,true,false],"classes":["toll","motorway"],"in":2,"bearings":[94,100,274],"duration":37.986,"turn_duration":0.019,"admin_index":1,"out":0,"weight":36.068,"geometry_index":780,"location":[30.278655,40.844448]},{"entry":[true,false,false],"classes":["toll","motorway"],"in":2,"bearings":[94,269,274],"duration":50.252,"turn_duration":0.019,"admin_index":1,"out":0,"weight":47.722,"geometry_index":782,"location":[30.292157,40.843781]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[88,269],"duration":0.700,"admin_index":1,"out":0,"weight":0.665,"geometry_index":789,"location":[30.310024,40.843079]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[88,268],"duration":21.333,"admin_index":1,"out":0,"weight":20.800,"geometry_index":790,"location":[30.310279,40.843086]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[82,263],"duration":1.319,"admin_index":1,"out":0,"weight":1.319,"geometry_index":796,"location":[30.317844,40.843582]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[83,262],"duration":29.442,"admin_index":1,"out":0,"weight":29.442,"geometry_index":797,"location":[30.318277,40.843626]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[91,269],"duration":1.568,"admin_index":1,"out":0,"weight":1.568,"geometry_index":800,"location":[30.328053,40.844142]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[91,271],"duration":3.564,"admin_index":1,"out":0,"weight":3.564,"geometry_index":801,"location":[30.328574,40.844138]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[92,271],"duration":11.477,"admin_index":1,"out":0,"weight":11.477,"geometry_index":802,"location":[30.329758,40.844116]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[96,275],"duration":6.451,"admin_index":1,"out":0,"weight":6.613,"geometry_index":805,"location":[30.333577,40.843920]},{"entry":[true,false],"classes":["toll","motorway"],"in":1,"bearings":[98,277],"duration":1.354,"admin_index":1,"out":0,"weight":1.388,"geometry_index":807,"location":[30.335715,40.843728]},{"bearings":[98,278],"entry":[true,false],"classes":["toll","motorway"],"in":1,"admin_index":1,"out":0,"geometry_index":808,"location":[30.336156,40.843683]}],"maneuver":{"modifier":"slight left","instruction":"O-7\/E-80\/AH1\/AH5 caddesine girmek için solda kalın.","type":"fork","bearing_after":96,"bearing_before":99,"location":[30.031606,40.791499]},"name":"","duration":972.539,"distance":28341.998,"driving_side":"right","weight":897.537,"mode":"driving","ref":"O-7; E-80; AH1; AH5","geometry":"u{uxlAkn~gx@dAkZtAu\\~@g_@ZmWZi`@Ru^Gs]]s^o@ig@u@g^g@_Ss@cTgA{UyAyYkCe`@_Ca[kCg\\qEkc@}CqZ{BmOwX_jBoUowAqoAo{HcRahAiM{_AyYwkBgIam@oOkdAgFm]gMq}@oKy{@oH_w@}Fuw@cDsh@uBeb@sCip@sBsg@yA}g@m@wp@IarABcs@h@uk@z@sm@zB}r@~DqaAdCcd@nCyc@|Cyq@`HmnAzH}`AdF}h@nKghAlRwaBpS_bB|I_}@tQwgCjJ_iAlLmnAxQmzBvL_pBlIiaBjKkjBnLgmCpLkcCrLs}BnM{tCxJ{dCrEaeBxEicAhH_}BzA{f@lCke@bEqh@lF_p@hDaf@`Dyh@vAog@dB_ZxBcQ~Fy[nIwf@lGia@zDm]dDoc@rH}aAdDs^nMap@rTgvAhE{\\zDq`@`BkWjBga@zBcj@~Agi@`Aqh@Ly]Doh@mA}lAw@w]cBu]uEoz@qHggA_Hkq@kF}c@}Him@gEeVqCgO}EgSeJkZmJ}Y_My^wLu]{Ko[kOu`@wNu]gIePyRm`@qZ_l@weAemB{t@irAmn@whAgr@{qAwj@gmAm^m|@eb@geAsVus@yOif@{Ugw@cOcm@yUs~@uS}|@mTkiAaK{k@wi@ixCk]iiBgSghAyQcz@iMgl@_N_h@uLwa@_Smr@gJiZgMqa@a[o{@w]a~@aoAe_DuZkw@u^my@}yBwrFy|@sxB{~@{xBqd@}jAag@ymAeaAe}Be[sn@{\\{l@_`AwwAig@wq@sh@ao@am@aq@my@mz@ioBo~AauBiaBqz@sn@_xEavD_fDylCijA_~@ucA}}@kxC}|BgmCynCgw@o}@eu@ciAkuBkgD{mAq`CahAygC}qAkjDqcA_jDqaB_nJ}BeT{o@ahGiIuvAyBuvA{BigD{@_dAt@ebA|CmoBfGgfCngBgbm@xc@_bRbSwvKh`@kbTjGogDhVqqMtKmwFdE{lC|@kbARwdACagAWsq@M}NSsT_B{zAyC{|AgGmkBeF}rAcFahAwAaZyPszDcI{yCiCokGFq_@j@_iAt@}y@~B}kApEyeBzC}y@bFuiAxAqZlBg`@"},{"voiceInstructions":[{"ssmlAnnouncement":"<speak>500 metre sonunda, Adapazarı Köprülü Kavşağı caddesinde kalmak için solda kalın.<\/speak>","announcement":"500 metre sonunda, Adapazarı Köprülü Kavşağı caddesinde kalmak için solda kalın.","distanceAlongGeometry":494.3},{"ssmlAnnouncement":"<speak>Adapazarı Köprülü Kavşağı caddesinde kalmak için solda kalın.<\/speak>","announcement":"Adapazarı Köprülü Kavşağı caddesinde kalmak için solda kalın.","distanceAlongGeometry":115.7}],"intersections":[{"entry":[true,true,false],"classes":["motorway"],"in":2,"bearings":[99,105,278],"duration":20.723,"turn_duration":0.013,"admin_index":1,"out":1,"weight":21.228,"geometry_index":809,"location":[30.336688,40.843628]},{"bearings":[103,283],"entry":[true,false],"classes":["motorway"],"in":1,"turn_duration":15.000,"turn_weight":15.000,"toll_collection":{"type":"toll_booth"},"admin_index":1,"out":0,"geometry_index":814,"location":[30.342591,40.842525]}],"exits":"K-27","bannerInstructions":[{"primary":{"type":"fork","modifier":"slight left","text":"Adapazarı Köprülü Kavşağı","components":[{"text":"Adapazarı Köprülü Kavşağı","type":"text"}]},"distanceAlongGeometry":838.000}],"destinations":"Adapazarı","maneuver":{"modifier":"slight right","instruction":"K-27 çıkıştan çıkın.","type":"off ramp","bearing_after":105,"bearing_before":98,"location":[30.336688,40.843628]},"name":"Adapazarı Köprülü Kavşağı","duration":48.909,"distance":838.000,"driving_side":"right","weight":49.744,"mode":"driving","geometry":"wu{{lA_jrzx@~KaaAvCo_@xPidBbU{iBfJe}@|AkOvf@uyE"},{"bannerInstructions":[{"primary":{"type":"fork","modifier":"slight right","text":"Adapazarı Köprülü Kavşağı","components":[{"text":"Adapazarı Köprülü Kavşağı","type":"text"}]},"distanceAlongGeometry":609.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>Adapazarı Köprülü Kavşağı caddesine girmek için sağda kalın.<\/speak>","announcement":"Adapazarı Köprülü Kavşağı caddesine girmek için sağda kalın.","distanceAlongGeometry":247.0}],"intersections":[{"bearings":[101,110,284],"entry":[true,true,false],"classes":["motorway"],"in":2,"turn_duration":0.022,"admin_index":1,"out":0,"geometry_index":816,"location":[30.346352,40.841842]}],"maneuver":{"modifier":"slight left","instruction":"Adapazarı Köprülü Kavşağı caddesinde kalmak için solda kalın.","type":"fork","bearing_after":101,"bearing_before":104,"location":[30.346352,40.841842]},"name":"Adapazarı Köprülü Kavşağı","duration":24.656,"distance":609.000,"driving_side":"right","weight":24.634,"mode":"driving","geometry":"cfx{lA_fe{x@tCi_@hl@iiFp^ekD"},{"bannerInstructions":[{"primary":{"type":"new name","modifier":"straight","text":"Devam edin.","components":[{"text":"Devam edin.","type":"text"}]},"distanceAlongGeometry":707.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>400 metre sonunda, Decam edin.<\/speak>","announcement":"400 metre sonunda, Decam edin.","distanceAlongGeometry":391.3},{"ssmlAnnouncement":"<speak>Devam edin.<\/speak>","announcement":"Devam edin.","distanceAlongGeometry":141.3}],"intersections":[{"entry":[true,true,false,false],"in":3,"bearings":[101,106,282,284],"duration":62.308,"turn_weight":2.400,"turn_duration":0.008,"admin_index":1,"out":1,"weight":64.700,"geometry_index":819,"location":[30.353373,40.840537]},{"bearings":[19,195,201],"entry":[true,false,false],"in":1,"turn_weight":14.600,"turn_duration":0.008,"admin_index":1,"out":0,"geometry_index":843,"location":[30.353443,40.840407]}],"maneuver":{"modifier":"slight right","instruction":"Adapazarı Köprülü Kavşağı caddesine girmek için sağda kalın.","type":"fork","bearing_after":106,"bearing_before":104,"location":[30.353373,40.840537]},"name":"Adapazarı Köprülü Kavşağı","duration":66.575,"distance":707.000,"driving_side":"right","weight":83.559,"mode":"driving","geometry":"qtu{lAy|r{x@bP}lAlIa[zDsF~F}FhH_DlIyAbHZ`HhAdNdJ~GvJvDzKlDbTNdPgA|OoCnLoEvJ{GrJeIzEiEtAuEj@iJS_QyDsPgD{MkDuk@yR"},{"bannerInstructions":[{"secondary":{"text":"Karasu Caddesi","components":[{"text":"Karasu Caddesi","type":"text"}]},"primary":{"type":"fork","modifier":"slight left","text":"Karasu Caddesi","components":[{"text":"Karasu Caddesi","type":"text"}]},"distanceAlongGeometry":712.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>Karasu Caddesi caddesine girmek için solda kalın.<\/speak>","announcement":"Karasu Caddesi caddesine girmek için solda kalın.","distanceAlongGeometry":197.0}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[18,23,199],"duration":28.515,"turn_weight":5.000,"turn_duration":0.019,"admin_index":1,"out":0,"weight":33.496,"geometry_index":844,"location":[30.353760,40.841122]},{"bearings":[26,197,204],"entry":[true,false,false],"in":2,"turn_weight":1.800,"turn_duration":0.007,"admin_index":1,"out":0,"geometry_index":851,"location":[30.356049,40.845862]}],"maneuver":{"modifier":"straight","instruction":"Devam edin.","type":"new name","bearing_after":18,"bearing_before":19,"location":[30.353760,40.841122]},"name":"","duration":36.128,"distance":712.000,"driving_side":"right","weight":42.901,"mode":"driving","geometry":"cyv{lA_us{x@kf@kPo\\uKy[sLor@mWciAmb@od@sQmb@{SakAko@"},{"voiceInstructions":[{"ssmlAnnouncement":"<speak>5kilometre boyunca devam edin.<\/speak>","announcement":"5kilometre boyunca devam edin.","distanceAlongGeometry":5378.0},{"ssmlAnnouncement":"<speak>900 metre sonunda, Döner kavşağa girin ve Üçüncü çıkıştan D020 caddesine doğru çıkın.<\/speak>","announcement":"900 metre sonunda, Döner kavşağa girin ve Üçüncü çıkıştan D020 caddesine doğru çıkın.","distanceAlongGeometry":854.8},{"ssmlAnnouncement":"<speak>Döner kavşağa girin ve Üçüncü çıkıştan D020 caddesine doğru çıkın.<\/speak>","announcement":"Döner kavşağa girin ve Üçüncü çıkıştan D020 caddesine doğru çıkın.","distanceAlongGeometry":244.4}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[24,32,206],"duration":53.421,"turn_duration":0.021,"admin_index":1,"out":0,"weight":53.400,"geometry_index":852,"location":[30.356823,40.847079]},{"entry":[true,false],"in":1,"bearings":[57,229],"duration":5.300,"admin_index":1,"out":0,"weight":5.300,"geometry_index":857,"location":[30.359698,40.851324]},{"entry":[true,false],"in":1,"bearings":[82,251],"duration":64.700,"admin_index":1,"out":0,"weight":64.700,"geometry_index":859,"location":[30.360255,40.851538]},{"entry":[true,false,false],"in":1,"bearings":[7,180,191],"duration":7.566,"turn_weight":14.600,"turn_duration":0.011,"admin_index":1,"out":0,"weight":22.155,"geometry_index":881,"location":[30.360029,40.851218]},{"entry":[true,true,false],"in":2,"bearings":[17,52,195],"duration":4.824,"turn_duration":0.007,"admin_index":1,"out":0,"weight":4.817,"geometry_index":884,"location":[30.360375,40.852533]},{"entry":[true,false],"in":1,"bearings":[24,196],"duration":18.152,"turn_weight":5.000,"admin_index":1,"out":0,"weight":23.152,"geometry_index":886,"location":[30.360697,40.853354]},{"entry":[false,true],"in":0,"bearings":[164,338],"duration":19.318,"admin_index":1,"out":1,"weight":18.835,"geometry_index":897,"location":[30.361395,40.856425]},{"entry":[false,true,true],"in":0,"bearings":[131,311,334],"duration":1.930,"turn_duration":0.007,"admin_index":1,"out":1,"weight":1.875,"geometry_index":908,"location":[30.358402,40.858941]},{"entry":[true,false,false,true],"in":1,"bearings":[49,131,211,308],"duration":1.168,"turn_duration":0.022,"admin_index":1,"out":3,"weight":1.117,"geometry_index":909,"location":[30.357983,40.859214]},{"entry":[false,false,true,true],"in":1,"bearings":[61,128,227,308],"duration":2.584,"turn_duration":0.007,"admin_index":1,"out":3,"weight":2.513,"geometry_index":910,"location":[30.357719,40.859371]},{"entry":[false,false,true],"in":1,"bearings":[113,125,303],"duration":27.716,"turn_duration":0.021,"admin_index":1,"out":2,"weight":27.003,"geometry_index":912,"location":[30.357120,40.859707]},{"entry":[true,false,true],"in":1,"bearings":[90,173,349],"duration":6.610,"turn_duration":0.024,"admin_index":1,"out":2,"weight":6.422,"geometry_index":922,"location":[30.353170,40.864624]},{"entry":[true,false,true,true],"in":1,"bearings":[21,159,329,339],"duration":23.397,"turn_duration":0.038,"admin_index":1,"out":2,"weight":22.775,"geometry_index":925,"location":[30.352645,40.866010]},{"entry":[false,false,true],"in":1,"bearings":[158,164,343],"duration":12.648,"turn_duration":0.007,"admin_index":1,"out":2,"weight":12.009,"geometry_index":934,"location":[30.349886,40.870663]},{"entry":[true,false,true],"in":1,"bearings":[79,163,346],"duration":15.799,"turn_duration":0.008,"admin_index":1,"out":2,"weight":14.606,"geometry_index":936,"location":[30.348810,40.873315]},{"entry":[true,false,true],"in":1,"bearings":[76,165,340],"duration":10.415,"turn_duration":0.024,"admin_index":1,"out":2,"weight":9.612,"geometry_index":939,"location":[30.347520,40.876639]},{"entry":[true,false,true],"in":1,"bearings":[27,170,357],"duration":13.021,"turn_duration":0.012,"admin_index":1,"out":2,"weight":12.033,"geometry_index":942,"location":[30.346699,40.878832]},{"entry":[true,false],"in":1,"bearings":[16,194],"duration":2.618,"admin_index":1,"out":0,"weight":2.422,"geometry_index":945,"location":[30.347095,40.881651]},{"bearings":[15,196],"entry":[true,false],"in":1,"turn_weight":5.000,"admin_index":1,"out":0,"geometry_index":946,"location":[30.347298,40.882203]}],"bannerInstructions":[{"primary":{"degrees":9,"driving_side":"right","type":"roundabout","modifier":"straight","text":"D020","components":[{"text":"D020","type":"text"}]},"distanceAlongGeometry":5388.000}],"destinations":"Karasu Caddesi","maneuver":{"modifier":"slight left","instruction":"Karasu Caddesi caddesine girmek için solda kalın.","type":"fork","bearing_after":24,"bearing_before":26,"location":[30.356823,40.847079]},"name":"Karasu Caddesi","duration":303.176,"distance":5388.000,"driving_side":"right","weight":320.833,"mode":"driving","geometry":"mmb|lAmty{x@{uCsyAgnAor@cK_IiNkNwFeKgIuScBcMo@yH[sPr@_RzCqRrD}LnIsOfIwIdPkIjMoCbK?fMv@vLrFxJ|IdHxJzC|JpBbS_@rRaD~PyItMcOnK{WfFwt@@q[{CqTuDa_@aJmJuC{f@mNoiA{i@}\\oImKkAaLa@yKDiFZ{Eb@sHdA}HxA{FdBqFxAgFlBeFlC_MxHeMhJwJjJsIrKkGrJgWd^wKdRyVlc@aVrb@aPdYyHnO{IlQcI~Q}DlKs}@deBmg@tx@y_@hZuWzLkf@hOch@|LiYUiP^uOxA_TpDw`@jJ{^zN{[`Vo\\nXeZ|U}VxNaOhIyi@zV}a@zNei@jPil@tPuxAjc@akAz]an@~MkyBzr@ie@vLyWzJmwAx`@yVrD}}@nCqf@cDuhAcWoa@uKseAmXol@_R_^mPeH}C"},{"bannerInstructions":[{"primary":{"degrees":9,"driving_side":"right","type":"exit roundabout","modifier":"slight right","text":"D020","components":[{"text":"D020","type":"text"}]},"distanceAlongGeometry":75.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>D020 caddesine doğru döner kavşaktan çıkın.<\/speak>","announcement":"D020 caddesine doğru döner kavşaktan çıkın.","distanceAlongGeometry":75.0}],"intersections":[{"entry":[true,false,false],"in":1,"bearings":[18,202,255],"duration":0.591,"turn_weight":5.500,"turn_duration":0.022,"admin_index":1,"out":0,"weight":6.026,"geometry_index":950,"location":[30.348367,40.884704]},{"entry":[true,false,true],"in":1,"bearings":[8,198,352],"duration":0.331,"turn_duration":0.142,"admin_index":1,"out":2,"weight":0.175,"geometry_index":952,"location":[30.348410,40.884805]},{"entry":[false,true],"in":0,"bearings":[172,307],"duration":1.516,"admin_index":1,"out":1,"weight":1.402,"geometry_index":953,"location":[30.348403,40.884841]},{"entry":[false,false,true,true],"in":1,"bearings":[17,103,207,270],"duration":4.566,"turn_duration":3.477,"admin_index":1,"out":2,"weight":1.008,"geometry_index":959,"location":[30.348101,40.884947]},{"bearings":[27,163],"entry":[false,true],"in":0,"admin_index":1,"out":1,"geometry_index":964,"location":[30.347984,40.884773]}],"maneuver":{"exit":3,"modifier":"straight","instruction":"Döner kavşağa girin ve Üçüncü çıkıştan D020 caddesine doğru çıkın.","type":"roundabout","bearing_after":18,"bearing_before":22,"location":[30.348367,40.884704]},"name":"","duration":7.193,"distance":75.000,"driving_side":"right","weight":8.787,"mode":"driving","ref":"D020","geometry":"_}k~lA}ci{x@_B_AiBUgALeAb@oAlAy@hB_@|BAbC\\|Bn@~A~@hAlAp@lARnAEjA]"},{"bannerInstructions":[{"primary":{"type":"turn","modifier":"right","text":"sağa dönün.","components":[{"text":"sağa dönün.","type":"text"}]},"distanceAlongGeometry":608.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>sağa dönün.<\/speak>","announcement":"sağa dönün.","distanceAlongGeometry":244.4}],"intersections":[{"entry":[true,true,false,false],"in":3,"bearings":[117,193,276,343],"duration":12.730,"turn_weight":5.000,"turn_duration":0.048,"admin_index":1,"out":1,"weight":16.730,"geometry_index":965,"location":[30.347999,40.884735]},{"entry":[false,true],"in":0,"bearings":[17,195],"duration":2.741,"turn_weight":5.000,"admin_index":1,"out":1,"weight":7.535,"geometry_index":969,"location":[30.347062,40.882047]},{"bearings":[15,195],"entry":[false,true],"in":0,"admin_index":1,"out":1,"geometry_index":970,"location":[30.346863,40.881469]}],"maneuver":{"modifier":"slight right","instruction":"D020 caddesine doğru döner kavşaktan çıkın.","type":"exit roundabout","bearing_after":193,"bearing_before":163,"location":[30.347999,40.884735]},"name":"","duration":24.921,"distance":608.000,"driving_side":"right","weight":33.007,"mode":"driving","ref":"D020","geometry":"}~k~lA}lh{x@fDp@pa@dIbc@dI`{Arc@bc@lKbp@bPbj@dGlb@m@"},{"bannerInstructions":[{"primary":{"type":"turn","modifier":"left","text":"sola dönün.","components":[{"text":"sola dönün.","type":"text"}]},"distanceAlongGeometry":211.000}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>200 metre sonunda, sola dönün.<\/speak>","announcement":"200 metre sonunda, sola dönün.","distanceAlongGeometry":190.5},{"ssmlAnnouncement":"<speak>sola dönün.<\/speak>","announcement":"sola dönün.","distanceAlongGeometry":63.9}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[172,305,358],"duration":25.035,"turn_duration":6.253,"admin_index":1,"out":1,"weight":17.374,"geometry_index":973,"location":[30.346481,40.879426]},{"entry":[false,true],"in":0,"bearings":[114,293],"duration":6.104,"turn_weight":1.000,"admin_index":1,"out":1,"weight":6.646,"geometry_index":977,"location":[30.345326,40.880038]},{"bearings":[113,311],"entry":[false,true],"in":0,"turn_weight":1.500,"admin_index":1,"out":1,"geometry_index":978,"location":[30.344904,40.880176]}],"maneuver":{"modifier":"right","instruction":"sağa dönün.","type":"turn","bearing_after":305,"bearing_before":178,"location":[30.346481,40.879426]},"name":"","duration":39.279,"distance":211.000,"driving_side":"right","weight":33.049,"mode":"driving","geometry":"csa~lAane{x@uFnMsI`N_JfO}GjYsGjYiJhPqHlH"},{"bannerInstructions":[{"primary":{"type":"arrive","modifier":"left","text":"Hedefiniz solda.","components":[{"text":"Hedefiniz solda.","type":"text"}]},"distanceAlongGeometry":402.187}],"voiceInstructions":[{"ssmlAnnouncement":"<speak>400 metre boyunca devam edin.<\/speak>","announcement":"400 metre boyunca devam edin.","distanceAlongGeometry":392.2},{"ssmlAnnouncement":"<speak>200 metre sonunda, Hedefiniz solda kalacak.<\/speak>","announcement":"200 metre sonunda, Hedefiniz solda kalacak.","distanceAlongGeometry":202.3},{"ssmlAnnouncement":"<speak>Hedefiniz solda.<\/speak>","announcement":"Hedefiniz solda.","distanceAlongGeometry":57.8}],"intersections":[{"bearings":[143,220,270,349],"entry":[false,true,true,true],"in":0,"turn_weight":7.500,"turn_duration":6.628,"admin_index":1,"out":1,"geometry_index":980,"location":[30.344476,40.880510]}],"maneuver":{"modifier":"left","instruction":"sola dönün.","type":"turn","bearing_after":220,"bearing_before":323,"location":[30.344476,40.880510]},"name":"","duration":69.579,"distance":402.187,"driving_side":"right","weight":65.729,"mode":"driving","geometry":"{vc~lAwpa{x@bFpHxQnBrL\\|HnDpQvM`OlRnWp^r]f\\~TrS|XjS"},{"intersections":[{"bearings":[31],"entry":[true],"in":0,"admin_index":1,"geometry_index":990,"location":[30.341988,40.877515]}],"voiceInstructions":[],"bannerInstructions":[],"maneuver":{"modifier":"left","instruction":"Hedefiniz solda.","type":"arrive","bearing_after":0,"bearing_before":211,"location":[30.341988,40.877515]},"name":"","duration":0.000,"distance":0.000,"driving_side":"right","weight":0.000,"mode":"driving","geometry":"u{}}lAgu|zx@??"}],"distance":102255.922,"summary":"O-7, Karasu Caddesi"}],"geometry":"_rvzlAg`~ew@uPeYkQ_ZmNuZsXup@iNmb@iB|IvDvLpVfo@vE~JtDbI~Qz]nq@lkAjg@t|@zFvN~Qpc@t[``AzZh~@\\vMi@vJmA`JmAbECrEb@nErAtDzBhCtClA~IxCzKdGfQ|Lbi@zc@t\\nYxY`W`NpKpj@lc@zC`CxVpTnPxNppAjfAfx@hp@xRbUdO`RfLpShJhRbB`D|Ypr@jClG~]zw@nZbr@v^|~@|C`HnB`DdC|ChThWtbApnAt_@nc@x]vd@pLbO`EtFhKfQxL`SrPdX~Q~ZbZxf@`BqClf@c~@lWaVb_@{h@fNg[pLuYnLwT`JgNjEiCpAy@~@yAd@oBHyBSwBpKc\\lV{h@dKwWdKoOzOeLlPqExdAqGjAz@tA`@xA@tA]nA{@`AuAj@iBTuBCaC_@yBy@iBmAoA{Am@qEwNaA}EYcHv@wFrBuEpBgCpCkB|o@rEhD\\fS~@|WF|VeA`[_ElFg@~o@wIhG}BxI{EjMcKJIhPaVrDoNb@gJs@qLyAsMsD{H{GoHmSoJwU_D}J}EiHyKwDmMgDaTkJ}kAkOg}AUsDsEcs@_Cy_@gAgQoHwlA}Eiq@}Esl@cFwd@aJox@iEe\\kIol@eSoxAew@wwFkBgO{L_hA}Iex@gQw}B}DaZeCo`@mBsX}@wNqA_PeR{pC}c@qsG{M_qBsL{zAqIopAcQkaCqM}hBuAoQaBuRcHcz@_Gyi@_Jai@qEuWy[e}AwPml@wEuNsRan@yMy]iz@mzBmNe_@uXiz@eH}WqI}^uEiZ}Di]}Bo[o@mS]yR@mTIiSc@c`@OoOTmm@n@kf@VkQbAis@`@i[jCas@lBwe@pO_iCdCuJlBqN|BcIlCeGhD{DhD_C~E}AzDk@fFOvJhAbLnC|JbDlJpExLpGfOdM~LnOfDbHxBnHx@lG?tF_@rGiAlFeBfE}CxEoD|C_F|AiFp@mHXoMD}LUsLFq`@gDw`@wF}QsDsNeDou@wU}PgHeZkOwOkHsKsFuNcFoOyLcKkIiPoQs[k]mNaQwQsWgNaTkUya@kR}a@oQ{a@gMg[_KiZeJo\\_R_o@m`AskD}Mah@gWy`Ay[sbA{Osc@_HePmHkPePg\\sLcTcN}ScMaQaMqOa]q_@cJwIoJ}HkJyI}YeSwLgI{Y}N{SmJ}UoJaYuIuVcFiTcDaTqCcRyAyRkAyNOgOOcTTyV|@aTtAgUpC_ZzE{VdEqnFjyAyi@zJqt@pLcv@pHyw@rEyeAdAcbAyB{dBeIipIyb@ao@iDmw@cEyt@aE{xI_d@muCoOehAeFol@sC{q@sEwh@eEmp@eHg_@uFk}@}N}o@wMu^eJeRyE}g@uO{_@oLqnCyz@{uAwc@kqAq`@kzA{e@aw@uV}gDoeAqiAw\\iH{Cgi@qQcTgJkp@_Zii@eVkc@iVep@c_@e{@{k@_v@cm@o]oXqqAenAinAiwA{bFuzF}JgPcfAcmAqZc]cKmMaKeO{HiN}HsOqGuPgGmRkLmh@qDiWsCqZ_Bi[McSIoOb@wTdAoTtBcVdFi^jE{TrJgc@hO_cAdXcwAzPkjAdUecBtMunAlOumBrF{|@bCmr@|DgoAnBspA|@ylAq@wiAeAcjA_DglAaCiu@gDit@eE{s@kF{t@gIu`AcKs`Am_@y`DiUynBq]eyCkKs_Agf@meEce@kcEwSwoBgJa{@wLutAqWm~CiSq_D}Rg|CkNwnBiL{kBkSerCoKsiAiRy~AmSeaB{b@gaDmQolAaZmlBqSwrA}S_qAcWcuA_^y|B{Lgv@yI_|@kImmAaLu}AuI_fB_Gg}A{Bsu@m@cz@Z{w@j@whAj@ckAbAwq@`Dcs@`M}eC~Nq{At]g|CrN{iAdQo~@l]qgBd`@{iCtc@w{BrmC{pOb`A_wFdj@e`Eja@muEfOkjC~DyaBpFqhExDsxApEekBpByrAzDmlDfGi~DfCyxA~KqmC~UugHdLyvErKqsIeCeuDKunAIedAdLi|Fjb@_eHli@caIrIq}@xZiiBrNcn@vQaj@|o@weBfh@c{A`Rce@t|AsuDfhAgxBpUgh@jf@u}@jf@e_Avf@s{@`f@q|@td@i`Alr@gyAhd@q`Alb@maAfd@ahAxKmYxNa`@b_A_kCl}@aqCp|@afD`e@akBbg@wxBp\\_dBjGg[pi@{{CzBiMn`@orC~XsiC~c@}vDrYmmB~b@k|Cf{@gxErh@c~E`Q}wBrO_jBbc@k|Jd[cbJvRelGzEczA|IetDxIw~C~Ay}ALyhKsBipE|AmeDjDecCdLuvCzSkpDv`@agGxrAwoQ|c@qhFzVojDfR{oBv\\mwCp^{eC`i@}_Dv[ooBf~A}yH`~AoyHddBomIb`BydIdl@{cDjQqsAde@ssE`XmxDfMibBrD{y@`j@_qSvy@qoJ~rAevJ|aEoqMvyFo~Nr_AqcCj_AocC~mAsaDzY_t@`sBoyFdZ{eAry@miD~o@arD|d@{mEze@qsEl_@epDnVgnDla@s_Gd^ivEtF_rAnFyc@hF}d@fDsi@nEwh@`Gef@dG}e@xJql@|Kog@fOaf@~Pwd@xXws@xT{e@lPy[zOuUzRyWz^ij@rrAknB~gAirAvx@eo@ddAyp@`aAgh@rcAkg@`kBg_AxeDebBjmCmyAjbAml@vbAsk@zCcBpbAgo@z]sV|bAcy@|{@cw@vz@ux@z}@ix@v_Aoy@hm@ij@fq@}n@hg@_j@dd@_i@rjAcvAnu@aw@zcAwmAfu@_gAbVqb@jVsc@vQc_@nY{o@hT_l@dU_r@nReq@fLsf@nJah@|I_n@tHim@lDi]rDk^tEwe@tFqu@bBuXrAeVdAkZtAu\\~@g_@ZmWZi`@Ru^Gs]]s^o@ig@u@g^g@_Ss@cTgA{UyAyYkCe`@_Ca[kCg\\qEkc@}CqZ{BmOwX_jBoUowAqoAo{HcRahAiM{_AyYwkBgIam@oOkdAgFm]gMq}@oKy{@oH_w@}Fuw@cDsh@uBeb@sCip@sBsg@yA}g@m@wp@IarABcs@h@uk@z@sm@zB}r@~DqaAdCcd@nCyc@|Cyq@`HmnAzH}`AdF}h@nKghAlRwaBpS_bB|I_}@tQwgCjJ_iAlLmnAxQmzBvL_pBlIiaBjKkjBnLgmCpLkcCrLs}BnM{tCxJ{dCrEaeBxEicAhH_}BzA{f@lCke@bEqh@lF_p@hDaf@`Dyh@vAog@dB_ZxBcQ~Fy[nIwf@lGia@zDm]dDoc@rH}aAdDs^nMap@rTgvAhE{\\zDq`@`BkWjBga@zBcj@~Agi@`Aqh@Ly]Doh@mA}lAw@w]cBu]uEoz@qHggA_Hkq@kF}c@}Him@gEeVqCgO}EgSeJkZmJ}Y_My^wLu]{Ko[kOu`@wNu]gIePyRm`@qZ_l@weAemB{t@irAmn@whAgr@{qAwj@gmAm^m|@eb@geAsVus@yOif@{Ugw@cOcm@yUs~@uS}|@mTkiAaK{k@wi@ixCk]iiBgSghAyQcz@iMgl@_N_h@uLwa@_Smr@gJiZgMqa@a[o{@w]a~@aoAe_DuZkw@u^my@}yBwrFy|@sxB{~@{xBqd@}jAag@ymAeaAe}Be[sn@{\\{l@_`AwwAig@wq@sh@ao@am@aq@my@mz@ioBo~AauBiaBqz@sn@_xEavD_fDylCijA_~@ucA}}@kxC}|BgmCynCgw@o}@eu@ciAkuBkgD{mAq`CahAygC}qAkjDqcA_jDqaB_nJ}BeT{o@ahGiIuvAyBuvA{BigD{@_dAt@ebA|CmoBfGgfCngBgbm@xc@_bRbSwvKh`@kbTjGogDhVqqMtKmwFdE{lC|@kbARwdACagAWsq@M}NSsT_B{zAyC{|AgGmkBeF}rAcFahAwAaZyPszDcI{yCiCokGFq_@j@_iAt@}y@~B}kApEyeBzC}y@bFuiAxAqZlBg`@~KaaAvCo_@xPidBbU{iBfJe}@|AkOvf@uyEtCi_@hl@iiFp^ekDbP}lAlIa[zDsF~F}FhH_DlIyAbHZ`HhAdNdJ~GvJvDzKlDbTNdPgA|OoCnLoEvJ{GrJeIzEiEtAuEj@iJS_QyDsPgD{MkDuk@yRkf@kPo\\uKy[sLor@mWciAmb@od@sQmb@{SakAko@{uCsyAgnAor@cK_IiNkNwFeKgIuScBcMo@yH[sPr@_RzCqRrD}LnIsOfIwIdPkIjMoCbK?fMv@vLrFxJ|IdHxJzC|JpBbS_@rRaD~PyItMcOnK{WfFwt@@q[{CqTuDa_@aJmJuC{f@mNoiA{i@}\\oImKkAaLa@yKDiFZ{Eb@sHdA}HxA{FdBqFxAgFlBeFlC_MxHeMhJwJjJsIrKkGrJgWd^wKdRyVlc@aVrb@aPdYyHnO{IlQcI~Q}DlKs}@deBmg@tx@y_@hZuWzLkf@hOch@|LiYUiP^uOxA_TpDw`@jJ{^zN{[`Vo\\nXeZ|U}VxNaOhIyi@zV}a@zNei@jPil@tPuxAjc@akAz]an@~MkyBzr@ie@vLyWzJmwAx`@yVrD}}@nCqf@cDuhAcWoa@uKseAmXol@_R_^mPeH}C_B_AiBUgALeAb@oAlAy@hB_@|BAbC\\|Bn@~A~@hAlAp@lARnAEjA]fDp@pa@dIbc@dI`{Arc@bc@lKbp@bPbj@dGlb@m@uFnMsI`N_JfO}GjYsGjYiJhPqHlHbFpHxQnBrL\\|HnDpQvM`OlRnWp^r]f\\~TrS|XjS"}],"waypoints":[{"distance":10.953,"name":"Yeni Bağdat Caddesi","location":[29.474324,40.824624]},{"distance":7.551,"name":"","location":[30.341988,40.877515]}],"code":"Ok"}
                """.trimIndent()
                //response.use {
                    if (1 ==1) {

                        //val responseBodyJson = response.body!!.string()
                        Timber.d(
                            "calculateRoute ValhallaRouting responseBodyJson: %s",
                            responseBodyJson
                        )
                        val maplibreResponse = DirectionsResponse.fromJson(responseBodyJson);
                        this@ValhallaNavigationActivity.route = maplibreResponse.routes
                            .first()
                            .copy(
                                routeOptions = RouteOptions(
                                    // These dummy route options are not not used to create directions,
                                    // but currently they are necessary to start the navigation
                                    // and to use the voice instructions.
                                    // Again, this isn't ideal, but it is a requirement of the framework.
                                    baseUrl = "http://65.109.128.110:8002/route",
                                    profile = "valhalla",
                                    user = "valhalla",
                                    accessToken = "valhalla",
                                    voiceInstructions = true,
                                    bannerInstructions = true,
                                    language = language,
                                    coordinates = listOf(origin, destination),
                                    requestUuid = "0000-0000-0000-0000",
                                    annotations = DirectionsCriteria.ANNOTATION_SPEED

                                )
                            )

                        runOnUiThread {
                            navigationMapRoute?.addRoutes(maplibreResponse.routes)
                            binding.startRouteLayout.visibility = View.VISIBLE
                        }
                    //} else {

                      //  println(response.body!!.string())
                        println("response.body")

                        Timber.e(
                            "calculateRoute Request to Valhalla failed with status code: %s: %s",
                        )
                   // }
                }
           // }
        //})
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mapLibreMap.isInitialized) {
            mapLibreMap.removeOnMapClickListener(this)
        }
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}
