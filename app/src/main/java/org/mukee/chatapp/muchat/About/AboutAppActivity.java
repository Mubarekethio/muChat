package org.mukee.chatapp.muchat.About;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.mukee.chatapp.muchat.R;

import java.util.Objects;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_app);

        // Set Home Activity Toolbar Name
        Toolbar mToolbar = findViewById(R.id.about_page_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("About");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button gitBtn = findViewById(R.id.git_btn);
        Button facebookBtn = findViewById(R.id.facebook_btn);
        Button instaBtn = findViewById(R.id.insta_btn);
        Button linBtn = findViewById(R.id.lin_btn);
        Button twBtn = findViewById(R.id.tw_btn);

        // 4 buttons
        // git button
        gitBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://github.com/Mubarekethio");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        // facebook button
        facebookBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.facebook.com/mubarek.kebede.5");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        // instagram button
        instaBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.instagram.com/mub_f");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        // Linkedin button
        linBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.linkedin.com/in/mubarek-kebede-582012148");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        //twitter button
        twBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://twitter.com/mubarekethio");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });


    }

}
