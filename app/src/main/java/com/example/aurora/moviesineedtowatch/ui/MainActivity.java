package com.example.aurora.moviesineedtowatch.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.aurora.moviesineedtowatch.R;
import com.example.aurora.moviesineedtowatch.adaprer.RecyclerAdapter;
import com.example.aurora.moviesineedtowatch.tmdb.Const;
import com.example.aurora.moviesineedtowatch.tmdb.Movie;

import java.util.Objects;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    private static Realm sRealm;
    Typeface mFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);
        mFont = Typeface.createFromAsset(getAssets(), "comic_relief.ttf");

        ImageButton searchButton = findViewById(R.id.main_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                searchTMDB();
            }
        });

        initRealm();
        final RealmResults<Movie> movies = getMoviesFromDB();
        final RecyclerAdapter mAdapter = initRecyclerView(movies);

        movies.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Movie>>() {
            @Override
            public void onChange(@NonNull RealmResults<Movie> results,
                                 @NonNull OrderedCollectionChangeSet changeSet) {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sRealm.close();
    }

    public static Realm getRealm() {
        return sRealm;
    }

    private void initRealm() {
        Realm.init(this);
        sRealm = Realm.getDefaultInstance();
    }

    private RealmResults<Movie> getMoviesFromDB() {
        sRealm.beginTransaction();
        RealmResults<Movie> movies = sRealm.where(Movie.class).findAll();
        sRealm.commitTransaction();
        return movies;
    }

    private RecyclerAdapter initRecyclerView(RealmResults<Movie> movies) {
        final RecyclerView mRecyclerView = findViewById(R.id.movie_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final RecyclerAdapter mAdapter = new RecyclerAdapter(movies, getApplicationContext(), getResources());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new RecyclerAdapter.ClickListener() {
            @Override
            public void onItemClick(View v, String movieId, String dataLang) {
                movieTMDB(movieId, dataLang);
            }

            @Override
            public void onItemLongClick(View v, String movieId, String dataLang) {
                sRealm.beginTransaction();
                RealmResults<Movie> result = sRealm.where(Movie.class)
                        .equalTo("id", movieId)
                        .equalTo("savedLang", dataLang)
                        .findAll();
                result.deleteAllFromRealm();
                sRealm.commitTransaction();
            }
        });
        return mAdapter;
    }

    private void movieTMDB(String movieId, String dataLang) {
        Intent intent = new Intent(this, MovieActivity.class);
        intent.putExtra("EXTRA_MOVIE_ID", movieId);
        intent.putExtra("EXTRA_DATA_LANG", dataLang);
        startActivity(intent);
    }

    private void searchTMDB() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
}