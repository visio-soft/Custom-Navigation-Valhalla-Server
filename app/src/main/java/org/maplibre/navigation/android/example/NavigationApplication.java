package org.maplibre.navigation.android.example;

import android.app.Application;
import org.maplibre.android.MapLibre;


public class NavigationApplication extends Application {


  @Override
  public void onCreate() {
    super.onCreate();

    MapLibre.getInstance(getApplicationContext());
  }

}
