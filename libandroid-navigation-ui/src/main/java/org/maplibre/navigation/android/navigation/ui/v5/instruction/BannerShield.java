package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import org.maplibre.navigation.android.navigation.v5.models.BannerComponents;

class BannerShield {
  private String url;
  private String text;
  private int nodeIndex;
  private int startIndex = -1;

  BannerShield(BannerComponents bannerComponents, int nodeIndex) {
    this.url = bannerComponents.getImageBaseUrl();
    this.nodeIndex = nodeIndex;
    this.text = bannerComponents.getText();
  }

  String getUrl() {
    return url;
  }

  public String getText() {
    return text;
  }

  public int getNodeIndex() {
    return nodeIndex;
  }

  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  public int getStartIndex() {
    return startIndex;
  }

  int getEndIndex() {
    return startIndex + text.length();
  }
}
