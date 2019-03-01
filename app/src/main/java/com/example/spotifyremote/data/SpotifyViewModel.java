package com.example.spotifyremote.data;

import androidx.lifecycle.ViewModel;

public class SpotifyViewModel extends ViewModel {
    private String mAuthToken = null;

    public void setAuthToken(String token) {
        mAuthToken = token;
    }

    public String getAuthToken() {
        return mAuthToken;
    }
}
