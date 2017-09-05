package com.example.android.popular_movies;

import com.example.android.popular_movies.utils.NetworkUtils;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getURL() {
        final String IMAGE_URL = "https://image.tmdb.org/t/p/w185/";
        final String MOVIE_URL = "https://api.themoviedb.org/3/movie/";
        String jsonMovieResponse = null;

        URL movieRequestUrl = NetworkUtils.buildUrl(MOVIE_URL + "popular");

        try {
            jsonMovieResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(jsonMovieResponse);
    }
}