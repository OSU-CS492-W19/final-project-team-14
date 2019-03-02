package com.example.spotifyremote;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.spotifyremote.data.SpotifyViewModel;
import com.example.spotifyremote.utils.SpotifyUtils;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AlbumAdapter.OnAlbumClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private SpotifyViewModel mSpotifyViewModel;
    private AlbumAdapter mAlbumAdapter;


    private RecyclerView mAlbumsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(getString(R.string.new_releases_title));

        mSpotifyViewModel = ViewModelProviders.of(this).get(SpotifyViewModel.class);
        mAlbumAdapter = new AlbumAdapter(this);

        // grab ui
        mAlbumsRV = findViewById(R.id.rv_albums);
        mAlbumsRV.setAdapter(mAlbumAdapter);
        mAlbumsRV.setLayoutManager(new LinearLayoutManager(this));
        mAlbumsRV.setHasFixedSize(true);

        // authenticate if we don't have an access token
        if (mSpotifyViewModel.getAuthToken() == null) {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SpotifyUtils.CLIENT_ID, AuthenticationResponse.Type.TOKEN, SpotifyUtils.REDIRECT_URI);
            builder.setScopes(new String[]{"streaming"});
            AuthenticationRequest request = builder.build();
            AuthenticationClient.openLoginActivity(this, SpotifyUtils.REQUEST_CODE, request);
        }
        else connected();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SpotifyUtils.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    mSpotifyViewModel.setAuthToken(response.getAccessToken());
                    Log.d(TAG, "successfully received access token: " + mSpotifyViewModel.getAuthToken());
                    connected();
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.d(TAG, "failed to authenticate: " + response.getError());
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    private void connected() {
        mSpotifyViewModel.getNewReleases(SpotifyUtils.getNewReleasesUrl()).observe(this, new Observer<ArrayList<SpotifyUtils.SpotifyAlbum>>() {
            @Override
            public void onChanged(ArrayList<SpotifyUtils.SpotifyAlbum> albums) {
                if (albums != null) {
                    mAlbumAdapter.updateAlbums(albums);
                }
            }
        });
    }

    @Override
    public void onAlbumClick(SpotifyUtils.SpotifyAlbum album) {

    }
}
