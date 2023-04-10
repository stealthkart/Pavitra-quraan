package com.scalosphere.labs.kquran;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;



public class HelpActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.QuranAndroid);
    super.onCreate(savedInstanceState);

    this.getActionBar().setDisplayShowHomeEnabled(true);
    this.getActionBar().setDisplayHomeAsUpEnabled(true);

    setContentView(R.layout.help);

    TextView helpText = (TextView) findViewById(R.id.txtHelp);
    helpText.setText(Html.fromHtml(getString(R.string.help)));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return false;
  }
}
