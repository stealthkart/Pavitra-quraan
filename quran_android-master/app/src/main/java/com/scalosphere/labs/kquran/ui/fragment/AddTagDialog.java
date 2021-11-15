package com.scalosphere.labs.kquran.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.scalosphere.labs.kquran.R;

public class AddTagDialog extends DialogFragment {
   public static final String TAG = "AddTagDialog";

   private static final String EXTRA_ID = "id";
   private static final String EXTRA_NAME = "name";

   public static AddTagDialog newInstance(long id, String name) {
     final Bundle args = new Bundle();
     args.putLong(EXTRA_ID, id);
     args.putString(EXTRA_NAME, name);
     final AddTagDialog dialog = new AddTagDialog();
     dialog.setArguments(args);
     return dialog;
   }

   public AddTagDialog(){
   }

   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Bundle args = getArguments();

      final long id;
      final String name;
      if (args != null) {
        id = args.getLong(EXTRA_ID, -1);
        name = args.getString(EXTRA_NAME);
      } else {
        id = -1;
        name = null;
      }

      LayoutInflater inflater = getActivity().getLayoutInflater();
      View layout = inflater.inflate(R.layout.tag_dialog, null);

      AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.CustomTheme_Dialog));
      builder.setTitle(getString(R.string.tag_dlg_title));

      final EditText nameText =
              (EditText)layout.findViewById(R.id.tag_name);

      if (id > -1) {
         nameText.setText(name == null? "" : name);
      }

      builder.setView(layout);
      builder.setPositiveButton(getString(R.string.dialog_ok),
              (dialog, which) -> {
                 Activity activity = getActivity();
                 if (activity != null &&
                     activity instanceof OnTagChangedListener){
                    OnTagChangedListener listener =
                            (OnTagChangedListener)activity;
                    String name1 = nameText.getText().toString();
                    if (id > 0){
                       listener.onTagUpdated(id, name1);
                    }
                    else {
                       listener.onTagAdded(name1);
                    }
                 }

                 dialog.dismiss();
              });

      return builder.create();
   }

   public interface OnTagChangedListener {
      public void onTagAdded(String name);
      public void onTagUpdated(long id, String name);
   }
}
