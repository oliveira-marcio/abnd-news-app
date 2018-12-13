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

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {

    // Adaptador para preencher a lista de notícias com os títulos e as datas de publicação
    public NewsAdapter(Context context, List<News> newses) {
        super(context, 0, newses);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_list_item, parent, false);
        }

        News currentNews = getItem(position);

        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title_text_view);
        titleTextView.setText(currentNews.getTitle());

        TextView authorTextView = (TextView) listItemView.findViewById(R.id.author_text_view);
        String authors = currentNews.getAuthors();
        if (TextUtils.isEmpty(authors)) {
            authorTextView.setVisibility(View.GONE);
        } else {
            authorTextView.setText(authors);
            authorTextView.setVisibility(View.VISIBLE);
        }

        TextView dateTextView = (TextView) listItemView.findViewById(R.id.date_text_view);
        dateTextView.setText(formatDate(currentNews.getDate()));

        return listItemView;
    }

    /**
     * Formata a data recebida do JSON (Ex: "2016-09-28T11:20:00Z") para um formato mais amigável.
     * Ex: "28 set, 2016 11:20 AM". Caso haja algum problema na formatação, retorna a data sem
     * formatação.
     */
    private String formatDate(String dateString) {
        SimpleDateFormat firstFormatter, secondFormatter;
        firstFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            Date oldDate = firstFormatter.parse(dateString.replaceFirst(":(?=[0-9]{2}$)", ""));
            secondFormatter = new SimpleDateFormat("dd LLL, yyyy h:mm a");
            return secondFormatter.format(oldDate);
        } catch (java.text.ParseException e) {
            return dateString;
        }
    }
}
