package com.example.spotifyremote.data;

import android.os.AsyncTask;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.io.IOException;
import java.util.ArrayList;

public class DevicesAsyncTask extends AsyncTask<String, Void, String> {

    private String mURL;
    private Callback mCallback;

    public interface Callback {
        void onLoadFinish(ArrayList<SpotifyUtils.SpotifyDevice> devices);
    }

    public DevicesAsyncTask(String url, Callback callback) {
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
        ArrayList<SpotifyUtils.SpotifyDevice> results = null;
        if (s != null) results = SpotifyUtils.parseDeviceListJSON(s);
        mCallback.onLoadFinish(results);
    }
}
