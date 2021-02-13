package org.love2d.luahttps;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Keep
class LuaHTTPS {
    static private String TAG = "LuaHTTPS";

    private String urlString;
    private byte[] postData;
    private byte[] response;
    private int responseCode;
    private HashMap<String, String> headers;

    public LuaHTTPS() {
        headers = new HashMap<String, String>();
        reset();
    }

    public void reset() {
        urlString = null;
        postData = null;
        response = null;
        responseCode = 0;
        headers.clear();
    }

    public void setUrl(String url) {
        urlString = url;
    }

    public void setPostData(byte[] postData) {
        this.postData = postData;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String[] getInterleavedHeaders() {
        String[] result = new String[headers.size() * 2];
        int i = 0;

        for (Map.Entry<String, String> header: headers.entrySet()) {
            result[i * 2] = header.getKey();
            result[i * 2 + 1] = header.getValue();
            i++;
        }

        return result;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public byte[] getResponse() {
        return response;
    }

    public boolean request() {
        if (urlString == null) {
            return false;
        }

        URL url;
        try {
            url = new URL(urlString);

            if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
                return false;
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error", e);
            return false;
        }

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.e(TAG, "Error", e);
            return false;
        }

        // Set header
        for (Map.Entry<String, String> headerData: headers.entrySet()) {
            connection.setRequestProperty(headerData.getKey(), headerData.getValue());
        }

        // Set post data
        if (postData != null) {
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);

            try {
                OutputStream out = connection.getOutputStream();
                out.write(postData);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
                connection.disconnect();
                return false;
            }
        }

        // Request
        try {
            InputStream in;

            // Set response code
            responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                in = connection.getErrorStream();
            } else {
                in = connection.getInputStream();
            }

            // Read response
            int readed;
            byte[] temp = new byte[4096];
            ByteArrayOutputStream response = new ByteArrayOutputStream();

            while ((readed = in.read(temp)) != -1) {
                response.write(temp, 0, readed);
            }

            this.response = response.toByteArray();
            response.close();

            // Read headers
            headers.clear();
            for (Map.Entry<String, List<String>> header: connection.getHeaderFields().entrySet()) {
                headers.put(header.getKey(), TextUtils.join(", ", header.getValue()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
            connection.disconnect();
            return false;
        }

        connection.disconnect();
        return true;
    }
}
