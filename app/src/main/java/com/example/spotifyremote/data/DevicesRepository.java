package com.example.spotifyremote.data;

import android.util.Log;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DevicesRepository implements SpotifyAsyncTask.Callback {
    private static final String TAG = DevicesRepository.class.getSimpleName();

    private MutableLiveData<ArrayList<SpotifyUtils.SpotifyDevice>> mDevices;
    private MutableLiveData<Status> mLoadingStatus;

    public DevicesRepository() {
        mDevices = new MutableLiveData<>();
        mDevices.setValue(null);

        mLoadingStatus = new MutableLiveData<>();
        mLoadingStatus.setValue(Status.SUCCESS);
    }

    public LiveData<ArrayList<SpotifyUtils.SpotifyDevice>> getDevices() { return mDevices; }
    public LiveData<Status> getLoadingStatus() { return mLoadingStatus; }

    public void loadDevices(String token) {
        mDevices.setValue(null);
        mLoadingStatus.setValue(Status.LOADING);
        String url = SpotifyUtils.getDeviceListURL();
        Log.d(TAG, "loading devices from url: " + url);
        new SpotifyAsyncTask(url, this).execute(token);
    }

    @Override
    public void onLoadFinish(SpotifyUtils.SpotifyResponse response) {
        if (response != null) {
            if (response.devices != null) {
                mDevices.setValue(new ArrayList<>(Arrays.asList(response.devices)));
                if (response.devices.length > 0) mLoadingStatus.setValue(Status.SUCCESS);
                else mLoadingStatus.setValue(Status.EMPTY);
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
