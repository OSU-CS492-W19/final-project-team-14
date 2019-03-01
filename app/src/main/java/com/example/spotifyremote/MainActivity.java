package com.example.spotifyremote;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.spotifyremote.data.SpotifyViewModel;
import com.example.spotifyremote.utils.SpotifyUtils;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String CLIENT_ID = "bfca3780fa8947edaa1959febab111b9";
    private static final String REDIRECT_URI = "spotifyremote://spotify/callback";
    private static final int REQUEST_CODE = 1423;

    private TextView mMainTV;
    private SpotifyViewModel mSpotifyViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpotifyViewModel = ViewModelProviders.of(this).get(SpotifyViewModel.class);

        // grab ui
        mMainTV = findViewById(R.id.tv_main);

        // authenticate if we don't have an access token
        if (mSpotifyViewModel.getAuthToken() == null) {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
            builder.setScopes(new String[]{"streaming"});
            AuthenticationRequest request = builder.build();
            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        }
        else connected();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
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
        mSpotifyViewModel.getNewReleases(SpotifyUtils.getNewReleasesUrl()).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mMainTV.setText(s);
            }
        });
    }

}
