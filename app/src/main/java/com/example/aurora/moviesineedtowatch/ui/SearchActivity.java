package com.example.aurora.moviesineedtowatch.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.aurora.moviesineedtowatch.R;
import com.example.aurora.moviesineedtowatch.tmdb.API;
import com.example.aurora.moviesineedtowatch.tmdb.Const;
import com.example.aurora.moviesineedtowatch.tmdb.DB;
import com.example.aurora.moviesineedtowatch.tmdb.MovieBuilder;
import com.example.aurora.moviesineedtowatch.tmdb.SearchBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.example.aurora.moviesineedtowatch.tmdb.Const.EN;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.RU;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.IMAGE_PATH;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.IMAGE_SIZE;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.IMDb_MOVIE;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.QUERY;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.SEE;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.SHARED_REFERENCES;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.TMDB_MOVIE;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.TMDB_SEARCH;
import static com.example.aurora.moviesineedtowatch.tmdb.Const.genres;

/**
 * Created by Android Studio.
 * User: Iryna
 * Date: 25/01/18
 * Time: 20:41
 */
public class SearchActivity extends AppCompatActivity {
    private Switch s;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search);

        final EditText editText = findViewById(R.id.search_query);
        ImageButton button = findViewById(R.id.search_button);

        editText.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    runSearch(editText);
                    return true;
                }
                return false;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                runSearch(editText);
            }
        });

        s = findViewById(R.id.switchToEN);
        SharedPreferences settings = getSharedPreferences(SHARED_REFERENCES, MODE_PRIVATE);
        boolean set = settings.getBoolean("lang_key", false);
        s.setChecked(set);

        s.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SharedPreferences.Editor editor = getSharedPreferences(SHARED_REFERENCES, MODE_PRIVATE).edit();
                editor.putBoolean("lang_key", s.isChecked());
                editor.apply();
            }
        });
    }


    @SuppressLint("SetTextI18n")
    void runSearch(EditText editText) {
        String searchQuery = editText.getText().toString();
        Log.e(Const.DEBUG, searchQuery);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new TMDBSearchManager().execute(searchQuery);
        } else {
            TextView textView = new TextView(SearchActivity.this);
            textView.setText("No network connection.");
            setContentView(textView);
            Log.e(Const.ERR, "stepErr");
        }
    }

    class TMDBSearchManager extends AsyncTask <String, Void, ArrayList<SearchBuilder>>{

        @Override
        protected void onPreExecute() {
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected ArrayList<SearchBuilder> doInBackground(String... params) {
            String searchQuery = params[0];
            try {
                return search(searchQuery);
            } catch (IOException e) {
                return null;
            }
        }

        @SuppressLint("ResourceType")
        @Override
        protected void onPostExecute(ArrayList<SearchBuilder> search) {
            Log.e(Const.DEBUG, String.valueOf(search.size()));

            TableLayout mTable = findViewById(R.id.searchTable);

            mTable.removeAllViewsInLayout();

            final Typeface font = Typeface.createFromAsset(getAssets(), "comic_relief.ttf");

            for (int i = 0; i < search.size(); i++) {
                RelativeLayout tr = new RelativeLayout(SearchActivity.this);

                final String movieId = String.valueOf(search.get(i).getId());

                //get poster
                ImageView mPoster = new ImageView(SearchActivity.this);
                mPoster.setId(1);
                String imagePath = IMAGE_PATH + IMAGE_SIZE[3] + search.get(i).getPosterPath();
                RequestOptions options = new RequestOptions()
                        .error(R.drawable.noposter)
                        .skipMemoryCache(true);

                Glide.with(SearchActivity.this)
                        .load(imagePath)
                        .apply(options)
                        .into(mPoster);

                //title
                TextView mTitle = new TextView(SearchActivity.this);
                mTitle.setId(2);
                mTitle.setText(search.get(i).getTitle());
                mTitle.setTypeface(font, Typeface.BOLD);
                mTitle.setTextColor(getResources().getColor(R.color.colorBlue));
                mTitle.setGravity(Gravity.CENTER);
                mTitle.setTextSize(16);

                //original title
                TextView mOTitle = new TextView(SearchActivity.this);
                mOTitle.setId(3);
                mOTitle.setText(search.get(i).getOriginalTitle());
                mOTitle.setTypeface(font, Typeface.ITALIC);

                //get genres
                StringBuilder genresString = new StringBuilder();
                if (search.get(i).getGenreIds().isEmpty())
                    genresString = new StringBuilder("not defined");
                else {
                    for (Integer genreId : search.get(i).getGenreIds()) {
                        genresString.append(genres.get(genreId)[s.isChecked() ? 0 : 1]).append("\n");
                    }
                }
                TextView mGenres = new TextView(SearchActivity.this);
                mGenres.setId(4);
                mGenres.setText(String.valueOf(genresString.toString()));
                mGenres.setTypeface(font, Typeface.BOLD);
                mGenres.setTextColor(getResources().getColor(R.color.colorLightBlue));

                //get TMDb rating
                TextView mTMDb = new TextView(SearchActivity.this);
                mTMDb.setId(5);
                mTMDb.setText(String.valueOf(search.get(i).getVoteAverage()));
                mTMDb.setTypeface(font, Typeface.BOLD);
                mTMDb.setTextSize(15);

                //get year
                TextView mYear = new TextView(SearchActivity.this);
                mYear.setId(6);
                mYear.setTypeface(font);
                if (!Objects.equals(search.get(i).getReleaseDate(), ""))
                    mYear.setText(search.get(i).getReleaseDate().subSequence(0, 4));
                else mYear.setText("----");

                RelativeLayout.LayoutParams posterParams = new RelativeLayout.LayoutParams(230, 485);
                posterParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                posterParams.setMargins(20, 5, 20, 20);
                tr.addView(mPoster, posterParams);

                RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                titleParams.addRule(RelativeLayout.ALIGN_TOP);
                titleParams.setMargins(0, 10, 0, 10);
                tr.addView(mTitle, titleParams);

                RelativeLayout.LayoutParams oTitleParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                oTitleParams.addRule(RelativeLayout.RIGHT_OF, mPoster.getId());
                oTitleParams.addRule(RelativeLayout.BELOW, mTitle.getId());
                oTitleParams.setMargins(0, 0, 10, 20);
                tr.addView(mOTitle, oTitleParams);

                RelativeLayout.LayoutParams genresParams = new RelativeLayout.LayoutParams(
                        320, RelativeLayout.LayoutParams.WRAP_CONTENT);
                genresParams.addRule(RelativeLayout.RIGHT_OF, mPoster.getId());
                genresParams.addRule(RelativeLayout.BELOW, mOTitle.getId());
                genresParams.setMargins(10, 0, 10, 10);
                tr.addView(mGenres, genresParams);

                RelativeLayout.LayoutParams tmdbParams = new RelativeLayout.LayoutParams(
                        50, RelativeLayout.LayoutParams.WRAP_CONTENT);
                tmdbParams.addRule(RelativeLayout.RIGHT_OF, mGenres.getId());
                tmdbParams.addRule(RelativeLayout.BELOW, mOTitle.getId());
                tmdbParams.setMargins(10,0,20, 0);
                tr.addView(mTMDb, tmdbParams);

                RelativeLayout.LayoutParams yearParams = new RelativeLayout.LayoutParams(
                        70, RelativeLayout.LayoutParams.WRAP_CONTENT);
                yearParams.addRule(RelativeLayout.RIGHT_OF, mGenres.getId());
                yearParams.addRule(RelativeLayout.BELOW, mTMDb.getId());
                yearParams.setMargins(0, 0, 0, 0);
                tr.addView(mYear, yearParams);

                tr.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new TMDBMovieManager().execute(movieId);
                    }
                });


                mTable.addView(tr);
            }
            progressBar.setVisibility(View.INVISIBLE);
            Log.e(Const.DEBUG, "we're on the onPostExecute");
        }

        ArrayList<SearchBuilder> search(String searchQuery) throws IOException {

            String encodedQuery = URLEncoder.encode(searchQuery, "UTF-8");
            String stringBuilder = TMDB_SEARCH + (s.isChecked()?EN:RU) + "api_key=" + API.KEY + QUERY + encodedQuery;

            URL url = new URL(stringBuilder);
            Log.e(Const.SEE, url.toString());

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
                return parseSearch(stringify(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        private ArrayList<SearchBuilder> parseSearch(String result) {
            ArrayList<SearchBuilder> results = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray array = (JSONArray) jsonObject.get("results");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonMovieObject = array.getJSONObject(i);
                    JSONArray gjIds = jsonMovieObject.getJSONArray("genre_ids");
                    ArrayList<Integer> gIds = new ArrayList<>();
                    for (int j = 0; j < gjIds.length(); j++) {
                        gIds.add(gjIds.getInt(j));
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
                Log.d(Const.DEBUG, "Error parsing JSON. String was: " + result);
            }
            return results;
        }

        String stringify(InputStream stream) throws IOException {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
        }
    }

    private class TMDBMovieManager extends AsyncTask <String, Void, MovieBuilder>{

        @Override
        protected void onPreExecute() {
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected MovieBuilder doInBackground(String... params) {
            String movieId = params[0];
            try {
                return movie(movieId);
            } catch (IOException e) {
                return null;
            }
        }

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        protected void onPostExecute(MovieBuilder movie) {
            DB db1 = new DB(SearchActivity.this);
//            SQLiteDatabase db = db1.getWritableDatabase();
//            db1.onUpgrade(db, 3,4);
            db1.addMovie(movie);

            SharedPreferences.Editor editor = getSharedPreferences(SHARED_REFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean("db_is_changed", true);
            editor.apply();

            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(SearchActivity.this, "Movie added to the wish list", Toast.LENGTH_SHORT).show();
            Log.e(Const.DEBUG, "we're on the onPostExecute of the movie");
        }

        MovieBuilder movie(String movieId) throws IOException {
            String stringBuilder = TMDB_MOVIE + movieId + (s.isChecked()?EN:RU) + "api_key=" + API.KEY;
            URL url = new URL(stringBuilder);
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
                return parseMovie(stringify(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        private MovieBuilder parseMovie(String result) {

            GsonBuilder gsonBuilder = new GsonBuilder();
            JsonDeserializer<MovieBuilder> deserializer = new JsonDeserializer<MovieBuilder>() {
                @Override
                public MovieBuilder deserialize(JsonElement json, Type typeOfT,
                                                JsonDeserializationContext context) throws JsonParseException {
                    JsonObject jsonObject = json.getAsJsonObject();

                    String id = jsonObject.get("id").getAsString();
                    String title = jsonObject.get("title").getAsString();
                    String originalTitle = jsonObject.get("original_title").getAsString();
                    String originalLanguage = jsonObject.get("original_language").getAsString();
                    String overview = jsonObject.get("overview").getAsString();
                    String tagline = jsonObject.get("tagline").getAsString();
                    String savedLang = String.valueOf(s.isChecked());

                    //parsing yearParams
                    String releaseDate;
                    if (Objects.equals(jsonObject.get("release_date").getAsString(), ""))
                        releaseDate = "----";
                    else
                        releaseDate = (String) jsonObject.get("release_date").getAsString().subSequence(0, 4);

                    //parsing runtime
                    String runtime;
                    String jsonRuntime = jsonObject.get("runtime").getAsString();
                    if(Objects.equals(jsonRuntime, "null") || Objects.equals(jsonRuntime, "0")) {
                        runtime = "unknown";
                    }else {
                        int duration = Integer.parseInt(jsonObject.get("runtime").getAsString());
                        int hours = duration / 60;
                        int minutes = duration % 60;
                        if (s.isChecked())
                            runtime = hours + "h " + minutes + "min";
                        else
                            runtime = hours + "ч " + minutes + "мин";
                    }

                    String tmdb;
                    float tmdb_rating = Float.parseFloat(jsonObject.get("vote_average").getAsString());
                    if (tmdb_rating == 0.0f) tmdb = "none";
                    else tmdb = String.valueOf(tmdb_rating);

                    int voteCount = jsonObject.get("vote_count").getAsInt();

                    //get imdb rating
                    String imdbId;
                    String rating;
                    String json_IMDB_result = jsonObject.get("imdb_id").getAsString();
                    if(json_IMDB_result.equals("null") || json_IMDB_result.equals("") ){
                        imdbId = "none";
                        rating = "none";
                    } else {
                        imdbId = jsonObject.get("imdb_id").getAsString();

                        Document doc = null;
                        try {
                            doc = Jsoup.connect(IMDb_MOVIE + json_IMDB_result).get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        assert doc != null;
                        if (doc.select("span[itemprop = ratingValue]").first() == null) {
                            rating = "none";
                        } else {
                            Element rat = doc.select("span[itemprop = ratingValue]").first();
                            rating = rat.ownText();
                        }
                    }

                    //get picture bitmap
                    String posterPath = jsonObject.get("poster_path").getAsString();
                    Bitmap img = null;
                    try {
                        if (!Objects.equals(posterPath, "null")) {
                            String urldisplay = IMAGE_PATH + IMAGE_SIZE[3] + posterPath;
                            RequestOptions options = new RequestOptions()
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE);
                        
                            img = Glide
                                    .with(SearchActivity.this)
                                    .asBitmap()
                                    .load(urldisplay)
                                    .apply(options)
                                    .submit()
                                    .get();
                        

                        } else img = BitmapFactory.decodeResource(getResources(), R.drawable.noposter);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }


                    //parsing genres ids
                    JsonArray ids = jsonObject.get("genres").getAsJsonArray();
                    ArrayList<Integer> arrGenres = new ArrayList<>();
                    for (int i = 0; i < ids.size(); i++) {
                        JsonObject jObject = ids.get(i).getAsJsonObject();
                        arrGenres.add(jObject.get("id").getAsInt());
                    }

                    //parsing production companies
                    JsonArray companies = jsonObject.get("production_companies").getAsJsonArray();
                    ArrayList<String> arrCompanies = new ArrayList<>();
                    for (int i = 0; i < companies.size(); i++) {
                        JsonObject jObject = companies.get(i).getAsJsonObject();
                        arrCompanies.add(jObject.get("name").getAsString());
                    }

                    //parsing production countries
                    JsonArray countries = jsonObject.get("production_countries").getAsJsonArray();
                    ArrayList<String> arrCountries = new ArrayList<>();
                    for (int i = 0; i < countries.size(); i++) {
                        JsonObject jObject = countries.get(i).getAsJsonObject();
                        arrCountries.add(jObject.get("iso_3166_1").getAsString());
                    }

                    return new MovieBuilder(
                            id,
                            imdbId,
                            rating,
                            title,
                            originalTitle,
                            originalLanguage,
                            overview,
                            posterPath,
                            img,
                            releaseDate,
                            tagline,
                            runtime,
                            tmdb,
                            voteCount,
                            arrGenres,
                            arrCompanies,
                            arrCountries,
                            savedLang
                    );
                }
            };
            gsonBuilder.registerTypeAdapter(MovieBuilder.class, deserializer);

            Gson customGson = gsonBuilder.create();

            return customGson.fromJson(result, MovieBuilder.class);
        }

        String stringify(InputStream stream) throws IOException {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
        }

    }
}
