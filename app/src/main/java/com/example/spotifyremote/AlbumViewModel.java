package com.example.spotifyremote;

import com.example.spotifyremote.data.AlbumRepository;
import com.example.spotifyremote.data.Status;
import com.example.spotifyremote.utils.SpotifyUtils;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class AlbumViewModel extends ViewModel {

    private static final String TAG = AlbumViewModel.class.getSimpleName();

    private LiveData<ArrayList<SpotifyUtils.SpotifyAlbum>> mAlbums;
    private LiveData<Status> mLoadingStatus;
    private AlbumRepository mRepository;
    private String mAuthToken = null;

    public AlbumViewModel() {
        mRepository = new AlbumRepository();
        mAlbums = mRepository.getAlbums();
        mLoadingStatus = mRepository.getLoadingStatus();
    }

    public LiveData<ArrayList<SpotifyUtils.SpotifyAlbum>> getAlbums() { return mAlbums; }
    public LiveData<Status> getLoadingStatus() { return mLoadingStatus; }

    public void loadAlbums(String url) { mRepository.loadAlbums(mAuthToken, url); }

    public void setAuthToken(String token) {
        mAuthToken = token;
    }
}
