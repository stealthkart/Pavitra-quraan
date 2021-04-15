package com.scalosphere.labs.kquran.ui.helpers;

import android.graphics.drawable.BitmapDrawable;

import com.scalosphere.labs.kquran.common.Response;
import com.scalosphere.labs.kquran.widgets.AyahToolBar;

import java.util.Set;

public interface AyahTracker {
  public void highlightAyah(int sura, int ayah, HighlightType type);
  public void highlightAyah(int sura, int ayah, HighlightType type, boolean scrollToAyah);
  public void highlightAyat(
      int page, Set<String> ayahKeys, HighlightType type);
  public void unHighlightAyah(int sura, int ayah, HighlightType type);
  public void unHighlightAyahs(HighlightType type);
  public AyahToolBar.AyahToolBarPosition getToolBarPosition(int sura, int ayah,
      int toolBarWidth, int toolBarHeight);
  public void updateView();
  public void onLoadImageResponse(BitmapDrawable drawable, Response response);
}