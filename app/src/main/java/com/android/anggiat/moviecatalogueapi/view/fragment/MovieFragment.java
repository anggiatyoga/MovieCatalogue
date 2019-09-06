package com.android.anggiat.moviecatalogueapi.view.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.anggiat.moviecatalogueapi.BuildConfig;
import com.android.anggiat.moviecatalogueapi.R;
import com.android.anggiat.moviecatalogueapi.view.activity.FavoriteListActivity;
import com.android.anggiat.moviecatalogueapi.view.activity.MainActivity;
import com.android.anggiat.moviecatalogueapi.view.adapter.MovieAdapter;
import com.android.anggiat.moviecatalogueapi.data.api.ApiClient;
import com.android.anggiat.moviecatalogueapi.data.api.ApiService;
import com.android.anggiat.moviecatalogueapi.data.models.Movie;
import com.android.anggiat.moviecatalogueapi.data.models.MovieResult;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieFragment extends Fragment {

    private final String TAG = MovieFragment.class.getSimpleName();
    private MovieAdapter movieAdapter;

    @BindView(R.id.rv_movie)
    RecyclerView rvMovie;
    @BindView(R.id.progress_load_photo)
    ProgressBar progressBar;

    private Unbinder unbinder;
    private static final String API_KEY = BuildConfig.API_KEY;
    private ArrayList<Movie> movies;

    public MovieFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);

        if (savedInstanceState != null) {
            movies = savedInstanceState.getParcelableArrayList("movies");
        } else {
            progressBar = view.findViewById(R.id.progress_load_photo);
            getData("");
        }

//        getActivity().setTitle(getString(R.string.title_movie));
        unbinder = ButterKnife.bind(this, view);

        MovieAdapter movieAdapter = new MovieAdapter(getContext(), movies);
        rvMovie.setAdapter(movieAdapter);
        rvMovie.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvMovie.setItemAnimator(new DefaultItemAnimator());
        rvMovie.setNestedScrollingEnabled(true);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_main, menu);
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = new SearchView(getActivity());
        searchView.setQueryHint("Cari Film ... ");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2){
                    getData(query);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                getData(newText);
                return false;
            }
        });
        searchItem.setActionView(searchView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_settings:
                Intent mIntentSetting = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivity(mIntentSetting);
                break;
            case R.id.action_favorite_movie:
                Intent mIntentFavorite = new Intent(getContext(), FavoriteListActivity.class);
                startActivity(mIntentFavorite);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getData(final String query) {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<MovieResult> movieResultCall;
        if (query.length() > 0) {
            Toast.makeText(getContext(), "get Query "+query, Toast.LENGTH_SHORT).show();
            movieResultCall = apiService.getSearchMovie(query, API_KEY);
        } else {
            Toast.makeText(getContext(), "else", Toast.LENGTH_SHORT).show();
            movieResultCall = apiService.getMovieList(API_KEY);
        }

        movieResultCall.enqueue(new Callback<MovieResult>() {
            @Override
            public void onResponse(@NonNull Call<MovieResult> call, @NonNull Response<MovieResult> response) {
                if (response.body() != null) {
                    movies = response.body().getResults();
                    Log.d(TAG, "Get data Success :) ");
                    MovieAdapter movieAdapter = new MovieAdapter(getContext(), movies);
                    rvMovie.setAdapter(movieAdapter);
                } else {
                    Log.d(TAG, "Get data Failed :)");
                    Toast.makeText(getContext(), "Something Wrong When Parsing JSON", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<MovieResult> call, @NonNull Throwable t) {
                Log.d(TAG, "Failure to get connection");
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("movies", movies);
    }
}
