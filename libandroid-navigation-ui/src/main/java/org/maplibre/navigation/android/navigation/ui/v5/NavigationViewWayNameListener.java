package org.maplibre.navigation.android.navigation.ui.v5;

import androidx.annotation.NonNull;

import org.maplibre.navigation.android.navigation.ui.v5.map.OnWayNameChangedListener;

class NavigationViewWayNameListener implements OnWayNameChangedListener {

  private final NavigationPresenter presenter;

  NavigationViewWayNameListener(NavigationPresenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void onWayNameChanged(@NonNull String wayName) {
    presenter.onWayNameChanged(wayName);
  }
}
