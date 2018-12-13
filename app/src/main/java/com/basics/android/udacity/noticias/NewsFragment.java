package com.basics.android.udacity.noticias;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

// Fragmento que irá exibir a lista de notícias de acordo com a categoria selecionada
public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<News>> {

    // URL para retornar dados de notícias da Guardian API e seus respectivos parâmetros para
    // filtrar as categorias
    private static final String GUARDIAN_REQUEST_URL = "http://content.guardianapis.com/search";
    private String[] mCategoryValues;

    // Elementos da interface
    private int mLoaderId;
    private NewsAdapter mAdapter;
    private TextView mEmptyStateTextView;
    private View mRootView;
    private ListView mNewsListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Classe estática necessária para inicializar um fragmento para uma determinada categoria de
    // notícias e passar o índice dessa categoria via Bundle para ser usado pelo próprio fragmento.
    public static NewsFragment newInstance(int index) {
        NewsFragment f = new NewsFragment();

        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }

    // Retorna o índice da categoria de notícias correspondente à instância do fragmento criada.
    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCategoryValues = getActivity().getResources().getStringArray(R.array.cattegory_values);
        mLoaderId = getShownIndex();

        mRootView = inflater.inflate(R.layout.fragment_news, container, false);

        initializeUIElements();

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Checa o status da conexão internet e inicializa o loader ou exibe um erro.
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().initLoader(mLoaderId, null, this);
        } else {
            View loadingIndicator = mRootView.findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

        return mRootView;

    }

    public void initializeUIElements() {
        mNewsListView = (ListView) mRootView.findViewById(R.id.list);

        mEmptyStateTextView = (TextView) mRootView.findViewById(R.id.empty_view);
        mNewsListView.setEmptyView(mEmptyStateTextView);
        mAdapter = new NewsAdapter(getActivity(), new ArrayList<News>());
        mNewsListView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadNews();
            }
        });

        mNewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                News currentNews = mAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNews.getUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(websiteIntent);
            }
        });
    }

    public void reloadNews() {
        mAdapter.clear();
        mEmptyStateTextView.setVisibility(View.GONE);
        getLoaderManager().restartLoader(mLoaderId, null, this);
    }

    @Override
    public Loader<ArrayList<News>> onCreateLoader(int i, Bundle bundle) {
        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("tag", mCategoryValues[getShownIndex()]);
        uriBuilder.appendQueryParameter("order-by", "newest");
        uriBuilder.appendQueryParameter("api-key", "test");
        uriBuilder.appendQueryParameter("show-tags", "contributor");

        return new NewsLoader(getActivity(), uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<News>> loader, ArrayList<News> newses) {
        View loadingIndicator = mRootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        mEmptyStateTextView.setText(R.string.no_news);
        mAdapter.clear();

        if (newses != null && !newses.isEmpty()) {
            mAdapter.addAll(newses);
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<News>> loader) {
        mAdapter.clear();
    }
}
