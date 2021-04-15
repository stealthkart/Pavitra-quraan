package com.scalosphere.labs.kquran.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.scalosphere.labs.kquran.common.QuranAyah;
import com.scalosphere.labs.kquran.data.QuranDataProvider;
import com.scalosphere.labs.kquran.data.QuranInfo;
import com.scalosphere.labs.kquran.database.DatabaseHandler;
import com.scalosphere.labs.kquran.ui.PagerActivity;
import com.scalosphere.labs.kquran.util.QuranSettings;
import com.scalosphere.labs.kquran.widgets.TranslationView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ahmedre
 * Date: 5/7/13
 * Time: 11:03 PM
 */
public class TranslationTask extends AsyncTask<Void, Void, List<QuranAyah>> {
   private static final String TAG = "TranslationTask";

   private Context mContext;

   private Integer[] mAyahBounds;
   private int mHighlightedAyah;
   private String mDatabaseName = null;
   private WeakReference<TranslationView> mTranslationView;
   private int mPageNumber;

   public TranslationTask(Context context, Integer[] ayahBounds,
                          String databaseName){
     mContext = context;
     mDatabaseName = databaseName;
     mAyahBounds = ayahBounds;
     mHighlightedAyah = 0;
     mTranslationView = null;

   }

   public TranslationTask(Context context, int pageNumber,
                          int highlightedAyah, String databaseName,
                          TranslationView view){
      mContext = context;
      mDatabaseName = databaseName;
      mPageNumber=pageNumber;
      mAyahBounds = QuranInfo.getPageBounds(pageNumber);
      mHighlightedAyah = highlightedAyah;
      mTranslationView = new WeakReference<TranslationView>(view);

      if (context instanceof PagerActivity){
         ((PagerActivity)context).setLoadingIfPage(pageNumber);
      }
   }

   protected boolean loadArabicAyahText() {
     return QuranSettings.wantArabicInTranslationView(mContext);
   }

   @Override
   protected List<QuranAyah> doInBackground(Void... params) {
      //Log.i(TAG, "Inside Translation Task doInBG for DB "+mDatabaseName) ;
      Integer[] bounds = mAyahBounds;
      if (bounds == null){ return null; }

      String databaseName = mDatabaseName;

      // is this an arabic translation/tafseer or not
      boolean isArabic = mDatabaseName.contains(".ar.") ||
              mDatabaseName.equals("quran.muyassar.db");
      List<QuranAyah> verses = new ArrayList<QuranAyah>();

      try {
         DatabaseHandler translationHandler =
                 new DatabaseHandler(mContext, databaseName);
         Cursor translationCursor =
                 translationHandler.getVerses(bounds[0], bounds[1],
                         bounds[2], bounds[3],
                         DatabaseHandler.VERSE_TABLE);

         DatabaseHandler ayahHandler = null;
         Cursor arabicAyahCursor = null;
         //Log.i(TAG, " flag for loading arabic kannada view "+ QuranSettings.wantArabicInTranslationView(mContext));
         //Log.i(TAG," loadArabicAyahText now? "+ loadArabicAyahText());
         if (loadArabicAyahText()){
            try {
               ayahHandler = new DatabaseHandler(mContext,
                       QuranDataProvider.QURAN_ARABIC_DATABASE);
                arabicAyahCursor = ayahHandler.getVerses(bounds[0], bounds[1],
                       bounds[2], bounds[3],
                       DatabaseHandler.ARABIC_TEXT_TABLE);
            }
            catch (Exception e){
               // ignore any exceptions due to no arabic database
            }
         }
          //Log.i(TAG, "translationCursor is "+translationCursor.getCount()) ;
         if (translationCursor != null) {
            boolean validAyahCursor = false;
            if (arabicAyahCursor != null && arabicAyahCursor.moveToFirst()){
               validAyahCursor = true;
            }

            if (translationCursor.moveToFirst()) {
               do {
                  int sura = translationCursor.getInt(0);
                  int ayah = translationCursor.getInt(1);
                  String translation = translationCursor.getString(2);
                  QuranAyah verse = new QuranAyah(sura, ayah);
                  verse.setTranslation(translation);
                  //Log.i(TAG, " Reading footnotes from " +databaseName);
                  String footNoteText=translationCursor.getString(3);
                  if(footNoteText!=null) {
                      verse.setFootnotes("{"+sura+" : "+ayah+"} "+footNoteText);
                  }
                  if (validAyahCursor){
                     String text = arabicAyahCursor.getString(2);
                     verse.setText(text);
                  }
                  verse.setArabic(isArabic);
                  verses.add(verse);
               }
               while (translationCursor.moveToNext() &&
                       (!validAyahCursor || arabicAyahCursor.moveToNext()));
            }
            translationCursor.close();
            if (arabicAyahCursor != null){
                arabicAyahCursor.close();
            }
         }
         translationHandler.closeDatabase();
         if (ayahHandler != null){
            ayahHandler.closeDatabase();
         }
      }
      catch (Exception e){
         Log.e(TAG, "unable to open " + databaseName + " - " + e);
      }

      return verses;
   }

   @Override
   protected void onPostExecute(List<QuranAyah> result) {
      if (result != null){
         final TranslationView view = mTranslationView == null ? null : mTranslationView.get();
          List<String> notes = new ArrayList<String>();
          for(QuranAyah note :result){
              if(note.getFootnotes()!=null)
              notes.add(note.getFootnotes());
          }

         if (view != null){
            view.setAyahs(result);
            if (mHighlightedAyah > 0){
               // give a chance for translation view to render
               view.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                     view.highlightAyah(mHighlightedAyah);
                  }
               }, 100);
            }
         }
        // Log.i(TAG, "Setting footnotes in PagerActivity for page " + mPageNumber + " and the foot notes are "+notes);
         if(!PagerActivity.hasNotes(mPageNumber)){
             PagerActivity.setNotes(mPageNumber,notes);
         }
         if (mContext != null && mContext instanceof PagerActivity){
            ((PagerActivity)mContext).setLoading(false);

         }
      }
   }
}
