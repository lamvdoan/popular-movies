package com.example.android.popular_movies;

import java.util.Properties;

/**
 * Created by lamvdoan on 9/4/17.
 */

public class Configuration {
    public String movieDbApiKey;

    public Configuration() {
        Properties configFile = new java.util.Properties();
        try {
            configFile.load(this.getClass().getClassLoader().getResourceAsStream("credentials"));
            movieDbApiKey = configFile.getProperty("movieDb.apiKey");
        } catch (Exception eta) {
            eta.printStackTrace();
        }
    }

    public String getMovieDbApiKey() {
        return movieDbApiKey;
    }
}
