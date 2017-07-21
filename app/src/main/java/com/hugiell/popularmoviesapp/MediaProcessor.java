package com.hugiell.popularmoviesapp;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MediaProcessor {

    public static void playMedia(Context context, String videoKey, String site) {
        switch (site.toLowerCase()) {
            case "youtube": {
                Uri videoUri = Uri.parse("https://www.youtube.com/watch?v=" + videoKey);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(videoUri);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
                break;
            }
        }
    }
}
