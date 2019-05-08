package com.example.aurora.moviesineedtowatch.dagger.blocks.towatchfragment;

import com.example.aurora.moviesineedtowatch.ui.main.towatchtab.ToWatchFragmentScreen;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Android Studio.
 * User: Iryna
 * Date: 08/05/19
 * Time: 21:15
 */
@Module
public class ToWatchFragmentScreenModule {
    private final ToWatchFragmentScreen.View mView;

    public ToWatchFragmentScreenModule(ToWatchFragmentScreen.View mView) {
        this.mView = mView;
    }

    @Provides
    ToWatchFragmentScreen.View providesToWatchFragmentScreenView() {
        return mView;
    }
}

