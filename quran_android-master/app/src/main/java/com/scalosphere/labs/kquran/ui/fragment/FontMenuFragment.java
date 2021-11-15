package com.scalosphere.labs.kquran.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.ui.PagerActivity;
import com.scalosphere.labs.kquran.util.ArabicStyle;
import com.scalosphere.labs.kquran.util.QuranSettings;

public class FontMenuFragment extends DialogFragment {
    public static final String TAG = "FontMenuFragment";

    private String fontFileName;

    public FontMenuFragment(){
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        FragmentActivity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.fonts_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.CustomTheme_Dialog));
        final Spinner fontSpinner = (Spinner)layout.findViewById(
                R.id.font_spinner);
        String[] fonts = activity.getResources().
                getStringArray(R.array.font_names);
        for (int i=0; i<fonts.length; i++){
            /*fonts[i] = QuranUtils.getLocalizedNumber(activity, (i+1)) + fonts[i];*/
            fonts[i] = fonts[i];
        }

        final TextView sampleText = (TextView)layout.findViewById(R.id.sample_text);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                activity, android.R.layout.simple_spinner_item, fonts);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        fontSpinner.setAdapter(adapter);
        String currentFontName= ArabicStyle.getPreferredArabicFontName(getActivity());
       // Log.i(TAG, " currentFontName is "+currentFontName);
        int pos=ArabicStyle.getFontNamePosition(currentFontName);
       // Log.i(TAG, " getFontNamePosition return position is "+pos);
        fontSpinner.setSelection(pos,true);
        fontFileName = ArabicStyle.getFontFiles(pos);
        sampleText.setTextSize(QuranSettings.getTextSize());
        ArabicStyle.applyFont(getActivity(),sampleText,fontFileName);


        fontSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long rowId) {
                        int fontSelected = position;
                        fontFileName = ArabicStyle.getFontFiles(fontSelected);
                        sampleText.setTextSize(QuranSettings.getTextSize());
                        ArabicStyle.applyFont(getActivity(),sampleText,fontFileName);
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
                          ArabicStyle.setFontFile(fontFileName);
                          String currentFontName= ArabicStyle.getPreferredArabicFontName(getActivity());
                          int pos=ArabicStyle.getFontNamePosition(currentFontName);
                          //Log.i(TAG, " position is "+pos);
                          fontSpinner.setSelection(pos,true);

                          //Log.i("FontMenuFragment", "ArabicStyle.getFontFile() is " +ArabicStyle.getPreferredArabicFontName(getActivity()));
                          if(getActivity() instanceof PagerActivity){
                              ((PagerActivity)getActivity()).refreshTranslationPages();
                              //Log.i(TAG, " refreshing the Translation pages");
                          }
                          dialog.dismiss();


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
