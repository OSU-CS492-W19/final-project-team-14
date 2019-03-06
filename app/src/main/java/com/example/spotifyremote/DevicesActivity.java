package com.example.spotifyremote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.spotifyremote.data.Status;
import com.example.spotifyremote.utils.SpotifyUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class DevicesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DevicesAdapter.OnDeviceClickListener {
    private static final String TAG = DevicesActivity.class.getSimpleName();

    private DevicesViewModel mDevicesViewModel;
    private DevicesAdapter mDevicesAdapter;

    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mDevicesRV;

    private LinearLayout mLoadingErrorLL;
    private LinearLayout mNoDevicesLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionbar.setTitle(getString(R.string.devices_title));

        NavigationView navigationView = findViewById(R.id.nv_nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        mDevicesViewModel = ViewModelProviders.of(this).get(DevicesViewModel.class);
        mDevicesAdapter = new DevicesAdapter(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mDevicesViewModel.loadDevices();
            }
        });

        mLoadingErrorLL = findViewById(R.id.ll_loading_error);
        mNoDevicesLL = findViewById(R.id.ll_no_devices);

        mDevicesRV = findViewById(R.id.rv_devices);
        mDevicesRV.setAdapter(mDevicesAdapter);
        mDevicesRV.setLayoutManager(new LinearLayoutManager(this));
        mDevicesRV.setHasFixedSize(true);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SpotifyUtils.SPOTIFY_AUTH_TOKEN_EXTRA)) {
            String authToken = (String) intent.getSerializableExtra(SpotifyUtils.SPOTIFY_AUTH_TOKEN_EXTRA);
            mDevicesViewModel.setAuthToken(authToken);
            loadDevices();
        }
    }

    private void loadDevices() {
        mDevicesViewModel.loadDevices();
        mSwipeRefreshLayout.setRefreshing(true);

        mDevicesViewModel.getDevices().observe(this, new Observer<ArrayList<SpotifyUtils.SpotifyDevice>>() {
            @Override
            public void onChanged(ArrayList<SpotifyUtils.SpotifyDevice> devices) {
               mDevicesAdapter.updateDevices(devices);
            }
        });

        mDevicesViewModel.getLoadingStatus().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                if (status == Status.LOADING) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    mLoadingErrorLL.setVisibility(View.INVISIBLE);
                    mNoDevicesLL.setVisibility(View.INVISIBLE);
                    mDevicesRV.setVisibility(View.VISIBLE);
                } else if (status == Status.SUCCESS) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mLoadingErrorLL.setVisibility(View.INVISIBLE);
                    mNoDevicesLL.setVisibility(View.INVISIBLE);
                    mDevicesRV.setVisibility(View.VISIBLE);
                } else if (status == Status.EMPTY) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mLoadingErrorLL.setVisibility(View.INVISIBLE);
                    mNoDevicesLL.setVisibility(View.VISIBLE);
                    mDevicesRV.setVisibility(View.INVISIBLE);
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mLoadingErrorLL.setVisibility(View.VISIBLE);
                    mNoDevicesLL.setVisibility(View.INVISIBLE);
                    mDevicesRV.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mDrawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_new_releases:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_devices:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeviceClick(SpotifyUtils.SpotifyDevice device) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_device_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_device_id_key), device.id);
        editor.apply();
        Log.d(TAG, "set chosen device with id: " + device.id);
    }
}
