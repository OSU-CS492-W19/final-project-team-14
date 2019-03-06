package com.example.spotifyremote.data;

import android.util.Log;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DevicesRepository implements DevicesAsyncTask.Callback{
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
        Log.d(TAG, "loading albums from url: " + url);
        new DevicesAsyncTask(url, this).execute(token);
    }

    @Override
    public void onLoadFinish(ArrayList<SpotifyUtils.SpotifyDevice> devices) {
        mDevices.setValue(devices);
        if (devices != null) {
            if (devices.size() > 0) mLoadingStatus.setValue(Status.SUCCESS);
            else mLoadingStatus.setValue(Status.EMPTY);
        } else {
            mLoadingStatus.setValue(Status.ERROR);
        }
    }
}