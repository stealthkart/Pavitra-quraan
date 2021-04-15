package com.scalosphere.labs.kquran.widgets;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.common.QuranAyah;
import com.scalosphere.labs.kquran.data.QuranInfo;
import com.scalosphere.labs.kquran.ui.util.ArabicTypefaceSpan;
import com.scalosphere.labs.kquran.util.ArabicStyle;
import com.scalosphere.labs.kquran.util.QuranScreenInfo;
import com.scalosphere.labs.kquran.util.QuranSettings;

import java.util.List;

public class TranslationView extends ScrollView {

   private Context mContext;
   private int mLeftRightMargin;
   private int mTopBottomMargin;
   private int mTextStyle;
   private int mHighlightedStyle;
   private int mFontSize;
   private int mHeaderImage1;
   private int mHeaderBismillah;
   private int mFooterSpacerHeight;
   private boolean mIsArabic;
   private boolean mUseArabicFont;
   private boolean mShouldReshape;
   private int mLastHighlightedAyah;
   private boolean mIsNightMode;
   private int mNightModeTextColor;
   private boolean mIsInAyahActionMode;
   private String mTranslatorName;
   private int mPageNumber;

   private List<QuranAyah> mAyat;


   private LinearLayout mLinearLayout;
   private TranslationClickedListener mTranslationClickedListener;

   public TranslationView(Context context){
      super(context);
      init(context);
   }

   public TranslationView(Context context, AttributeSet attrs){
      super(context, attrs);
      init(context);
   }

   public TranslationView(Context context, AttributeSet attrs, int defStyle){
      super(context, attrs, defStyle);
      init(context);
   }

   public void setIsInAyahActionMode(boolean isInAyahActionMode) {
      mIsInAyahActionMode = isInAyahActionMode;
   }


    public void setPageNumber(int pageNumber) {
        this.mPageNumber = mPageNumber;
    }

   public void init(Context context){
      mContext = context;
      setFillViewport(true);
      mLinearLayout = new LinearLayout(context);
      mLinearLayout.setOrientation(LinearLayout.VERTICAL);
      addView(mLinearLayout, ScrollView.LayoutParams.MATCH_PARENT,
              ScrollView.LayoutParams.WRAP_CONTENT);
      mLinearLayout.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (mTranslationClickedListener != null){
               mTranslationClickedListener.onTranslationClicked();
            }
         }
      });

      Resources resources = getResources();
      mLeftRightMargin = resources.getDimensionPixelSize(
              R.dimen.translation_left_right_margin);
      mTopBottomMargin = resources.getDimensionPixelSize(
          R.dimen.translation_top_bottom_margin);
      mFooterSpacerHeight = resources.getDimensionPixelSize(
          R.dimen.translation_footer_spacer);

      mHeaderImage1 = R.drawable.header;
      mHeaderBismillah = R.drawable.bismillah;
      initResources();
   }

   private void initResources(){
      mFontSize = QuranSettings.getTranslationTextSize(mContext);
      //mFontSize = QuranSettings.getTranslationTextSize(mContext);
      //Log.i("TranslationView","font size is "+mFontSize);

      mIsArabic = QuranSettings.isArabicNames(mContext);
      mShouldReshape = QuranSettings.isReshapeArabic(mContext);
      mUseArabicFont = QuranSettings.needArabicFont(mContext);

      mIsNightMode = QuranSettings.isNightMode(mContext);
      if (mIsNightMode) {
         int brightness = QuranSettings.getNightModeTextBrightness(mContext);
         mNightModeTextColor = Color.rgb(brightness, brightness, brightness);
      }
      mTextStyle = mIsNightMode ? R.style.TranslationText_NightMode :
              R.style.TranslationText;
      mHighlightedStyle = mIsNightMode?
              R.style.TranslationText_NightMode_Highlighted :
              R.style.TranslationText_Highlighted;
   }

   public void refresh(){
      initResources();
      if (mAyat != null){
        setAyahs(mAyat);
      }
   }

   public void setNightMode(boolean isNightMode, int textBrightness) {
     mIsNightMode = isNightMode;
     if (isNightMode) {
       mNightModeTextColor = Color.rgb(textBrightness, textBrightness, textBrightness);
     }
     mTextStyle = mIsNightMode ? R.style.TranslationText_NightMode :
         R.style.TranslationText;
     mHighlightedStyle = mIsNightMode?
         R.style.TranslationText_NightMode_Highlighted :
         R.style.TranslationText_Highlighted;
     if (mAyat != null){
       setAyahs(mAyat);
     }
   }

   public void setTranslatorName(String name) {
     mTranslatorName = name;
   }

   public void setAyahs(List<QuranAyah> ayat){
     mLastHighlightedAyah = -1;

      mLinearLayout.removeAllViews();
      mAyat = ayat;


      if (mTranslatorName != null) {
        addTranslationNameHeader(mTranslatorName);
      }

      int currentSura = 0;
      for (QuranAyah ayah : ayat){
         if (!mIsInAyahActionMode && ayah.getSura() != currentSura){
             if(ayah.getAyah() == 1)
             {
                 addSuraHeaderOnce(ayah.getSura());
             }else{
                 addSuraHeader(ayah.getSura());
             }
            currentSura = ayah.getSura();
         }
         addTextForAyah(ayah);
      }

      addFooterSpacer();
   }

   public void unhighlightAyat(){
      if (mLastHighlightedAyah > 0){
         TextView text = (TextView)mLinearLayout
                 .findViewById(mLastHighlightedAyah);
         if (text != null){
            text.setTextAppearance(getContext(), mTextStyle);
            text.setTextSize(mFontSize);
            ArabicStyle.applyFont(mContext,text);
           /* Typeface typeface = ArabicStyle.getTypeface(mContext);
            text.setTypeface(typeface);*/
         }
      }
      mLastHighlightedAyah = -1;
   }

   public void highlightAyah(int ayahId){
      if (mLastHighlightedAyah > 0){
         TextView text = (TextView)mLinearLayout.
                 findViewById(mLastHighlightedAyah);
         text.setTextAppearance(getContext(), mTextStyle);
         ArabicStyle.applyFont(mContext,text);
/*         Typeface typeface = ArabicStyle.getTypeface(mContext);
         text.setTypeface(typeface);*/
      }

      TextView text = (TextView)mLinearLayout.findViewById(ayahId);
      if (text != null){
         text.setTextAppearance(getContext(), mHighlightedStyle);
         text.setTextSize(mFontSize);
         ArabicStyle.applyFont(mContext,text);
         /*Typeface typeface = ArabicStyle.getTypeface(mContext);
         text.setTypeface(typeface);*/
         mLastHighlightedAyah = ayahId;

         int screenHeight = QuranScreenInfo.getInstance().getHeight();
         int y = text.getTop() - (int)(0.25 * screenHeight);
         smoothScrollTo(getScrollX(), y);
      }
      else { mLastHighlightedAyah = -1; }
   }

   private OnClickListener mOnAyahClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
         if (mTranslationClickedListener != null){
            mTranslationClickedListener.onTranslationClicked();
         }
      }
   };

   private OnLongClickListener mOnCopyAyahListener = new OnLongClickListener(){
      @Override
      public boolean onLongClick(View v) {
         if (v instanceof TextView){
            ClipboardManager mgr = (ClipboardManager)mContext.
                    getSystemService(Service.CLIPBOARD_SERVICE);
            mgr.setText(((TextView)v).getText());
            Toast.makeText(mContext, R.string.ayah_copied_popup,
                    Toast.LENGTH_SHORT).show();
         }
         return true;
      }
   };

   private void addFooterSpacer() {
     final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
         LayoutParams.MATCH_PARENT, mFooterSpacerHeight);
     final View view = new View(mContext);
     mLinearLayout.addView(view, params);
   }

   private void addTranslationNameHeader(String translationName) {
     final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
         LayoutParams.MATCH_PARENT,
         LayoutParams.WRAP_CONTENT);
     params.setMargins(mLeftRightMargin, mTopBottomMargin,
         mLeftRightMargin, mTopBottomMargin);

     final TextView translationHeader = new TextView(mContext);
     translationHeader.setTextAppearance(mContext, mTextStyle);
     if (mIsInAyahActionMode) { translationHeader.setTextColor(Color.WHITE); }
     else if (mIsNightMode) { translationHeader.setTextColor(mNightModeTextColor); }

     translationHeader.setTextSize(mFontSize);
     /* Orginal translationHeader.setText(translationName);*/
     translationHeader.setTypeface(null, Typeface.BOLD);
     mLinearLayout.addView(translationHeader, params);
   }

   private void addTextForAyah(QuranAyah ayah){
      LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
              LayoutParams.MATCH_PARENT,
              LayoutParams.WRAP_CONTENT);
       headerParams.setMargins(mLeftRightMargin, mTopBottomMargin,
              mLeftRightMargin, mTopBottomMargin);

      TextView ayahHeader = new TextView(mContext);
      ayahHeader.setTextAppearance(mContext, mTextStyle);
      if (mIsInAyahActionMode) ayahHeader.setTextColor(Color.WHITE);
      else if (mIsNightMode) ayahHeader.setTextColor(mNightModeTextColor);
      ayahHeader.setTextSize(mFontSize);
      ayahHeader.setText(ayah.getSura() + ":" + ayah.getAyah());
      ayahHeader.setTypeface(null, Typeface.BOLD);
      mLinearLayout.addView(ayahHeader, headerParams);

      TextView ayahView = new TextView(mContext);
      ayahView.setId(
              QuranInfo.getAyahId(ayah.getSura(), ayah.getAyah()));
      ayahView.setOnClickListener(mOnAyahClickListener);

      ayahView.setTextAppearance(mContext, mTextStyle);
      if (mIsInAyahActionMode) ayahView.setTextColor(Color.WHITE);
      else if (mIsNightMode) ayahView.setTextColor(mNightModeTextColor);
      ayahView.setTextSize(mFontSize);

      ArabicStyle.applyFont(mContext,ayahView);
      /*Typeface typeface = ArabicStyle.getTypeface(mContext);
      ayahView.setTypeface(typeface);*/

      // arabic
      String ayahText = ayah.getText();
      if (!TextUtils.isEmpty(ayahText)){
         // Ayah Text
         ayahView.setLineSpacing(1.4f, 1.4f);

         boolean customFont = false;
         if (mShouldReshape){
            ayahText = ArabicStyle.reshape(mContext, ayahText);
            if (mUseArabicFont){
               customFont = true;
            }
         }
         SpannableString arabicText = new SpannableString(ayahText);

         CharacterStyle spanType;
         if (customFont){
            spanType = new ArabicTypefaceSpan(mContext, true);
         }
         else { spanType = new StyleSpan(Typeface.BOLD); }

         arabicText.setSpan(spanType, 0, ayahText.length(),
                 Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         ayahView.append(arabicText);
         ayahView.append("\n\n");
      }

      // translation
      String translationText = ayah.getTranslation();

       LinearLayout.LayoutParams ayahParams = new LinearLayout.LayoutParams(
               LayoutParams.MATCH_PARENT,
               LayoutParams.WRAP_CONTENT);

      if (TextUtils.isEmpty(translationText)){
            // Ayah Text
           ayahView.setLineSpacing(1.0f, 1.0f);
          ayahParams.setMargins(mLeftRightMargin, 0,
                  mLeftRightMargin, 0);
      }else{
          ayahView.setLineSpacing(1.4f, 1.4f);
          ayahParams.setMargins(mLeftRightMargin, mTopBottomMargin,
                  mLeftRightMargin, mTopBottomMargin);
      }
      boolean customFont = false;
      if (mShouldReshape){
         if (ayah.isArabic()){
            translationText = ArabicStyle.reshape(mContext,
                    translationText);
            customFont = true;
         }
      }

      SpannableString translation = new SpannableString(translationText);
      if (customFont){
         ArabicTypefaceSpan span = new ArabicTypefaceSpan(mContext, false);
         translation.setSpan(span, 0, translation.length(),
                 Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      //ayahView.append(translation);
       ayahView.append(Html.fromHtml(translationText));

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
         setTextSelectableHoneycomb(ayahView);
      }
      else {
         ayahView.setOnLongClickListener(mOnCopyAyahListener);
      }

      mLinearLayout.addView(ayahView, ayahParams);
   }

   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
   private void setTextSelectableHoneycomb(TextView ayahView) {
     ayahView.setTextIsSelectable(true);
   }

   private void addSuraHeaderOnce (int currentSura){
      final String suraName = QuranInfo.getSuraNameWithFN(mContext, currentSura, false);
      final String metaData = QuranInfo.getSuraListMetaString(mContext, currentSura);

       FrameLayout cFrameLayout = new FrameLayout(mContext);

       ImageView imageView1 = new ImageView(mContext);
       imageView1.setImageResource(mHeaderImage1);
       cFrameLayout.addView(imageView1);

      final TextView headerView = new TextView(mContext);
       FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
              LayoutParams.WRAP_CONTENT);
      params.gravity = Gravity.CENTER;
      headerView.setTextColor(Color.BLACK);
      headerView.setTextSize(19);
      headerView.isClickable();

      headerView.setText(Html.fromHtml(suraName));
      // Log.i("TranslationView", "suraName is " + suraName);
       headerView.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View viewIn) {
               if (headerView.getText() == suraName) {
                   headerView.setText(Html.fromHtml(metaData));
               } else {
                   headerView.setText(Html.fromHtml(suraName));
               }
           }
       });
       cFrameLayout.addView(headerView, params);
       mLinearLayout.addView(cFrameLayout);


       ImageView imageBismillah = new ImageView(mContext);
       imageBismillah.setImageResource(mHeaderBismillah);
       mLinearLayout.addView(imageBismillah);
       if(currentSura == 1 || currentSura == 9){
           imageBismillah.setVisibility(View.GONE);
       }else{
           imageBismillah.setVisibility(View.VISIBLE);
       }
   }


    private void addSuraHeader(int currentSura){
        final String suraName = QuranInfo.getSuraNameOnly(mContext, currentSura, false);
        final String metaData = QuranInfo.getSuraListMetaString(mContext, currentSura);


        FrameLayout cFrameLayout = new FrameLayout(mContext);

        final TextView headerView = new TextView(mContext);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        headerView.setTextColor(Color.BLACK);
        headerView.setTextSize(18);
        headerView.setText(suraName);
        headerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                if (headerView.getText() == suraName) {
                    headerView.setText(metaData);
                } else {
                    headerView.setText(suraName);
                }
            }
        });
        cFrameLayout.addView(headerView, params);
        mLinearLayout.addView(cFrameLayout);
    }

   public void setTranslationClickedListener(
           TranslationClickedListener listener){
      mTranslationClickedListener = listener;
   }

   public interface TranslationClickedListener {
      public void onTranslationClicked();
   }
}
