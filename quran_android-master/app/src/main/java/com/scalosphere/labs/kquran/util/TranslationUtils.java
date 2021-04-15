package com.scalosphere.labs.kquran.util;

import android.content.Context;

import com.scalosphere.labs.kquran.common.TranslationItem;

import java.util.List;

public class TranslationUtils {

   private static TranslationItem kanTItem=null;

   private static TranslationItem getKannadaTranslationItem(Context context){

       if(kanTItem!=null){
           return kanTItem;
       }else {
           kanTItem = new TranslationItem(22, "Kannada Translation", "Muhammad Hamzah", 1, "db/quran.kan.hamzah.db", "http://pavitra-quraan.com/db/quran.kan.hamzah.db", true);
           return kanTItem;
       }
   }

    public static TranslationItem getKannadaTranslationItem(){

        if(kanTItem!=null){
            return kanTItem;
        }else {
            kanTItem = new TranslationItem(22, "Kannada Translation", "Muhammad Hamzah", 2, "db/quran.kan.hamzah.db", "http://pavitra-quraan.com/db/quran.kan.hamzah.db", true);
            return kanTItem;
        }
    }


    public static String getDefaultTranslation(Context context,
                                       List<TranslationItem> items) {
     final TranslationItem item = getKannadaTranslationItem(context);
    // original code
    // final TranslationItem item = getDefaultTranslationItem(context, items);
     return item == null ? null : item.filename;
   }

 /* public static TranslationItem getDefaultTranslationItem(Context context,
      List<TranslationItem> items){
      if (items == null || items.size() == 0){ return null; }
      SharedPreferences prefs =
              PreferenceManager.getDefaultSharedPreferences(context);
      final String db =
          prefs.getString(Constants.PREF_ACTIVE_TRANSLATION, null);

      TranslationItem result = null;
      boolean changed = false;
      if (db == null){
         changed = true;
         result = items.get(0);
      }
      else {
         boolean found = false;
         for (TranslationItem item : items){
            if (item.filename.equals(db)){
               found = true;
               result = item;
               break;
            }
         }

         if (!found){
            changed = true;
            result = items.get(0);
         }
      }

      if (changed && result != null){
         prefs.edit().putString(
             Constants.PREF_ACTIVE_TRANSLATION, result.filename)
                 .commit();
      }

      return result;
   }*/
}
