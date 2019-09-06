package com.android.anggiat.moviecatalogueapi.view.activity;

import android.arch.persistence.room.Room;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.anggiat.moviecatalogueapi.R;
import com.android.anggiat.moviecatalogueapi.data.local.database.MovieDatabase;
import com.android.anggiat.moviecatalogueapi.data.local.entity.MovieEntity;
import com.android.anggiat.moviecatalogueapi.view.adapter.FavoriteMovieAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class FavoriteListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private MovieDatabase movieDatabase;
    private ArrayList<MovieEntity> movieEntityList;

    private void setActionBarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        String title = getString(R.string.favorite_list);
        setActionBarTitle(title);

        movieEntityList = new ArrayList<>();

        movieDatabase = Room.databaseBuilder(getApplicationContext(),MovieDatabase.class,"dbFavoriteMovie")
                .allowMainThreadQueries().build();

        movieEntityList.addAll(Arrays.asList(movieDatabase.movieDao().readDataMovie()));

        recyclerView = findViewById(R.id.rv_favorite_movie);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FavoriteMovieAdapter(movieEntityList, FavoriteListActivity.this);
        recyclerView.setAdapter(adapter);
    }
}
