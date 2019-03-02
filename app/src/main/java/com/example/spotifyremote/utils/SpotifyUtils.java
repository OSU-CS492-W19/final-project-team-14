package com.example.spotifyremote.utils;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SpotifyUtils {
    public static final String CLIENT_ID = "bfca3780fa8947edaa1959febab111b9";
    public static final String REDIRECT_URI = "spotifyremote://spotify/callback";
    public static final int REQUEST_CODE = 1423;
    public static final String SPOTIFY_AUTH_TOKEN_EXTRA = "SPOTIFY_AUTH_TOKEN_EXTRA";
    public static final String[] SPOTIFY_PERMISSIONS = {
            "streaming",
            "user-read-playback-state"
    };

    private static final String SPOTIFY_NEW_RELEASES_URL = "https://api.spotify.com/v1/browse/new-releases";
    private static final String SPOTIFY_DEVICE_LIST_URL = "https://api.spotify.com/v1/me/player/devices";

    public static String doAuthorizedHTTPGet(String url, String token) throws IOException {
        String[] header = {"Authorization", "Bearer " + token};
        return NetworkUtils.doHTTPGet(url, header);
    }

    /* Gson parsing classes */
    public static class SpotifyAlbumResults {
        public SpotifyAlbumsItem albums;
    }

    public static class SpotifyAlbumsItem {
        public SpotifyAlbum[] items;
    }

    public static class SpotifyAlbum {
        public String album_type;
        public SpotifyArtist[] artists;
        public String id;
        public SpotifyImage[] images;
        public String name;
        public String type;
        public String uri;
    }

    public static class SpotifyArtist {
        public String id;
        public String name;
        public String type;
        public String uri;
    }

    public static class SpotifyImage {
        public int width;
        public int height;
        public String url;
    }

    /* NEW RELEASES */
    public static String getNewReleasesUrl() { return SPOTIFY_NEW_RELEASES_URL; }
    public static ArrayList<SpotifyAlbum> parseNewReleasesJSON(String json) {
        Gson gson = new Gson();
        SpotifyAlbumResults results = gson.fromJson(json, SpotifyAlbumResults.class);

        if (results != null && results.albums != null && results.albums.items != null) {
            return new ArrayList<>(Arrays.asList(results.albums.items));
        }
        else return null;
    }

    /* DEVICES */
    public static class SpotifyDeviceList {
        public SpotifyDevice[] devices;
    }

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
    public static ArrayList<SpotifyDevice> parseDeviceListJSON(String json) {
        Gson gson = new Gson();
        SpotifyDeviceList results = gson.fromJson(json, SpotifyDeviceList.class);

        if (results != null && results.devices != null) {
            return new ArrayList<>(Arrays.asList(results.devices));
        }
        else return null;
    }
}
