package com.scalosphere.labs.kquran;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class AboutUsActivity extends SherlockActivity {

    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.QuranAndroid);
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.abt_us);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.about_us);
        TextView txtAbout = (TextView) findViewById(R.id.txtAbout);
        txtAbout.setVerticalScrollBarEnabled(true);
        txtAbout.setText(Html.fromHtml(getString(R.string.aboutUsContent)));
        txtAbout.setMovementMethod(LinkMovementMethod.getInstance());

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
