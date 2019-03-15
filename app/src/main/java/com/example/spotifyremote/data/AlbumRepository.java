package com.example.spotifyremote.data;

import android.util.Log;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AlbumRepository implements SpotifyAsyncTask.Callback {
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

    public void loadAlbums(String token, String url) {
        mAlbums.setValue(null);
        mLoadingStatus.setValue(Status.LOADING);
        Log.d(TAG, "loading albums from url: " + url);
        new SpotifyAsyncTask(url, this).execute(token);
    }

    @Override
    public void onLoadFinish(SpotifyUtils.SpotifyResponse response) {
        if (response != null) {
            if (response.albums != null && response.albums.items != null) {
                mAlbums.setValue(new ArrayList<>(Arrays.asList(response.albums.items)));
                mLoadingStatus.setValue(Status.SUCCESS);
                return;
            }
            else if (response.error != null && response.error.status == 401) {
                mLoadingStatus.setValue(Status.AUTH_ERR);
                return;
            }
        }
        mLoadingStatus.setValue(Status.ERROR);
    }
}
