package com.example.spotifyremote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.spotifyremote.utils.SpotifyUtils;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class AuthenticatableActivity extends AppCompatActivity {

    private final String TAG = AuthenticatableActivity.class.getSimpleName();

    protected void authenticate() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SpotifyUtils.CLIENT_ID, AuthenticationResponse.Type.TOKEN, SpotifyUtils.REDIRECT_URI);
        builder.setScopes(SpotifyUtils.SPOTIFY_PERMISSIONS);
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, SpotifyUtils.REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SpotifyUtils.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    setAuthToken(response.getAccessToken());
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.d(TAG, "failed to authenticate: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    private void setAuthToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_auth_token_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_auth_token_key), token);
        editor.apply();
        Log.d(TAG, "set auth token: " + token);
    }

    protected String getAuthToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_auth_token_key), Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(getString(R.string.pref_auth_token_key), getString(R.string.pref_auth_token_default));
        Log.d(TAG, "pulled token: " + token);
        return token;
    }
}
