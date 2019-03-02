package com.example.spotifyremote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.spotifyremote.data.DevicesViewModel;
import com.example.spotifyremote.utils.SpotifyUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class DevicesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DevicesAdapter.OnDeviceClickListener {
    private static final String TAG = DevicesActivity.class.getSimpleName();

    private DevicesViewModel mDevicesViewModel;
    private DevicesAdapter mDevicesAdapter;

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDevicesRV;

    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorTV;

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

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mDevicesViewModel = ViewModelProviders.of(this).get(DevicesViewModel.class);
        mDevicesAdapter = new DevicesAdapter(this);

        mLoadingIndicatorPB = findViewById(R.id.pb_loading_indicator);
        mLoadingErrorTV = findViewById(R.id.tv_loading_error_message);

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
        mLoadingIndicatorPB.setVisibility(View.VISIBLE);
        mDevicesViewModel.getDevices(SpotifyUtils.getDeviceListURL()).observe(this, new Observer<ArrayList<SpotifyUtils.SpotifyDevice>>() {
            @Override
            public void onChanged(ArrayList<SpotifyUtils.SpotifyDevice> devices) {
                mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
                if (devices != null) {
                    mLoadingErrorTV.setVisibility(View.INVISIBLE);
                    mDevicesRV.setVisibility(View.VISIBLE);
                    mDevicesAdapter.updateDevices(devices);
                } else {
                    mDevicesRV.setVisibility(View.INVISIBLE);
                    mLoadingErrorTV.setVisibility(View.VISIBLE);
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
