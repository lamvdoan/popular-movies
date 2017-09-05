package com.example.android.popular_movies;

/**
 * Created by lamvdoan on 9/5/17.
 */

public class MovieInfo {
    private int id;
    private String posterPath;

    public MovieInfo(int id, String posterPath) {
        this.id = id;
        this.posterPath = posterPath;
    }

    public int getId() {
        return id;
    }

    public String getPosterPath() {
        return posterPath;
    }
}
