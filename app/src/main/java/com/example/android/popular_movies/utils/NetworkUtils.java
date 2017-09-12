package com.example.android.popular_movies.utils;

import android.net.Uri;
import android.util.Log;

import com.example.android.popular_movies.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by lamvdoan on 9/4/17.
 */

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static final String MOVIE_URL = "https://api.themoviedb.org/3/movie/";
    final static String QUERY_PARAM = "api_key";

    public static URL buildUrl(String hostname) {
        Uri builtUri = buildUri(MOVIE_URL + hostname);
;
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }
    public static Uri buildUri(String hostname) {
        Configuration configuration = new Configuration();
        String apiKey = configuration.getMovieDbApiKey();

        Uri builtUri = Uri.parse(hostname).buildUpon()
                .appendQueryParameter(QUERY_PARAM, apiKey)
                .build();

        try {
            Log.v(TAG, "Built URI " + new URL(builtUri.toString()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return builtUri;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
