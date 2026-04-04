package com.example.warranymanagement.warranty;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.warranymanagement.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageFullScreenActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL = "imageUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_fullscreen);

        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        ImageView ivFullScreen = findViewById(R.id.ivFullScreen);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImageFromUrl(imageUrl, ivFullScreen);
        } else {
            Toast.makeText(this, "No image URL provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        ivFullScreen.setOnClickListener(v -> finish());
    }

    private void loadImageFromUrl(String urlString, ImageView imageView) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream input = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setDoInput(true);
                connection.setRequestProperty("Accept", "image/*");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.connect();

                int code = connection.getResponseCode();
                if (code < 200 || code >= 300) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Failed to load image: HTTP " + code, Toast.LENGTH_SHORT).show());
                    return;
                }

                input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                if (bitmap != null) {
                    runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    if (input != null) input.close();
                } catch (Exception ignored) {}
                if (connection != null) connection.disconnect();
            }
        }).start();
    }
}