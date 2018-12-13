package com.basics.android.udacity.noticias;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class CategoryAdapter extends FragmentPagerAdapter {

    // Array com os títulos de todas as abas de notícias
    private String[] mCategoryLabels;

    public CategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mCategoryLabels = context.getResources().getStringArray(R.array.cattegory_labels);
    }

   // Retorna uma instância do fragmento para exibir notícias de uma determinada categoria
    @Override
    public Fragment getItem(int position) {
        return NewsFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return mCategoryLabels.length;
    }

    // Retorna o nome da aba de categorias para o fragmento em exibição
    @Override
    public CharSequence getPageTitle(int position) {
        return mCategoryLabels[position];
    }
}
