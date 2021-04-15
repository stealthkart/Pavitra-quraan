package com.scalosphere.labs.kquran.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.data.Constants;
import com.scalosphere.labs.kquran.data.QuranInfo;
import com.scalosphere.labs.kquran.ui.PagerActivity;
import com.scalosphere.labs.kquran.ui.QuranActivity;
import com.scalosphere.labs.kquran.util.QuranUtils;

public class JumpFragment extends SherlockDialogFragment {
    public static final String TAG = "JumpFragment";

    public JumpFragment(){
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        FragmentActivity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.jump_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.CustomTheme_Dialog));
        final Spinner suraSpinner = (Spinner)layout.findViewById(
                R.id.sura_spinner);
        String[] suras = activity.getResources().
                getStringArray(R.array.sura_names_only);
        for (int i=0; i<suras.length; i++){
            suras[i] = QuranUtils.getLocalizedNumber(activity, (i+1)) +
                    ". " + suras[i];
        }
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                activity, android.R.layout.simple_spinner_item, suras);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        suraSpinner.setAdapter(adapter);

        // Ayah Spinner
        final Spinner ayahSpinner = (Spinner)layout.findViewById(
                R.id.ayah_spinner);
        final ArrayAdapter<CharSequence> ayahAdapter =
                new ArrayAdapter<CharSequence>(activity,
                        android.R.layout.simple_spinner_item);
        ayahAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        ayahSpinner.setAdapter(ayahAdapter);

        // Page text
        final EditText input = (EditText)layout.findViewById(R.id.page_number);

        suraSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long rowId) {
                        int sura = position + 1;
                        int ayahCount = QuranInfo.getNumAyahs(sura);
                        CharSequence[] ayahs = new String[ayahCount];
                        for (int i = 0; i < ayahCount; i++){
                            ayahs[i] = String.valueOf(i + 1);
                        }
                        ayahAdapter.clear();

                        for (int i=0; i<ayahCount; i++){
                            ayahAdapter.add(ayahs[i]);
                        }

                        int page = QuranInfo.getPageFromSuraAyah(sura, 1);
                        input.setHint(String.valueOf(page));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });

        ayahSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long rowId) {
                        int ayah = position + 1;
                        int sura = suraSpinner.getSelectedItemPosition() + 1;
                        int page = QuranInfo.getPageFromSuraAyah(sura, ayah);
                        //Log.i("JumpFragment", " on item selected " + sura + " ayah " + ayah + " page " + page);
                        input.setHint(String.valueOf(page));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });

        builder.setView(layout);

       builder.setPositiveButton(getString(R.string.dialog_ok),
              new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                      try {
                          dialog.dismiss();
                          String text = input.getText().toString();
                          if (TextUtils.isEmpty(text)){
                              text = input.getHint().toString();
                          }
                          int ayah= ayahSpinner.getSelectedItemPosition() + 1;
                          int sura = suraSpinner.getSelectedItemPosition() + 1;
                          int page = Integer.parseInt(text);
                          //Log.i("JumpFragment", " on OK click " + sura + " ayah " + ayah + " page " + page);


                          if (page >= Constants.PAGES_FIRST && page
                                  <= Constants.PAGES_LAST) {
                              Activity activity = getActivity();
                              if (activity instanceof QuranActivity) {
                                 // ((QuranActivity) activity).jumpTo(page);
                                  ((QuranActivity) activity).jumpToAndHighlight(page,sura,ayah);
                              }
                              else if (activity instanceof PagerActivity) {
                                  //((PagerActivity) activity).jumpTo(page);
                                  ((PagerActivity) activity).jumpToAndHighlight(page,sura,ayah);
                              }
                          }
                      } catch (Exception e) {
                      }
                  }
              });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button negativeButton = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                Button positiveButton = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);

                // this not working because multiplying white background (e.g. Holo Light) has no effect
                //negativeButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

                final Drawable negativeButtonDrawable = getResources().getDrawable(R.drawable.button_shape);
                final Drawable positiveButtonDrawable = getResources().getDrawable(R.drawable.button_shape);
                if (Build.VERSION.SDK_INT >= 16) {
                    negativeButton.setBackground(negativeButtonDrawable);
                    positiveButton.setBackground(positiveButtonDrawable);
                } else {
                    negativeButton.setBackgroundDrawable(negativeButtonDrawable);
                    positiveButton.setBackgroundDrawable(positiveButtonDrawable);
                }

                negativeButton.invalidate();
                positiveButton.invalidate();
            }
        });

        return dialog;
    }
}
