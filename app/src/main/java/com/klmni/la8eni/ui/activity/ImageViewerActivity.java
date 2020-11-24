package com.klmni.la8eni.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.klmni.la8eni.R;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity
{

    private ImageView imageView;
    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        initialViewer();
        viewerImage();
    }

    private void initialViewer()
    {
        imageView = findViewById(R.id.image_viewer);
    }

    private void viewerImage()
    {
        imageURL = getIntent().getStringExtra("url");

        Picasso.get()
                .load(imageURL)
                .into(imageView);
    }
}