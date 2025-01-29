package org.maplibre.navigation.android.navigation.ui.v5.summary.list;

import androidx.annotation.NonNull;
import android.text.SpannableString;

import org.maplibre.navigation.android.navigation.v5.models.BannerInstructions;
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute;
import org.maplibre.navigation.android.navigation.v5.models.LegStep;
import org.maplibre.navigation.android.navigation.v5.models.RouteLeg;
import org.maplibre.navigation.android.navigation.ui.v5.BaseTest;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.navigation.android.navigation.v5.utils.DistanceFormatter;
import org.maplibre.navigation.android.navigation.v5.utils.RouteUtils;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstructionListPresenterTest extends BaseTest {

  private static final int FIRST = 0;

  @Test
  public void onBindInstructionListView_distanceTextIsUpdated() throws Exception {
    SpannableString spannableString = mock(SpannableString.class);
    RouteProgress routeProgress = buildRouteProgress();
    InstructionListPresenter presenter = buildPresenter(spannableString);
    presenter.updateBannerListWith(routeProgress);
    InstructionListView listView = mock(InstructionListView.class);

    presenter.onBindInstructionListViewAtPosition(0, listView);

    verify(listView).updateDistanceText(spannableString);
  }

  @Test
  public void onBindInstructionListView_primaryTextIsUpdated() throws Exception {
    SpannableString spannableString = mock(SpannableString.class);
    RouteProgress routeProgress = buildRouteProgress();
    InstructionListPresenter presenter = buildPresenter(spannableString);
    presenter.updateBannerListWith(routeProgress);
    InstructionListView listView = mock(InstructionListView.class);

    presenter.onBindInstructionListViewAtPosition(0, listView);

    verify(listView).updatePrimaryText(anyString());
  }

  @Test
  public void onBindInstructionListView_secondaryTextIsUpdated() throws Exception {
    SpannableString spannableString = mock(SpannableString.class);
    RouteProgress routeProgress = buildRouteProgress();
    InstructionListPresenter presenter = buildPresenter(spannableString);
    presenter.updateBannerListWith(routeProgress);
    InstructionListView listView = mock(InstructionListView.class);

    presenter.onBindInstructionListViewAtPosition(0, listView);

    verify(listView).updateSecondaryText(anyString());
  }

  @Test
  public void onBindInstructionListView_maneuverViewTypeAndModifierAreUpdated() throws Exception {
    SpannableString spannableString = mock(SpannableString.class);
    RouteProgress routeProgress = buildRouteProgress();
    InstructionListPresenter presenter = buildPresenter(spannableString);
    presenter.updateBannerListWith(routeProgress);
    InstructionListView listView = mock(InstructionListView.class);

    presenter.onBindInstructionListViewAtPosition(0, listView);

    verify(listView).updateManeuverViewTypeAndModifier(anyString(), anyString());
  }

  @Test
  public void onBindInstructionListView_maneuverViewDrivingSideIsUpdated() throws Exception {
    SpannableString spannableString = mock(SpannableString.class);
    RouteProgress routeProgress = buildRouteProgress();
    InstructionListPresenter presenter = buildPresenter(spannableString);
    presenter.updateBannerListWith(routeProgress);
    InstructionListView listView = mock(InstructionListView.class);

    presenter.onBindInstructionListViewAtPosition(0, listView);

    verify(listView).updateManeuverViewDrivingSide(anyString());
  }

  @Test
  public void retrieveBannerInstructionListSize_returnsCorrectListSize() throws Exception {
    RouteProgress routeProgress = buildRouteProgress();
    RouteUtils routeUtilsMock = mockRouteUtils(routeProgress);
    DistanceFormatter distanceFormatter = mock(DistanceFormatter.class);
    InstructionListPresenter presenter = new InstructionListPresenter(distanceFormatter);

    presenter.updateBannerListWith(routeProgress);

    int expectedInstructionSize = retrieveInstructionSizeFrom(routeProgress.getCurrentLeg());
    assertEquals(expectedInstructionSize, presenter.retrieveBannerInstructionListSize());
  }

  @Test
  public void updateBannerListWith_instructionListIsPopulated() throws Exception {
    RouteProgress routeProgress = buildRouteProgress();
    RouteUtils routeUtilsMock = mockRouteUtils(routeProgress);
    DistanceFormatter distanceFormatter = mock(DistanceFormatter.class);
    InstructionListPresenter presenter = new InstructionListPresenter(distanceFormatter);

    boolean didUpdate = presenter.updateBannerListWith(routeProgress);

    assertTrue(didUpdate);
  }

  @Test
  public void updateBannerListWith_emptyInstructionsReturnFalse() throws Exception {
    RouteProgress routeProgress = buildRouteProgress();
    RouteUtils routeUtilsMock = mockRouteUtils(routeProgress);
    clearInstructions(routeProgress);
    DistanceFormatter distanceFormatter = mock(DistanceFormatter.class);
    InstructionListPresenter presenter = new InstructionListPresenter(distanceFormatter);

    boolean didUpdate = presenter.updateBannerListWith(routeProgress);

    assertFalse(didUpdate);
  }

  @Test
  public void updateDistanceFormatter_newFormatterIsUsed() throws Exception {
    RouteProgress routeProgress = buildRouteProgress();
    DistanceFormatter firstDistanceFormatter = buildDistanceFormatter();
    RouteUtils routeUtilsMock = mockRouteUtils(routeProgress);
    InstructionListPresenter presenter = buildPresenter(firstDistanceFormatter);
    presenter.updateBannerListWith(routeProgress);
    InstructionListView listView = mock(InstructionListView.class);

    DistanceFormatter secondDistanceFormatter = buildDistanceFormatter();
    presenter.updateDistanceFormatter(secondDistanceFormatter);
    presenter.onBindInstructionListViewAtPosition(0, listView);

    verify(secondDistanceFormatter).formatDistance(anyDouble());
  }

  @Test
  public void updateDistanceFormatter_doesNotAllowNullValues() throws Exception {
    RouteProgress routeProgress = buildRouteProgress();
    DistanceFormatter distanceFormatter = buildDistanceFormatter();
    InstructionListPresenter presenter = buildPresenter(distanceFormatter);
    presenter.updateBannerListWith(routeProgress);
    InstructionListView listView = mock(InstructionListView.class);

    presenter.updateDistanceFormatter(null);
    presenter.onBindInstructionListViewAtPosition(0, listView);

    verify(distanceFormatter).formatDistance(anyDouble());
  }


  @NonNull
  private RouteProgress buildRouteProgress() throws Exception {
    DirectionsRoute route = buildTestDirectionsRoute();
    return buildRouteProgress(route, 100, 100, 100, 0, 0);
  }

  @NonNull
  private InstructionListPresenter buildPresenter(SpannableString spannableString) {
    DistanceFormatter distanceFormatter = mock(DistanceFormatter.class);
    when(distanceFormatter.formatDistance(anyDouble())).thenReturn(spannableString);
    return new InstructionListPresenter(distanceFormatter);
  }

  @NonNull
  private InstructionListPresenter buildPresenter(DistanceFormatter distanceFormatter) {
    return new InstructionListPresenter(distanceFormatter);
  }

  @NonNull
  private RouteUtils mockRouteUtils(RouteProgress routeProgress) {
    BannerInstructions instructions = routeProgress.getCurrentLegProgress().getCurrentStep().getBannerInstructions().get(FIRST);
    RouteUtils routeUtils = mock(RouteUtils.class);
    when(routeUtils.findCurrentBannerInstructions(any(LegStep.class), anyDouble())).thenReturn(instructions);
    return routeUtils;
  }

  @NonNull
  private DistanceFormatter buildDistanceFormatter() {
    SpannableString spannableString = mock(SpannableString.class);
    DistanceFormatter distanceFormatter = mock(DistanceFormatter.class);
    when(distanceFormatter.formatDistance(anyDouble())).thenReturn(spannableString);
    return distanceFormatter;
  }

  private int retrieveInstructionSizeFrom(RouteLeg routeLeg) {
    List<BannerInstructions> instructions = new ArrayList<>();
    List<LegStep> steps = routeLeg.getSteps();
    for (LegStep step : steps) {
      List<BannerInstructions> bannerInstructions = step.getBannerInstructions();
      if (bannerInstructions != null) {
        instructions.addAll(bannerInstructions);
      }
    }
    return instructions.size() - 1;
  }

  private void clearInstructions(RouteProgress routeProgress) {
    for (LegStep step : routeProgress.getCurrentLeg().getSteps()) {
      List<BannerInstructions> instructions = step.getBannerInstructions();
      if (instructions != null) {
        instructions.clear();
      }
    }
  }
}