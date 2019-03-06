package com.example.spotifyremote.data;

import android.util.Log;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AlbumRepository implements AlbumAsyncTask.Callback {
    private static final String TAG = AlbumRepository.class.getSimpleName();

    private MutableLiveData<ArrayList<SpotifyUtils.SpotifyAlbum>> mAlbums;
    private MutableLiveData<Status> mLoadingStatus;

    public AlbumRepository() {
        mAlbums = new MutableLiveData<>();
        mAlbums.setValue(null);

        mLoadingStatus= new MutableLiveData<>();
        mLoadingStatus.setValue(Status.SUCCESS);
    }

    public LiveData<ArrayList<SpotifyUtils.SpotifyAlbum>> getAlbums() { return mAlbums; }
    public LiveData<Status> getLoadingStatus() { return mLoadingStatus; }

    public void loadAlbums(String token) {
        mAlbums.setValue(null);
        mLoadingStatus.setValue(Status.LOADING);
        String url = SpotifyUtils.getNewReleasesUrl();
        Log.d(TAG, "loading albums from url: " + url);
        new AlbumAsyncTask(url, this).execute(token);
    }

    @Override
    public void onLoadFinish(ArrayList<SpotifyUtils.SpotifyAlbum> albums) {
        mAlbums.setValue(albums);
        if (albums != null) {
            mLoadingStatus.setValue(Status.SUCCESS);
        } else {
            mLoadingStatus.setValue(Status.ERROR);
        }
    }
}
