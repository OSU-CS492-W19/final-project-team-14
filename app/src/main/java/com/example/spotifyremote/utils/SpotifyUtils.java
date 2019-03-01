package com.example.spotifyremote.utils;

import java.io.IOException;

public class SpotifyUtils {
    private static final String SPOTIFY_NEW_RELEASES_URL = "https://api.spotify.com/v1/browse/new-releases";

    public static String doAuthorizedHTTPGet(String url, String token) throws IOException {
        String[] header = {"Authorization", "Bearer " + token};
        return NetworkUtils.doHTTPGet(url, header);
    }

    public static String getNewReleasesUrl() { return SPOTIFY_NEW_RELEASES_URL; }
}
