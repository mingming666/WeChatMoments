package com.ming.android.wechatmoments;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ming.android.wechatmoments.been.Item;
import com.ming.android.wechatmoments.been.TeetItem;
import com.ming.android.wechatmoments.been.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MYNOTEBOOK on 2017/12/29.
 */

public class DataFetch {
    private static final String TAG = "DataFetch";
    private static final String FETCH_USERINFO_PATH = "user/jsmith";
    private static final String SEARCH_TEETS_PATH = "user/jsmith/tweets";
    private static final Uri HOST = Uri.parse("http://thoughtworks-ios.herokuapp.com/");


    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ":with" + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public List<Item> downloadTeetItems(String url) {
        List<Item> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            parserTeetItem(items, jsonString);
            Log.i(TAG, "downloadTeetItems: " + jsonString);
        } catch (IOException e) {
            Log.e(TAG, "downloadTeetItems: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "downloadTeetItems: ", e);

        }
        return items;
    }

    /**
     * 解析teets数据
     *
     * @param items
     * @param jsonString
     * @throws JSONException
     */
    private void parserTeetItem(List<Item> items, String jsonString) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject teetjsonObject = jsonArray.getJSONObject(i);
            TeetItem teetItem = new Gson().fromJson(teetjsonObject.toString(),
                    new TypeToken<TeetItem>() {
                    }.getType());
            if (TextUtils.isEmpty(teetItem.getContent()) && teetItem.getImages() == null) {
                continue;
            }
            items.add(teetItem);
        }
    }

    public UserInfo downloadUserInfoItems(String url) {
        UserInfo userInfo = null;
        try {
            String jsonString = getUrlString(url);
            userInfo = new Gson().fromJson(jsonString, UserInfo.class);
        } catch (IOException e) {
            Log.e(TAG, "downloadItems: " + e.getMessage());
        }
        return userInfo;
    }


    public UserInfo fetchUserInfo() {
        String url = buildUrl(FETCH_USERINFO_PATH);
        return downloadUserInfoItems(url);
    }

    public List<Item> fetchTeets() {
        String url = buildUrl(SEARCH_TEETS_PATH);
        return downloadTeetItems(url);
    }

    private String buildUrl(String path) {
        Uri.Builder uriBuilder = HOST.buildUpon()
                .appendEncodedPath(path);
        return uriBuilder.build().toString();
    }

}
