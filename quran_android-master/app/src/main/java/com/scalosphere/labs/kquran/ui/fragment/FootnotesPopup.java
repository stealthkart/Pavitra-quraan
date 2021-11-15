package com.scalosphere.labs.kquran.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;


import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.ui.PagerActivity;

import java.util.ArrayList;
import java.util.List;

public class FootnotesPopup extends DialogFragment {
    public static final String TAG = "FootnotesPopup";
    private View mFootnotesView;
    private Activity mActivity;
    ArrayAdapter<String> adapter;
    private static final String PAGE_NUM = "pageNumber";
    List <String> footNotes ;
    int mPage;

    public static FootnotesPopup newInstance(int page) {
        final FootnotesPopup f = new FootnotesPopup();
        final Bundle args = new Bundle();
        args.putInt(PAGE_NUM, page);
        f.setArguments(args);
        return f;
    }

    public FootnotesPopup(){
     }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mPage = getArguments() != null ?getArguments().getInt(PAGE_NUM) : -1;
        mActivity=getActivity();
        //Log.i(TAG, mActivity.getLocalClassName() + " current page is "+mPage);
        //Log.i(TAG, " activity is " + mActivity);
        mFootnotesView = mActivity.getLayoutInflater().inflate(R.layout.pop_up, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        // builder.setTitle(getString(R.string.foot_notes));

        if(mActivity instanceof PagerActivity)
        {
            footNotes=((PagerActivity)mActivity).getNotes(mPage);
        }
        //footNotes=PagerActivity.getNotes(mPage);

        if(footNotes!=null && footNotes.size()==0)
        {
            footNotes.add(getString(R.string.no_foot_notes));
            //Log.i(TAG, "Footnotes after handing null item "+footNotes + " for page "+mPage);
        }else if(footNotes==null){
            //Log.i(TAG, "Adding an item inside null foot note");
            footNotes= new ArrayList<String>();
            footNotes.add(getString(R.string.no_foot_notes));
            //Log.i(TAG, "Footnotes after handing null list "+footNotes + " for page "+mPage);
        }
        //Log.i(TAG, "Footnotes are "+footNotes + " for page "+mPage + " size of footnotes " + footNotes.size());

        adapter = new ArrayAdapter<String>(mActivity,android.R.layout.simple_list_item_1,footNotes);

        ListView  mFootnotesList = (ListView)mFootnotesView.findViewById(R.id.lv);
        // TODO Need to set the text size from QuranSettings.getTranslationTextSize()

        mFootnotesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {

                //Log.i("long clicked", "->selected footNotes  : " + footNotes.get(pos) );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", footNotes.get(pos));
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mActivity, R.string.footnotes_copied_popup,
                            Toast.LENGTH_SHORT).show();
                }else{
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(footNotes.get(pos));
                    Toast.makeText(mActivity, R.string.footnotes_copied_popup,
                            Toast.LENGTH_SHORT).show();

                }

                return true;
            }
        });

        mFootnotesList.setAdapter(adapter);
        builder.setView(mFootnotesView);
        return builder.create();
    }

}
