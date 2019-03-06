package com.example.spotifyremote;


import com.example.spotifyremote.data.DevicesRepository;
import com.example.spotifyremote.data.Status;
import com.example.spotifyremote.utils.SpotifyUtils;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DevicesViewModel extends ViewModel {

    private static final String TAG = DevicesViewModel.class.getSimpleName();

    private LiveData<ArrayList<SpotifyUtils.SpotifyDevice>> mDevices;
    private LiveData<Status> mLoadingStatus;
    private DevicesRepository mRepository;
    private String mAuthToken = null;

    public DevicesViewModel() {
        mRepository = new DevicesRepository();
        mDevices = mRepository.getDevices();
        mLoadingStatus = mRepository.getLoadingStatus();
    }

    public LiveData<ArrayList<SpotifyUtils.SpotifyDevice>> getDevices() { return mDevices; }
    public LiveData<Status> getLoadingStatus() { return mLoadingStatus; }

    public void loadDevices() { mRepository.loadDevices(mAuthToken); }

    public void setAuthToken(String token) { mAuthToken = token; }
    public String getAuthToken() { return mAuthToken; }
}
