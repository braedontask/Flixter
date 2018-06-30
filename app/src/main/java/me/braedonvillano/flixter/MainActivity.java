package me.braedonvillano.flixter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import me.braedonvillano.flixter.models.Config;
import me.braedonvillano.flixter.models.Movie;

public class MainActivity extends AppCompatActivity {

    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    public final static String API_KEY_PARAM = "api_key";
    // public final static String API_KEY = "a07e22bc18f5cb106bfe4cc1f83ad8ed";
    public final static String TAG = "MovieListActivity";

    AsyncHttpClient client;

    ArrayList<Movie> movies;

    RecyclerView rvMovies;
    MovieAdapter adapter;
    Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new AsyncHttpClient();
        movies = new ArrayList<>();
        getConfiguration();

        adapter = new MovieAdapter(movies);
        rvMovies = (RecyclerView) findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);
    }

    private void getNowPlaying() {
        String url = API_BASE_URL + "/movie/now_playing";

        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                // need to the parse the movies now
                // should come back as an array of objects
                printStuff(res.toString());
                try {
                    JSONArray results = res.getJSONArray("results");
                    // iterate over the response and convert results into movies
                    for (int i = 0; i < results.length(); i++) {
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        adapter.notifyItemInserted(movies.size() - 1);
                    }
                    Log.i(TAG, String.format("Loaded %d movies", results.length()));
                } catch (JSONException e) {
                    logError("failed to parse the movies", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                logError("failed fetching the current movies", t, true);
            }
        });
    }

    private void getConfiguration() {
        String url = API_BASE_URL + "/configuration";

        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));
        // make the request to the client
        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                try {
                    config = new Config(res);
                    adapter.setConfig(config);
                    getNowPlaying();
                } catch (JSONException e) {
                    logError("failed parsing image url or size", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                logError("failed fetching the movies i think", t, true);
            }
        });
    }

    private void logError(String message, Throwable error, boolean alertUser) {
        Log.e(TAG, message, error);
        if (alertUser) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void printStuff(String message) {
        Log.d("*** PRINTED ***", message);
    }
}
