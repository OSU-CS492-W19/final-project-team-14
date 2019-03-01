package com.example.spotifyremote.data;

import android.os.AsyncTask;
import android.util.Log;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.io.IOException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SpotifyViewModel extends ViewModel {
    private static final String TAG = SpotifyViewModel.class.getSimpleName();

    private String mAuthToken = null;
    private MutableLiveData<String> json = null;

    public LiveData<String> getNewReleases(String url) {
        if (json == null) {
            json = new MutableLiveData<>();
            loadNewReleases(url);
        }
        else Log.d(TAG, "returning cached releases");
        return json;
    }

    public void loadNewReleases(String url) {
        Log.d(TAG, "making async query to url: " + url);

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... urls) {
                String url = urls[0];
                String json = null;
                try {
                    json = SpotifyUtils.doAuthorizedHTTPGet(url, mAuthToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return json;
            }

            @Override
            protected void onPostExecute(String s) {
                json.setValue(s);
            }
        }.execute(url);
    }

    public void setAuthToken(String token) {
        mAuthToken = token;
    }

    public String getAuthToken() {
        return mAuthToken;
    }
}
