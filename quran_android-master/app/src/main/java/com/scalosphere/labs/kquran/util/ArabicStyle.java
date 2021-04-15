package com.scalosphere.labs.kquran.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.scalosphere.labs.kquran.data.Constants;
import com.scalosphere.labs.kquran.ui.QuranActivity;

public class ArabicStyle {

  private static Typeface mTypeface;
  private static String fontFile = Constants.DEFAULT_ARABIC_FONT; //Custom

  public static String[] fontFileNames = {Constants.DEFAULT_ARABIC_FONT,
           "fonts/kfcNaskh.ttf","fonts/meQuran.ttf",
           "fonts/traditionalArabic.ttf","fonts/xbZar.ttf",
          "fonts/hafs.ttf"
 };

// public static String[] fontNames = {Constants.DEFAULT_ARABIC_FONT,"Naskh","Me Quran","Traditional Arabic","Xb Zar"};

  public static Typeface getTypeface(Context context) {
     // Log.i("ArabicStyle", " In getTypeface value of FONT is  " +  ArabicStyle.fontFile);
      getTypeface(context,fontFile);
    return mTypeface;
  }

  public static Typeface getTypeface(Context context, String fontFile) {
       mTypeface = Typeface.createFromAsset(context.getAssets(), fontFile);
      return mTypeface;
  }

  public static String legacyGetArabicNumbers(String input) {
    char[] retChars = new char[input.length()];
    for (int n = 0; n < input.length(); n++) {
      retChars[n] = input.charAt(n);
      if (retChars[n] >= '0' && retChars[n] <= '9') {
        retChars[n] += 0x0660 - '0';
      }
    }
    StringBuilder ret = new StringBuilder();
    ret.append(retChars);
    return ret.toString();
  }

  public static String reshape(Context context, String text) {
    ArabicReshaper rs = new ArabicReshaper();
    return rs.reshape(text);
  }

   public static void setFontFile(String font) {
      // Log.i("ArabicStyle", " value of current ArabicStyle.fontFile is  " + ArabicStyle.fontFile);
       SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(QuranActivity.getSharedContext());
       String fontFilePrefValue= myPrefs.getString(Constants.PREF_ARABIC_FONT_NAME, Constants.DEFAULT_ARABIC_FONT);
      // Log.i("ArabicStyle", " value of fontfile from pref before committing the new change " + fontFilePrefValue);
       if(!fontFilePrefValue.equalsIgnoreCase(font)){
           myPrefs.edit().putString(Constants.PREF_ARABIC_FONT_NAME,font).commit();
       }
       String fontFilePrefReadValue= myPrefs.getString(Constants.PREF_ARABIC_FONT_NAME, Constants.DEFAULT_ARABIC_FONT);
       //Log.i("ArabicStyle", " value of ArabicStyle.fontFile from pref after committing  " + fontFilePrefReadValue);
       if(fontFilePrefReadValue!=null){
           fontFile = fontFilePrefReadValue;
       }

   }

   public static String getFontFiles(int fonts){
        return fontFileNames[fonts];
   }

    public static int getFontNamePosition(String fontName){
        int position=0;
        for(int i=0; i<fontFileNames.length;i++){
            if(fontName.equalsIgnoreCase(fontFileNames[i])){
                position= i;
                break;
            }
        }
       //Log.i("TAG", " fontName " + fontName + "postion of fontName "+position);
       return position;
    }

    public static void applyFont(Context context, TextView view){
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String fontFilePrefValue= myPrefs.getString(Constants.PREF_ARABIC_FONT_NAME, Constants.DEFAULT_ARABIC_FONT);
        applyFont(context,view,fontFilePrefValue);
    }

   public static void applyFont(Context context, TextView view, String fontFileName){
       //Log.i("ArabicStyle", "font name passed is "+fontFileName);
       if (!fontFileName.equalsIgnoreCase(Constants.DEFAULT_ARABIC_FONT)) {
           Typeface typeface = getTypeface(context, fontFileName);
           view.setTypeface(typeface);
       } else {
           //Log.i("ArabicStyle", "using device default");
           view.setTypeface(null);

       }
   }

    public static String getPreferredArabicFontName(Context context){
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String fontFileName= myPrefs.getString(Constants.PREF_ARABIC_FONT_NAME, Constants.DEFAULT_ARABIC_FONT);
        return fontFileName;
    }
}
