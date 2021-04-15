/*
package com.scalosphere.labs.kquran.task;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.scalosphere.labs.kquran.common.QuranNotes;
import com.scalosphere.labs.kquran.data.QuranInfo;
import com.scalosphere.labs.kquran.database.DatabaseHandler;
import com.scalosphere.labs.kquran.ui.PagerActivity;
import com.scalosphere.labs.kquran.ui.fragment.FootnotesPopup;

import java.util.ArrayList;
import java.util.List;


 */
/* Created with IntelliJ IDEA.
 * User: shujath/haroon
 * Date: 25/11/14
 * Time: 14:05
 *//*



public class FootnotesTask extends AsyncTask<Void, Void, List<QuranNotes>> {
    private static final String TAG = "FootnotesTask";

    private Context mContext;

    private Integer[] mAyahBounds;
    private String mDatabaseName = null;
    private FootnotesPopup mFootnotesPopup;

    public FootnotesTask(Context context, int pageNumber,
                         String databaseName,FootnotesPopup mFNPopup, FragmentManager fm) {
        mContext = context;
        mDatabaseName = databaseName;
        mAyahBounds = QuranInfo.getPageBounds(pageNumber);
        mFootnotesPopup=mFNPopup;
    }

    @Override
    protected List<QuranNotes> doInBackground(Void... params) {
        Integer[] bounds = mAyahBounds;
        if (bounds == null) {
            return null;
        }

        String databaseName = mDatabaseName;

        List<QuranNotes> footNotes = new ArrayList<QuranNotes>();

        try {
*/
/*            DatabaseHandler notesHandler =
                    new DatabaseHandler(mContext, databaseName);
            Cursor notesCursor =
                    notesHandler.getNotes(bounds[0], bounds[1],
                            bounds[2], bounds[3],
                            DatabaseHandler.VERSE_TABLE);*//*

*/
/*            if (notesCursor != null) {
               if (notesCursor.moveToFirst()) {
                    do {
                        //int sura = notesCursor.getInt(0);
                        //int ayah = notesCursor.getInt(1);
                        String text = notesCursor.getString(0);
                        QuranNotes notes = new QuranNotes(0, 0);
                        notes.setText(text);
                        footNotes.add(notes);
                    }
                    while (notesCursor.moveToNext());
                }

                if (notesCursor != null) {
                    notesCursor.close();
                }
            }*//*

   */
/*         if (notesHandler != null) {
                notesHandler.closeDatabase();
            }*//*

        } catch (Exception e) {
            Log.e(TAG, "unable to open " + databaseName + " - " + e);
        }
        //Log.i(TAG, " values are " + footNotes);
        return footNotes;
    }

    @Override
    protected void onPostExecute(List<String> result) {
        if (result != null) {
            if (mFootnotesPopup != null) {
                if (mContext != null && mContext instanceof PagerActivity) {
                    ((PagerActivity) mContext).setLoading(false);
                    mFootnotesPopup.setNotes(result);
                   // mFootnotesPopup.show(fragmentManager,FootnotesPopup.TAG);
                }
            }
        }
    }
}*/
