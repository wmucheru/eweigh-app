package org.ilri.eweigh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.marcinorlowski.fonty.Fonty;

import org.ilri.eweigh.misc.InfoActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("About " + getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView txtAbout = findViewById(R.id.txt_about);

        String about = "" +
                "<p>eWeigh will allow farmers to farmers estimate the live-weight (LW) of cattle by\n" +
                "accessing a dedicated, proven algorithm embedded in a smart-phone application,\n" +
                "requiring only a heart-girth measurement as an input.</p>" +
                "<p>v" + BuildConfig.VERSION_NAME + "</p>";

        txtAbout.setText(Html.fromHtml(about));

        findViewById(R.id.btn_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AboutActivity.this, InfoActivity.class);

                // Specify page selected

                startActivity(intent);
            }
        });

        Fonty.setFonts(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
