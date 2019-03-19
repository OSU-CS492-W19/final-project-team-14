package com.example.spotifyremote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    private SharedPreferences mPreferences;

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

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
        int limit = Integer.parseInt(mPreferences.getString(getString(R.string.pref_results_limit_key), getString(R.string.pref_results_limit_default)));
        String url = SpotifyUtils.buildSearchURL(query, limit);
        Log.d(TAG, "querying search URL: " + url);

        mAlbumViewModel.loadAlbums(url);
    }

    @Override
    public void onAlbumClick(SpotifyUtils.SpotifyAlbum album) {
        Intent intent = new Intent(this, AlbumDetailActivity.class);
        intent.putExtra(SpotifyUtils.SPOTIFY_ALBUM_EXTRA, album);
        startActivity(intent);
    }
}
