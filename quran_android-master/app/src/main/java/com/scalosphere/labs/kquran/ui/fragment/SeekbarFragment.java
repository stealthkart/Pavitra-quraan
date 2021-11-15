package com.scalosphere.labs.kquran.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.data.Constants;
import com.scalosphere.labs.kquran.ui.PagerActivity;
import com.scalosphere.labs.kquran.util.QuranSettings;

public class SeekbarFragment extends DialogFragment implements
        SeekBar.OnSeekBarChangeListener {
    //private static final String androidns = "http://schemas.android.com/apk/res/android";

    private SeekBar mSeekBar;
    private TextView mValueText;
    private final int MINIMUM_TEXT_SIZE=Constants.DEFAULT_TEXT_SIZE-5;

    private int mMax, mValue = Constants.DEFAULT_TEXT_SIZE;

    public static final String TAG = "SeekbarFragment";

    public SeekbarFragment() {
        this.mValue=Constants.DEFAULT_TEXT_SIZE;
        this.mMax = 40;
     }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState ) {
        final FragmentActivity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.seekbar_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.CustomTheme_Dialog));
        //builder.setTitle(activity.getString(R.string.text_size));

        // review & use

        mSeekBar = (SeekBar)layout.findViewById(R.id.seekBar_id);
        mSeekBar.setOnSeekBarChangeListener(this);

        mValueText= (TextView)layout.findViewById(R.id.text_size_value);
        mValueText.setTextSize(mValue);
        builder.setView(layout);
        mSeekBar.setMax(mMax);
        setProgress(QuranSettings.getTextSize());

        //TODO add OK & cancel buttons
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    QuranSettings.setTranslationTextSize(mValue);
                   // Log.i(TAG, " setting the new font size "+mValue);
                    if(getActivity() instanceof PagerActivity){
                        ((PagerActivity)getActivity()).refreshTranslationPages();
                        //Log.i(TAG, " refreshing the Translation pages");
                    }
                    dialog.dismiss();
                } catch (Exception e) {
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
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


    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        //Toast.makeText(activity , "Text size changed to:" + progressChanged,
               // Toast.LENGTH_SHORT).show();
    }

    public void onProgressChanged(SeekBar seek, int progress, boolean fromTouch) {
        if (progress < MINIMUM_TEXT_SIZE) {
            setProgress(MINIMUM_TEXT_SIZE);
        }
        else {
            mValue = progress;
            String t = String.valueOf(mValue);
            mValueText.setTextSize(mValue);
            mValueText.setText(t);
        }
    }

    public void setMax(int max) {
        mMax = max;
    }

    public int getMax() {
        return mMax;
    }

    public void setProgress(int progress) {
        mValue = progress;
        if (mSeekBar != null)
            mSeekBar.setProgress(progress);
    }

    public int getProgress() {
        return mValue;
    }

}
