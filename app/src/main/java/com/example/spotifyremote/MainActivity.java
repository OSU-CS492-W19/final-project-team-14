package com.example.spotifyremote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SearchEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.spotifyremote.data.Status;
import com.example.spotifyremote.utils.SpotifyUtils;
import com.google.android.material.navigation.NavigationView;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;

public class MainActivity extends AuthenticatableActivity implements AlbumAdapter.OnAlbumClickListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private AlbumViewModel mAlbumViewModel;
    private AlbumAdapter mAlbumAdapter;

    private Toast mToast;

    private void toast(String msg) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mAlbumsRV;
    private LinearLayout mLoadingErrorLL;
    private LinearLayout mAuthErrorLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionbar.setTitle(getString(R.string.new_releases_title));

        NavigationView navigationView = findViewById(R.id.nv_nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        mAlbumViewModel = ViewModelProviders.of(this).get(AlbumViewModel.class);
        mAlbumAdapter = new AlbumAdapter(this);

        // grab ui
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAlbumViewModel.loadAlbums(SpotifyUtils.getNewReleasesUrl());
            }
        });

        mLoadingErrorLL = findViewById(R.id.ll_loading_error);
        mAuthErrorLL = findViewById(R.id.ll_auth_error);

        mAlbumsRV = findViewById(R.id.rv_albums);
        mAlbumsRV.setAdapter(mAlbumAdapter);
        mAlbumsRV.setLayoutManager(new LinearLayoutManager(this));
        mAlbumsRV.setHasFixedSize(true);

        if (TextUtils.equals(getAuthToken(), getString(R.string.pref_auth_token_default))) authenticate();
        mAlbumViewModel.setAuthToken(getAuthToken());
        mAlbumViewModel.loadAlbums(SpotifyUtils.getNewReleasesUrl());
        connected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPostAuthSuccess() {
        mAlbumViewModel.setAuthToken(getAuthToken());
        mAlbumViewModel.loadAlbums(SpotifyUtils.getNewReleasesUrl());
    }

    private void connected() {
        mAlbumViewModel.getAlbums().observe(this, new Observer<ArrayList<SpotifyUtils.SpotifyAlbum>>() {
            @Override
            public void onChanged(ArrayList<SpotifyUtils.SpotifyAlbum> albums) {
                mAlbumAdapter.updateAlbums(albums);
            }
        });

        mAlbumViewModel.getLoadingStatus().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                if (status == Status.LOADING) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    mLoadingErrorLL.setVisibility(View.INVISIBLE);
                    mAuthErrorLL.setVisibility(View.INVISIBLE);
                    mAlbumsRV.setVisibility(View.VISIBLE);
                } else if (status == Status.SUCCESS) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mLoadingErrorLL.setVisibility(View.INVISIBLE);
                    mAuthErrorLL.setVisibility(View.INVISIBLE);
                    mAlbumsRV.setVisibility(View.VISIBLE);
                } else if (status == Status.AUTH_ERR) {
                    authenticate();
                    mSwipeRefreshLayout.setRefreshing(false);
                    mLoadingErrorLL.setVisibility(View.INVISIBLE);
                    mAuthErrorLL.setVisibility(View.VISIBLE);
                    mAlbumsRV.setVisibility(View.INVISIBLE);
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mLoadingErrorLL.setVisibility(View.VISIBLE);
                    mAuthErrorLL.setVisibility(View.INVISIBLE);
                    mAlbumsRV.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onAlbumClick(SpotifyUtils.SpotifyAlbum album) {
        final String DEFAULT = getString(R.string.pref_device_id_default);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_device_key), Context.MODE_PRIVATE);
        String deviceID = sharedPreferences.getString(getString(R.string.pref_device_id_key), DEFAULT);
        if (!TextUtils.equals(deviceID, DEFAULT)) {
            Log.d(TAG, "playing \"" + album.uri + "\" to device: " + deviceID);
            new PlayContextOnDeviceTask().execute(album.uri, deviceID, getAuthToken());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_new_releases:
                return true;
            case R.id.nav_devices:
                Intent intent = new Intent(this, DevicesActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }
}
