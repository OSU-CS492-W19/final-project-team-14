package com.example.spotifyremote;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.spotifyremote.utils.ScrollingTextView;
import com.example.spotifyremote.utils.SpotifyUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlbumDetailActivity extends AuthenticatableActivity {

    private static final String TAG = AlbumDetailActivity.class.getSimpleName();

    private Toast mToast;

    private void toast(String msg) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    private SpotifyUtils.SpotifyAlbum mAlbum;

    private ImageView mAlbumArtIV;
    private Button mPlayLocalB;
    private Button mPlayRemoteB;
    private ScrollingTextView mTitleSTV;
    private TextView mArtistTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);

        mAlbumArtIV = findViewById(R.id.iv_album_art);
        mTitleSTV = findViewById(R.id.stv_title);
        mPlayLocalB = findViewById(R.id.b_play_locally);
        mPlayRemoteB = findViewById(R.id.b_play_remote);
        mArtistTV = findViewById(R.id.tv_artist);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SpotifyUtils.SPOTIFY_ALBUM_EXTRA)) {
            mAlbum = (SpotifyUtils.SpotifyAlbum) intent.getSerializableExtra(SpotifyUtils.SPOTIFY_ALBUM_EXTRA);


            final ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(mAlbum.artists[0].name);

            mTitleSTV.setText(mAlbum.name);
            mArtistTV.setText(mAlbum.artists[0].name);

            Glide.with(this)
                    .asBitmap()
                    .load(mAlbum.images[0].url)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            if (resource != null) {
                                Palette palette = Palette.from(resource).generate();

                                int color = palette.getMutedColor(ContextCompat.getColor(mAlbumArtIV.getContext(), R.color.colorPrimary));

                                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    getWindow().setStatusBarColor(ColorUtils.setAlphaComponent(color, 32));
                                    getWindow().getDecorView().setBackgroundColor(ColorUtils.setAlphaComponent(color, 75));
                                }
                                mPlayLocalB.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                                mPlayRemoteB.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                            }
                            return false;
                        }
                    })
                    .into(mAlbumArtIV);

            mPlayRemoteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String DEFAULT = getString(R.string.pref_device_id_default);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mPlayLocalB.getContext());
                    String deviceID = preferences.getString(getString(R.string.pref_device_id_key), getString(R.string.pref_device_id_default));
                    if (!TextUtils.equals(deviceID, DEFAULT)) {
                        Log.d(TAG, "playing \"" + mAlbum.uri + "\" to device: " + deviceID);
                        new PlayContextOnDeviceTask().execute(mAlbum.uri, deviceID, getAuthToken());
                    }
                }
            });

            mPlayLocalB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SpotifyUtils.isSpotifyInstalled(mPlayLocalB.getContext().getPackageManager())) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(mAlbum.uri));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                            intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + mPlayLocalB.getContext().getPackageName()));

                        startActivity(intent);
                    } else {
                        final String appPackageName = "com.spotify.music";
                        final String referrer = "adjust_campaign=PACKAGE_NAME&adjust_tracker=ndjczk&utm_source=adjust_preinstall";
                        try {
                            Uri uri = Uri.parse("market://details")
                                    .buildUpon()
                                    .appendQueryParameter("id", appPackageName)
                                    .appendQueryParameter("referrer", referrer)
                                    .build();
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        } catch (android.content.ActivityNotFoundException ignored) {
                            Uri uri = Uri.parse("https://play.google.com/store/apps/details")
                                    .buildUpon()
                                    .appendQueryParameter("id", appPackageName)
                                    .appendQueryParameter("referrer", referrer)
                                    .build();
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onPostAuthSuccess() {
        return;
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
