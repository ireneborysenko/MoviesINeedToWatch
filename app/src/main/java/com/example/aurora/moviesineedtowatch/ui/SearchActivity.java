package com.example.aurora.moviesineedtowatch.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.aurora.moviesineedtowatch.R;
import com.example.aurora.moviesineedtowatch.tmdb.API;
import com.example.aurora.moviesineedtowatch.tmdb.Const;
import com.example.aurora.moviesineedtowatch.tmdb.SearchBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.example.aurora.moviesineedtowatch.tmdb.Const.IMAGE_PATH;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.IMAGE_SIZE;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.QUERY;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.TMDB_SEARCH;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.genres;

/**
 * Created by Android Studio.
 * User: Iryna
 * Date: 25/01/18
 * Time: 20:41
 */
public class SearchActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search);

            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connMgr != null;
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new TMDBSearchManager().execute();
            } else {
                TextView textView = new TextView(this);
                textView.setText("No network connection.");
                setContentView(textView);
                Log.e(Const.ERR, "stepErr");
            }
    }

        class TMDBSearchManager extends AsyncTask {

            @Override
            protected ArrayList<SearchBuilder> doInBackground(Object... params) {
                try {
                    return search();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Object result) {

                ArrayList<SearchBuilder> search = (ArrayList<SearchBuilder>) result;
                TableLayout mTable = findViewById(R.id.tlMarksTable);

                TableRow tableRows[] = new TableRow[search.size()];
                for (int i = 0; i < search.size() - 1; i++) {

                    tableRows[i] = new TableRow(SearchActivity.this);
                    tableRows[i].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    tableRows[i].setWeightSum(2.0f);
                    tableRows[i].setPadding(5, 5, 5, 5);

                    //title
                    TextView mTitle = new TextView(SearchActivity.this);
                    mTitle.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                            android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                    mTitle.setText(search.get(i).getTitle());
                    tableRows[i].addView(mTitle);

                    //original title
                    TextView mOTitle = new TextView(SearchActivity.this);
                    mOTitle.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                            android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                    mOTitle.setText(search.get(i).getOriginalTitle());
                    tableRows[i].addView(mOTitle);

                    //get genres
                    String genresString = "";
                    if(search.get(i).getGenreIds().isEmpty())
                    {
                        genresString = "not defined";
                    } else {
                        for (Integer genreId: search.get(i).getGenreIds()) {
                            genresString += genres.get(genreId)[0] + "\n";
                        }
                    }
                    TextView mGenres = new TextView(SearchActivity.this);
                    mGenres.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                            android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                    mGenres.setText(String.valueOf(genresString));
                    tableRows[i].addView(mGenres);

                    //get year
                    TextView mYear = new TextView(SearchActivity.this);
                    mYear.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                            android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                    mYear.setText(search.get(i).getReleaseDate().subSequence(0, 4));
                    tableRows[i].addView(mYear);

                    //get TMDb rating
                    TextView mTMDb = new TextView(SearchActivity.this);
                    mTMDb.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                            android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                    mTMDb.setText(Float.toString(search.get(i).getVoteAverage()));
                    tableRows[i].addView(mTMDb);

                    // get poster
                    ImageView mPoster = new ImageView(SearchActivity.this);
                    mPoster.setLayoutParams(new android.widget.TableRow.LayoutParams(android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                            android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1.0f));

                    String imagePath = IMAGE_PATH + IMAGE_SIZE[3] + search.get(i).getPosterPath();
                    DownloadImageTask r = (DownloadImageTask) new DownloadImageTask().execute(imagePath);

                    try {
                        mPoster.setImageBitmap(r.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    tableRows[i].addView(mPoster);

                    mTable.addView(tableRows[i]);
                }
                Log.e(Const.DEBUG, "we're on the onPostExecute");
            }

            ArrayList<SearchBuilder> search() throws IOException {
                // Build URL
                String stringBuilder = TMDB_SEARCH + "?api_key=" + API.KEY + QUERY + "day%20after%20tomorrow";
                URL url = new URL(stringBuilder);
                Log.e(Const.SEE, url.toString());
                Log.e(Const.TAG,url.toString());

                InputStream stream = null;
                try {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.addRequestProperty("Accept", "application/json");
                    conn.setDoInput(true);
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    Log.e(Const.DEBUG, "The response code is: " + responseCode + " " + conn.getResponseMessage());
                    stream = conn.getInputStream();
                    return parseResult(stringify(stream));
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }

            private ArrayList<SearchBuilder> parseResult(String result) {
                ArrayList<SearchBuilder> results = new ArrayList<>();

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray array = (JSONArray) jsonObject.get("results");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonMovieObject = array.getJSONObject(i);

                        JSONArray gjIds = jsonMovieObject.getJSONArray("genre_ids");
                        ArrayList<Integer> gIds = new ArrayList<>();
                        for (int j=0; j<gjIds.length(); j++) {
                            gIds.add( gjIds.getInt(j) );
                        }

                        SearchBuilder.Builder searchBuilder = SearchBuilder.newBuilder(
                                Integer.parseInt(jsonMovieObject.getString("id")),
                                jsonMovieObject.getString("title"))
                                .setOriginalTitle(jsonMovieObject.getString("original_title"))
                                .setPosterPath(jsonMovieObject.getString("poster_path"))
                                .setReleaseDate(jsonMovieObject.getString("release_date"))
                                .setVoteAverage(Float.parseFloat(jsonMovieObject.getString("vote_average")))
                                .setGenreIds(gIds);
                        results.add(searchBuilder.build());
                    }
                } catch (JSONException e) {
                    System.err.println(e);
                    Log.d(Const.DEBUG, "Error parsing JSON. String was: " + result);
                }

                Log.e(Const.SEE, results.get(1).getTitle());
                Log.e(Const.SEE, results.get(3).getGenreIds().toString());
                return results;
            }

            String stringify(InputStream stream) throws IOException {
                Reader reader = new InputStreamReader(stream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(reader);
                return bufferedReader.readLine();
            }
        }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        DownloadImageTask() {
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap img = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                img = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return img;
        }

        protected void onPostExecute(Bitmap result) {
        }
    }
}
