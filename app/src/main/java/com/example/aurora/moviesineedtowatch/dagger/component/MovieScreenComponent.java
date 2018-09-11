package com.example.aurora.moviesineedtowatch.dagger.component;

import com.example.aurora.moviesineedtowatch.dagger.module.screens.MovieScreenModule;
import com.example.aurora.moviesineedtowatch.ui.movie.MovieActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Android Studio.
 * User: Iryna
 * Date: 19/07/18
 * Time: 14:57
 */
@Singleton
@Component(modules = MovieScreenModule.class)
public interface MovieScreenComponent {
    void inject(MovieActivity movieActivity);
}