package com.example.systeminteraction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.taskButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TaskInfoActivity.class);
            startActivity(intent);
        });
    }

    public void launchCamera(View view) {
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        startActivity(cameraIntent);
    }

    public void pickContact(View view) {
        Intent pickContactIntent = new Intent(this, PickContactActivity.class);
        startActivity(pickContactIntent);
    }

    public void openWebView(View view) {
        Intent webIntent = new Intent(this, WebActivity.class);
        startActivity(webIntent);
    }

    public void launchBrowser(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url)));
        startActivity(browserIntent);
    }
}
