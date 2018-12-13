/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.basics.android.udacity.noticias;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

// Classe utilitária com métodos para requistar e receber notícias da Guardian API.
public final class QueryUtils {

    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Construtor privado vazio necessário para que ninguém consiga criar objetos desta classe, visto
     * que ela existe apenas para disponibilizar métodos e variáveis estáticas.
     */
    private QueryUtils() {
    }

    public static ArrayList<News> fetchNewsData(String requestUrl) {
        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        ArrayList<News> newses = extractFeatureFromJson(jsonResponse);
        return newses;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static ArrayList<News> extractFeatureFromJson(String searchResultJSON) {
        if (TextUtils.isEmpty(searchResultJSON)) {
            return null;
        }

        ArrayList<News> newses = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(searchResultJSON);

            // Extrai do JSON um array de notícias e, para cada item do array, extrai as
            // propriedades necessárias.
            JSONArray newsArray = baseJsonResponse.getJSONObject("response").getJSONArray("results");
            for (int i = 0; i < newsArray.length(); i++) {
                JSONObject currentNews = newsArray.getJSONObject(i);

                String title = currentNews.getString("webTitle");
                String date = currentNews.getString("webPublicationDate");
                String url = currentNews.getString("webUrl");

                // Busca todos os autores separados por virgula, se houverem
                String authors = "";
                final String SEPARATOR = ", ";
                if(currentNews.has("tags")) {
                    JSONArray tagsArray = currentNews.getJSONArray("tags");
                    for(int j = 0; j < tagsArray.length(); j++){
                        JSONObject currentTag = tagsArray.getJSONObject(j);
                        if(currentTag.has("webTitle")){
                            authors += currentTag.getString("webTitle") + SEPARATOR;
                        }
                    }
                }

                // elimina a virgula final da string de autores
                if(!TextUtils.isEmpty(authors)){
                    authors = authors.substring(0, authors.length() - SEPARATOR.length());
                }

                News news = new News(title, authors, date, url);
                newses.add(news);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }

        return newses;
    }
}
