package com.example.spotifyremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AuthenticatableActivity implements AlbumAdapter.OnAlbumClickListener{
    private static final String TAG = SearchActivity.class.getSimpleName();

    private EditText mSearchBoxET;
    private RecyclerView mAlbumsRV;

    private AlbumViewModel mAlbumViewModel;
    private AlbumAdapter mAlbumAdapter;

    private Toast mToast;

    private void toast(String msg) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mAlbumViewModel = ViewModelProviders.of(this).get(AlbumViewModel.class);
        mAlbumAdapter = new AlbumAdapter(this);
        mAlbumViewModel.setAuthToken(getAuthToken());

        mAlbumsRV = findViewById(R.id.rv_albums);
        mAlbumsRV.setAdapter(mAlbumAdapter);
        mAlbumsRV.setLayoutManager(new LinearLayoutManager(this));
        mAlbumsRV.setHasFixedSize(true);

        mSearchBoxET = findViewById(R.id.et_search_box);
        Button searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = mSearchBoxET.getText().toString();
                if (!TextUtils.isEmpty(searchQuery)) {
                    doSearch(searchQuery);
                }
            }
        });

        mAlbumViewModel.getAlbums().observe(this, new Observer<ArrayList<SpotifyUtils.SpotifyAlbum>>() {
            @Override
            public void onChanged(ArrayList<SpotifyUtils.SpotifyAlbum> albums) {
                mAlbumAdapter.updateAlbums(albums);
            }
        });
    }

    @Override
    protected void onPostAuthSuccess() {

    }

    private void doSearch(String query) {
        String url = SpotifyUtils.buildSearchURL(query);
        Log.d(TAG, "querying search URL: " + url);

        mAlbumViewModel.loadAlbums(url);
    }

    @Override
    public void onAlbumClick(SpotifyUtils.SpotifyAlbum album) {
        final String DEFAULT = getString(R.string.pref_device_id_default);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_device_key), Context.MODE_PRIVATE);
        String deviceID = sharedPreferences.getString(getString(R.string.pref_device_id_key), DEFAULT);
        if (!TextUtils.equals(deviceID, DEFAULT)) {
            Log.d(TAG, "playing \"" + album.uri + "\" to device: " + deviceID);
            new SearchActivity.PlayContextOnDeviceTask().execute(album.uri, deviceID, getAuthToken());
        }
    }

    class PlayContextOnDeviceTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String uri = params[0];
            String id = params[1];
            String token = params[2];
            String res = SpotifyUtils.playContextURIOnDevice(uri, id, token);
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                SpotifyUtils.SpotifyResponse response = SpotifyUtils.parseResponseJSON(s);
                if (response != null && response.error != null) {
                    if (response.error.status == 403) toast(getString(R.string.playback_error_premium_required));
                }
            } else {
                toast(getString(R.string.playback_successful));
            }
        }
    }
}