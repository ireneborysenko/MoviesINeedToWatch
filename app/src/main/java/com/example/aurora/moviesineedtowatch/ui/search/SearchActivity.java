package com.example.aurora.moviesineedtowatch.ui.search;

import static com.example.aurora.moviesineedtowatch.tools.Constants.SHARED_CURRENT_THEME;
import static com.example.aurora.moviesineedtowatch.tools.Constants.SHARED_LANG_KEY;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aurora.moviesineedtowatch.R;
import com.example.aurora.moviesineedtowatch.adaprers.SearchRecyclerAdapter;
import com.example.aurora.moviesineedtowatch.dagger.SharedPreferencesSettings;
import com.example.aurora.moviesineedtowatch.dagger.blocks.searchscreen.DaggerSearchScreenComponent;
import com.example.aurora.moviesineedtowatch.dagger.module.ContextModule;
import com.example.aurora.moviesineedtowatch.dagger.module.SharedPreferencesModule;
import com.example.aurora.moviesineedtowatch.dagger.blocks.searchscreen.SearchScreenModule;
import com.example.aurora.moviesineedtowatch.tmdb.FoundMovie;
import com.example.aurora.moviesineedtowatch.tools.Extensions;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import javax.inject.Inject;

/**
 * Created by Android Studio.
 * User: Iryna
 * Date: 25/01/18
 * Time: 20:41
 */
public class SearchActivity extends AppCompatActivity implements SearchScreen.View {

    @Inject
    SharedPreferencesSettings sharedPreferencesSettings;

    @Inject
    SearchPresenter searchPresenter;

    @BindView(R.id.switchToEN) Switch mSwitch;
    @BindView(R.id.notificationField) TextView mNotificationField;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.search_recycler_view) RecyclerView mRecyclerView;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerSearchScreenComponent.builder()
                .searchScreenModule(new SearchScreenModule(this))
                .sharedPreferencesModule(new SharedPreferencesModule(getApplicationContext()))
                .contextModule(new ContextModule(this))
                .build().inject(this);

        if(sharedPreferencesSettings.contains(SHARED_CURRENT_THEME))
            Extensions.setAppTheme(sharedPreferencesSettings.getBooleanData(SHARED_CURRENT_THEME), R.layout.activity_search, this, this);
        else {
            sharedPreferencesSettings.putBooleanData(SHARED_CURRENT_THEME, false);
            Extensions.setAppTheme(false, R.layout.activity_search, this, this);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorSearchScreen));

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setCustomView(R.layout.switch_lang_layout);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.colorSearchScreen)));

        ButterKnife.bind(this);

        mSwitch.setChecked(sharedPreferencesSettings.getBooleanData(SHARED_LANG_KEY));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search,menu);

        SearchView searchView;
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Resources.getSystem().getDisplayMetrics().widthPixels - mSwitch.getWidth() - 40);
        searchView.setQueryHint("Enter Movie title...");

        searchPresenter.getResultsBasedOnQuery(searchView);

        return super.onCreateOptionsMenu(menu);
    }

    @OnClick(R.id.switchToEN)
    void saveSwitchState() {
        sharedPreferencesSettings.putBooleanData(SHARED_LANG_KEY, mSwitch.isChecked());
    }

    @Override
    public void initRecyclerView(FoundMovie[] search) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final SearchRecyclerAdapter mAdapter =
                new SearchRecyclerAdapter(search, getApplicationContext(), mSwitch);
        mRecyclerView.setAdapter(mAdapter);
        searchPresenter.recyclerViewListener(mAdapter);
    }

    @Override
    public void setNotificationField(String requiredString) {
        mNotificationField.setText(requiredString);
        mNotificationField.setTextSize(20);
    }

    @Override
    public Boolean getSwitchValue() {
        return mSwitch.isChecked();
    }

    @Override
    public void setProgressBarVisible() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setProgressBarInvisible() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showAddedMovieToast(String movieAddMessage) {
        Toast.makeText(SearchActivity.this, movieAddMessage, Toast.LENGTH_SHORT).show();
    }
}