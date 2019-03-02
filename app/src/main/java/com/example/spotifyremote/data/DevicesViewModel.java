package com.example.spotifyremote.data;

import android.os.AsyncTask;
import android.util.Log;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.io.IOException;
import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DevicesViewModel extends ViewModel {
    private static final String TAG = DevicesViewModel.class.getSimpleName();

    private MutableLiveData<ArrayList<SpotifyUtils.SpotifyDevice>> devices = null;
    private String mAuthToken = null;

    public LiveData<ArrayList<SpotifyUtils.SpotifyDevice>> getDevices(String url) {
        if (devices == null) {
            devices = new MutableLiveData<>();
            loadDevices(url);
        } else Log.d(TAG, "returning cached devices");
        return devices;
    }

    public void loadDevices(String url) {
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
                devices.setValue(SpotifyUtils.parseDeviceListJSON(s));
            }
        }.execute(url);
    }

    public void setAuthToken(String token) { mAuthToken = token; }
    public String getAuthToken() { return mAuthToken; }
}
