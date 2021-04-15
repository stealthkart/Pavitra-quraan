package com.scalosphere.labs.kquran.ui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.common.Response;
import com.scalosphere.labs.kquran.data.Constants;
import com.scalosphere.labs.kquran.data.QuranDataProvider;
import com.scalosphere.labs.kquran.data.QuranInfo;
import com.scalosphere.labs.kquran.task.TranslationTask;
import com.scalosphere.labs.kquran.ui.PagerActivity;
import com.scalosphere.labs.kquran.ui.helpers.AyahTracker;
import com.scalosphere.labs.kquran.ui.helpers.HighlightType;
import com.scalosphere.labs.kquran.ui.helpers.QuranDisplayHelper;
import com.scalosphere.labs.kquran.util.QuranFileUtils;
import com.scalosphere.labs.kquran.widgets.AyahToolBar;
import com.scalosphere.labs.kquran.widgets.TranslationView;

import java.util.Set;

public class TranslationFragment extends SherlockFragment
    implements AyahTracker {
  private static final String TAG = "TranslationPageFragment";
  private static final String PAGE_NUMBER_EXTRA = "pageNumber";

  private static final String SI_PAGE_NUMBER = "SI_PAGE_NUMBER";
  private static final String SI_HIGHLIGHTED_AYAH = "SI_HIGHLIGHTED_AYAH";

  private int mPageNumber;
  private int mHighlightedAyah;
  private TranslationView mTranslationView;
  private PaintDrawable mLeftGradient, mRightGradient = null;

  private View mMainView;
  private ImageView mLeftBorder, mRightBorder;

  private Resources mResources;
  private SharedPreferences mPrefs;
  private boolean mJustCreated;

  public static TranslationFragment newInstance(int page) {
    final TranslationFragment f = new TranslationFragment();
    final Bundle args = new Bundle();
    args.putInt(PAGE_NUMBER_EXTRA, page);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPageNumber = getArguments() != null ?
        getArguments().getInt(PAGE_NUMBER_EXTRA) : -1;
    if (savedInstanceState != null) {
      int page = savedInstanceState.getInt(SI_PAGE_NUMBER, -1);
      if (page == mPageNumber) {
        int highlightedAyah =
            savedInstanceState.getInt(SI_HIGHLIGHTED_AYAH, -1);
        if (highlightedAyah > 0) {
          mHighlightedAyah = highlightedAyah;
        }
      }
    }
    Display display = getActivity().getWindowManager().getDefaultDisplay();
    int width = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
        QuranDisplayHelper.getWidthKitKat(display) : display.getWidth();
    mLeftGradient = QuranDisplayHelper.getPaintDrawable(width, 0);
    mRightGradient = QuranDisplayHelper.getPaintDrawable(0, width);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(
        R.layout.translation_layout, container, false);
    view.setBackgroundDrawable((mPageNumber % 1 == 0 ?
        mLeftGradient : mRightGradient));

    mPrefs = PreferenceManager
        .getDefaultSharedPreferences(getActivity());
    mResources = getResources();


    mLeftBorder = (ImageView) view.findViewById(R.id.left_border);
    mRightBorder = (ImageView) view.findViewById(R.id.right_border);

    mTranslationView = (TranslationView) view
        .findViewById(R.id.translation_text);
    mTranslationView.setPageNumber(mPageNumber);
    mTranslationView.setTranslationClickedListener(
        new TranslationView.TranslationClickedListener() {
          @Override
          public void onTranslationClicked() {
            final Activity activity = getActivity();
            if (activity != null && activity instanceof PagerActivity){
              ((PagerActivity) getActivity()).toggleActionBar();
            }
          }
        });

    mMainView = view;
    updateView();
    mJustCreated = true;
    String kanDB = mPrefs.getString(Constants.PREF_ACTIVE_TRANSLATION, null);
    //Log.i("TFragment", " database before setting is " + kanDB) ;
    if(kanDB==null){
        if (QuranFileUtils.hasKanSearchDatabase(getActivity())) {
            kanDB = QuranDataProvider.QURAN_KAN_DATABASE;
        }else{
            //TODO download Kannada DB forcefully

            kanDB = QuranDataProvider.QURAN_KAN_DATABASE;
        }
        mPrefs.edit().putString(Constants.PREF_ACTIVE_TRANSLATION, kanDB).commit();
       // Log.i("TFragment", " database is " + kanDB) ;
    }

    refresh(kanDB);
    return view;
  }

  @Override
  public void onLoadImageResponse(BitmapDrawable drawable, Response response) {
    // no op, we're not requesting images here
  }

  public void updateView() {
    if (getActivity() == null || mResources == null ||
        mMainView == null || !isAdded()) {
      return;
    }

    mMainView.setBackgroundDrawable((mPageNumber % 1 == 0 ?
        mLeftGradient : mRightGradient));
    // Orginal if (!mPrefs.getBoolean(Constants.PREF_USE_NEW_BACKGROUND, true)) {
    if (mPrefs.getBoolean(Constants.PREF_USE_NEW_BACKGROUND, true)) {
      mMainView.setBackgroundColor(mResources.getColor(R.color.page_background));
    }

    boolean nightMode = mPrefs.getBoolean(Constants.PREF_NIGHT_MODE, false);
    int nightModeTextBrightness = mPrefs.getInt(
        Constants.PREF_NIGHT_MODE_TEXT_BRIGHTNESS,
        Constants.DEFAULT_NIGHT_MODE_TEXT_BRIGHTNESS);
    mTranslationView.setNightMode(nightMode, nightModeTextBrightness);
    if (nightMode) {
      mMainView.setBackgroundColor(Color.BLACK);
    }

    int lineImageId = R.drawable.dark_line;
    int leftBorderImageId = R.drawable.border_left;
    int rightBorderImageId = R.drawable.border_right;
    if (mPrefs.getBoolean(Constants.PREF_NIGHT_MODE, false)) {
      leftBorderImageId = R.drawable.night_left_border;
      rightBorderImageId = R.drawable.night_right_border;
      // Orginal lineImageId = R.drawable.light_line;
      lineImageId = R.drawable.dark_line;
    }

    // Orginal if (mPageNumber % 1 == 0)
      if (mPageNumber % 1 == 0) {
          mRightBorder.setVisibility(View.GONE);
          mLeftBorder.setBackgroundResource(leftBorderImageId);
      } else {
          mRightBorder.setVisibility(View.VISIBLE);
          mRightBorder.setBackgroundResource(rightBorderImageId);
          mLeftBorder.setBackgroundResource(lineImageId);
      }
  }

  @Override
  public void highlightAyah(int sura, int ayah, HighlightType type) {
    highlightAyah(sura, ayah, type, true);
  }

  @Override
  public void highlightAyah(int sura, int ayah, HighlightType type, boolean scrollToAyah) {
    if (mTranslationView != null) {
        //Log.i("TF", " outside Highlight type "+type);
      mHighlightedAyah = QuranInfo.getAyahId(sura, ayah);
      mTranslationView.highlightAyah(mHighlightedAyah);
      if(type==HighlightType.BLACK){
         //Log.i("TF", " inside black Highlight type "+type);
         mTranslationView.unhighlightAyat();
      }
    }
  }

  @Override
  public AyahToolBar.AyahToolBarPosition getToolBarPosition(int sura, int ayah,
      int toolBarWidth, int toolBarHeight) {
    // not yet implemented
    return null;
  }

  @Override
  public void highlightAyat(
      int page, Set<String> ayahKeys, HighlightType type) {
    // not yet supported
  }

  @Override
  public void unHighlightAyah(int sura, int ayah, HighlightType type) {
    if (mHighlightedAyah == QuranInfo.getAyahId(sura, ayah)) {
      unHighlightAyahs(type);
    }
  }

  @Override
  public void unHighlightAyahs(HighlightType type) {
    if (mTranslationView != null) {
      mTranslationView.unhighlightAyat();
      mHighlightedAyah = -1;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!mJustCreated) {
      updateView();
      mTranslationView.refresh();
    }
    mJustCreated = false;
  }

  public void refresh(String database) {
    if (database != null) {
      Activity activity = getActivity();
      if (activity != null) {
        new TranslationTask(activity, mPageNumber,
            mHighlightedAyah, database, mTranslationView).execute();
      }
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (mHighlightedAyah > 0) {
      outState.putInt(SI_HIGHLIGHTED_AYAH, mHighlightedAyah);
    }
    super.onSaveInstanceState(outState);
  }
}
