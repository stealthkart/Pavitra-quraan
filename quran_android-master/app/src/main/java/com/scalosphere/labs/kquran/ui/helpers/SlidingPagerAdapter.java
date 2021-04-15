package com.scalosphere.labs.kquran.ui.helpers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.ui.fragment.AyahPlaybackFragment;
import com.scalosphere.labs.kquran.ui.fragment.AyahTranslationFragment;
import com.scalosphere.labs.kquran.ui.fragment.TagBookmarkDialog;
import com.scalosphere.labs.kquran.widgets.IconPageIndicator;

public class SlidingPagerAdapter extends FragmentStatePagerAdapter implements
        IconPageIndicator.IconPagerAdapter {

  public static final int TAG_PAGE = 0;
  public static final int TRANSLATION_PAGE = 1;
  public static final int AUDIO_PAGE = 2;
  public static final int[] PAGES = {
      TAG_PAGE, TRANSLATION_PAGE, AUDIO_PAGE
  };
  public static final int[] PAGE_ICONS = {
      R.drawable.ic_tag, R.drawable.ic_translation, R.drawable.ic_play
  };

  public SlidingPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public int getCount() {
    return PAGES.length;
  }

  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case TAG_PAGE:
        return new TagBookmarkDialog();
      case TRANSLATION_PAGE:
        return new AyahTranslationFragment();
      case AUDIO_PAGE:
        return new AyahPlaybackFragment();
    }
    return null;
  }

  @Override
  public int getIconResId(int index) {
    return PAGE_ICONS[index];
  }

}
