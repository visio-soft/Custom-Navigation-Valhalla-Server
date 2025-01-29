Here's the updated README with the author and company details included:

---

# MapLibre Navigation with Valhalla Routing Engine

This project demonstrates the integration of **MapLibre Navigation Android SDK** with the **Valhalla routing engine** for custom routing and navigation. It provides an interactive navigation experience, allowing users to navigate with turn-by-turn directions using a custom routing backend.

### Written by:
Author: **Visiosoft**

## Features

- **MapLibre Navigation Android SDK** integration for smooth and customizable navigation.
- Custom routing powered by **Valhalla**, an open-source routing engine.
- Turn-by-turn directions with custom routing algorithms.
- Seamless interaction between the navigation interface and Valhalla backend.

## Requirements

- Android Studio (latest stable version)
- Android SDK (API level 21 or higher)
- **MapLibre Navigation Android SDK** (latest stable release)
- **Valhalla routing engine** (self-hosted or integrated API endpoint)

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/your-project-name.git
cd your-project-name
```

### 2. Add dependencies

In your `build.gradle` (app-level), add the following dependencies:

```gradle
dependencies {
    implementation 'org.maplibre.navigation:maplibre-navigation-android:<latest_version>'
    implementation 'org.maplibre:maplibre-sdk-android:<latest_version>'
    // Other necessary dependencies
}
```

Make sure to replace `<latest_version>` with the most recent stable versions of the libraries.

### 3. Set up MapLibre

To use MapLibre, you'll need to add your MapLibre access token and map style. Follow the [MapLibre documentation](https://maplibre.org) for getting started with your token and style configuration.

### 4. Configure Valhalla Routing Engine

Valhalla can be run as a self-hosted server or accessed via a public API. To set it up:

- If you're running a self-hosted Valhalla instance, follow the [Valhalla setup guide](https://github.com/valhalla/valhalla).
- If you're using a public API, you'll need to configure the endpoint URL in your application.

Example configuration:

```java
val routingEngine = new ValhallaRoutingEngine("https://your-valhalla-api-endpoint")
```

### 5. Implement Routing

To start using Valhalla for routing, initialize the engine and request a route between two locations.

```java
routingEngine.getRoute(startLocation, endLocation, new RoutingCallback() {
    @Override
    public void onRouteReady(Route route) {
        // Handle the route data
    }

    @Override
    public void onError(Throwable error) {
        // Handle errors
    }
});
```

### 6. Run the application

After setting up the dependencies and configurations, you can now run your application on an Android device or emulator.
