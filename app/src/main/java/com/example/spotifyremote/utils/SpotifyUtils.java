package com.example.spotifyremote.utils;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;

public class SpotifyUtils {
    private static final String TAG = SpotifyUtils.class.getSimpleName();

    public static final String CLIENT_ID = "bfca3780fa8947edaa1959febab111b9";
    public static final String REDIRECT_URI = "spotifyremote://spotify/callback";
    public static final int REQUEST_CODE = 1423;
    public static final String[] SPOTIFY_PERMISSIONS = {
            "streaming",
            "user-read-playback-state"
    };

    private static final String SPOTIFY_LIMIT_PARAM = "limit";

    private static final String SPOTIFY_NEW_RELEASES_URL = "https://api.spotify.com/v1/browse/new-releases";
    private static final String SPOTIFY_DEVICE_LIST_URL = "https://api.spotify.com/v1/me/player/devices";

    private static final String SPOTIFY_PLAYBACK_BASE_URL = "https://api.spotify.com/v1/me/player/play";
    private static final String SPOTIFY_PLAYBACK_QUERY_PARAM = "device_id";

    private static final String SPOTIFY_SEARCH_BASE_URL = "https://api.spotify.com/v1/search";
    private static final String SPOTIFY_SEARCH_QUERY_PARAM = "q";
    private static final String SPOTIFY_SEARCH_TYPE_PARAM = "type";

    public static final String SPOTIFY_ALBUM_EXTRA = "SPOTIFY_ALBUM_EXTRA";
    public static String doAuthorizedHTTPGet(String url, String token) throws IOException {
        String[] header = {"Authorization", "Bearer " + token};
        return NetworkUtils.doHTTPGet(url, header);
    }

    /* RESPONSES */
    public static class SpotifyResponse {
        public SpotifyAlbumsList albums;
        public SpotifyDevice[] devices;
        public SpotifyError error;
    }

    public static class SpotifyError {
        public int status;
        public String message;
        public String reason;
    }

    public static SpotifyResponse parseResponseJSON(String json) {
        Gson gson = new Gson();
        SpotifyResponse results = gson.fromJson(json, SpotifyResponse.class);
        return results;
    }

    /* ALBUMS */
    public static class SpotifyAlbumsList {
        public SpotifyAlbum[] items;
    }

    public static class SpotifyAlbum implements Serializable {
        public String album_type;
        public SpotifyArtist[] artists;
        public String id;
        public SpotifyImage[] images;
        public String name;
        public String type;
        public String uri;
    }

    public static class SpotifyArtist implements Serializable {
        public String id;
        public String name;
        public String type;
        public String uri;
    }

    public static class SpotifyImage implements Serializable {
        public int width;
        public int height;
        public String url;
    }

    /* NEW RELEASES */
    public static String buildNewReleasesUrl(int limit) {
        return Uri.parse(SPOTIFY_NEW_RELEASES_URL).buildUpon()
                .appendQueryParameter(SPOTIFY_LIMIT_PARAM, Integer.toString(limit))
                .build()
                .toString();
    }

    public static String buildSearchURL(String query, int limit) {
        return Uri.parse(SPOTIFY_SEARCH_BASE_URL).buildUpon()
                .appendQueryParameter(SPOTIFY_SEARCH_QUERY_PARAM, query)
                .appendQueryParameter(SPOTIFY_SEARCH_TYPE_PARAM, "album")
                .appendQueryParameter(SPOTIFY_LIMIT_PARAM, Integer.toString(limit))
                .build()
                .toString();
    }


    /* DEVICES */
    public static class SpotifyDevice {
        public String id;
        public Boolean is_active;
        public Boolean is_private_session;
        public Boolean is_restricted;
        public String name;
        public String type;
        public int volume_percent;
    }

    public static String getDeviceListURL() { return SPOTIFY_DEVICE_LIST_URL; }

    public static String playContextURIOnDevice(String contextURI, String deviceID, String token) {
        String uri = Uri.parse(SPOTIFY_PLAYBACK_BASE_URL).buildUpon()
                .appendQueryParameter(SPOTIFY_PLAYBACK_QUERY_PARAM, deviceID)
                .build()
                .toString();
        String[] header = {"Authorization", "Bearer " + token};
        String body = "{\"context_uri\":\"" + contextURI + "\"}";

        Log.d(TAG, "PUT to uri: " + uri);
        Log.d(TAG, "PUT with body: " + body);

        try {
            String res = NetworkUtils.doHTTPPut(uri, header, body);
            Log.d(TAG, "PUT res:\n" + res);
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isSpotifyInstalled(PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo("com.spotify.music", 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
