package com.example.spotifyremote.data;

import android.os.AsyncTask;
import android.util.Log;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SpotifyAsyncTask extends AsyncTask<String, Void, String> {

    private String mURL;
    private Callback mCallback;

    public interface Callback {
        void onLoadFinish(SpotifyUtils.SpotifyResponse response);
    }

    public SpotifyAsyncTask(String url, Callback callback) {
        mURL = url;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        if (mURL != null) {
            String results = null;
            try {
                results = SpotifyUtils.doAuthorizedHTTPGet(mURL, strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        } else return null;
    }

    @Override
    protected void onPostExecute(String s) {
        SpotifyUtils.SpotifyResponse response = null;
        if (s != null) response =  SpotifyUtils.parseResponseJSON(s);

        mCallback.onLoadFinish(response);
    }
}
