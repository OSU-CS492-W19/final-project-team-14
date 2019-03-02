package com.example.spotifyremote.utils;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkUtils {

    private static final OkHttpClient mHTTPClient = new OkHttpClient();

    public static String doHTTPGet(String url) throws IOException {
        Request req = new Request.Builder().url(url).build();
        Response res = mHTTPClient.newCall(req).execute();
        try {
            return res.body().string();
        } finally {
            res.close();
        }
    }

    public static String doHTTPGet(String url, String[] header) throws IOException {
        Request req = new Request.Builder().url(url).addHeader(header[0], header[1]).build();
        Response res = mHTTPClient.newCall(req).execute();
        try {
            return res.body().string();
        } finally {
            res.close();
        }
    }

    public static String doHTTPPut(String url, String[] header, String jsonBody) throws IOException {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader(header[0], header[1])
                .build();

        Response res = mHTTPClient.newCall(request).execute();
        try {
            return res.body().string();
        } finally {
            res.close();
        }
    }
}
