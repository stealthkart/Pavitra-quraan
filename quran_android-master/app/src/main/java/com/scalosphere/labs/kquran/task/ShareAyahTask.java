package com.scalosphere.labs.kquran.task;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.common.QuranAyah;
import com.scalosphere.labs.kquran.data.QuranDataProvider;
import com.scalosphere.labs.kquran.data.QuranInfo;
import com.scalosphere.labs.kquran.data.SuraAyah;
import com.scalosphere.labs.kquran.database.DatabaseHandler;
import com.scalosphere.labs.kquran.ui.PagerActivity;

import java.util.ArrayList;
import java.util.List;

public class ShareAyahTask extends PagerActivityTask<Void, Void, List<QuranAyah>> {
  private SuraAyah start, end;
  private boolean copy;

  public ShareAyahTask(PagerActivity activity, SuraAyah start, SuraAyah end, boolean copy) {
    super(activity);
    this.start = start;
    this.end = end;
    this.copy = copy;
  }

  @Override
  protected List<QuranAyah> doInBackground(Void... params) {
   String footNotesLabel= getActivity().getString(R.string.foot_notes);
    if (start == null || end == null) return null;
     List<QuranAyah> verses = new ArrayList<QuranAyah>();
     List<QuranAyah> kanVerses = new ArrayList<QuranAyah>();
    try {
      DatabaseHandler ayahHandler =
          new DatabaseHandler(getActivity(),
              QuranDataProvider.QURAN_ARABIC_DATABASE);
      Cursor cursor = ayahHandler.getVerses(start.sura, start.ayah,
          end.sura, end.ayah, DatabaseHandler.ARABIC_TEXT_TABLE);
      while (cursor.moveToNext()) {
        QuranAyah verse = new QuranAyah(cursor.getInt(0), cursor.getInt(1));
        verse.setText(cursor.getString(2));
        verses.add(verse);
      }
      cursor.close();
      ayahHandler.closeDatabase();
    }  catch (Exception e){
    }

      try {
          DatabaseHandler ayahHandler =
                  new DatabaseHandler(getActivity(),
                          QuranDataProvider.QURAN_KAN_DATABASE);
          Cursor cursor = ayahHandler.getVerses(start.sura, start.ayah,
                  end.sura, end.ayah, DatabaseHandler.VERSE_TABLE);
          while (cursor.moveToNext()) {
              QuranAyah verse = new QuranAyah(cursor.getInt(0), cursor.getInt(1));
              verse.setText(cursor.getString(2));
              verse.setFootnotes(cursor.getString(3));
              kanVerses.add(verse);
          }
          cursor.close();
          ayahHandler.closeDatabase();
        }
        catch (Exception e){
        }


    for(int i=0; i<verses.size();i++){
       QuranAyah verse= verses.get(i);
       QuranAyah kanVerse= kanVerses.get(i);
       String kanText = kanVerse.getText();
       kanText=kanText.replaceAll("<sup>","[");
       kanText= kanText.replaceAll("</sup>","]");
       String kanFN= null;
       try {
            kanFN = kanVerse.getFootnotes();
            //Log.i("ShareAyahTask", "1. kanFN is " +kanFN);
            //kanFN = kanFN.replaceAll("<br/>","\n");
       }catch (Exception e){
            Log.e("ShareAyahTask", " getFootnotes exception " +e);
       }


       if(!TextUtils.isEmpty(kanText)) {
            verse.setText(verse.getText() + "\n\n" + kanText + "\n\n");
            //Log.i("ShareAyahTask", "1. verse.getText() " +verse.getText() + " kanText " +kanText);
           if(!TextUtils.isEmpty(kanFN)) {
               verse.setText(verse.getText() +footNotesLabel + " : \n" + kanFN+ "\n");
               //Log.i("ShareAyahTask", "2. verse.getText() " +verse.getText() + " footNotesLabel " +footNotesLabel+ " kanText " +kanText);
           }
        }
    }
    //Log.i("ShareAyahTask", " prepared verses are" +verses);
    return verses;
  }

  @Override
  protected void onPostExecute(List<QuranAyah> verses) {
    super.onPostExecute(verses);
    Activity activity = getActivity();
      LayoutInflater inflater = activity.getLayoutInflater();
      View layout = inflater.inflate(R.layout.toast,(ViewGroup) activity.findViewById(R.id.toast_layout_root));

      TextView textview = (TextView) layout.findViewById(R.id.text);

      Toast toast = new Toast(activity);
      toast.setGravity(Gravity.TOP, 0, 0);
      toast.setDuration(Toast.LENGTH_SHORT);
      toast.setView(layout);
      textview.setText(R.string.ayah_copied_popup);
      //New code ends here

    if (verses != null && !verses.isEmpty() && activity != null) {
      StringBuilder sb = new StringBuilder();
      // TODO what's the best text format for multiple ayahs
      int count = 0;
      QuranAyah firstAayah = verses.get(0);
      for (QuranAyah verse : verses) {
        // append ( before ayah start
       /* if (count == 0) {
          sb.append("{");
        }*/
        sb.append(verse.getText());

          sb.append("\n");

        // append * between ayat
        if (count < verses.size() - 1) {
              sb.append(" ... ");
          }
        count++;
        // append ) after last ayah

        // prepare ayat labels
        if (count == verses.size()) {
          //sb.append("}");

          // append [ before sura label
          sb.append("[");
            // Orginal sb.append(QuranInfo.getSuraName(activity, firstAayah.getSura(), false));
          sb.append(QuranInfo.getSuraNameOnly(activity, firstAayah.getSura(), false));
          sb.append(" : ");
          if (count > 1) {
                sb.append(firstAayah.getAyah()).append(" - ");
            }
          if (firstAayah.getSura() != verse.getSura()) {
            sb.append(QuranInfo.getSuraNameOnly(activity, verse.getSura(), false));
            sb.append(" :");
          }
          sb.append(verse.getAyah());
          // close sura label
          sb.append("]").append("\n");
        }
      }
      sb.append(activity.getString(R.string.via_string)).append("\n http://bit.ly/KanQuraan");
      String text = sb.toString();
      if (copy) {
        ClipboardManager cm = (ClipboardManager)activity.
            getSystemService(Activity.CLIPBOARD_SERVICE);
        if (cm != null){
          cm.setText(text);

          toast.show();
//          Toast.makeText(activity, activity.getString(
//                  R.string.ayah_copied_popup),
//              Toast.LENGTH_SHORT
//          ).show();
        }
      } else {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(intent,
            activity.getString(R.string.share_ayah_text)));
      }
    }
  }
}
