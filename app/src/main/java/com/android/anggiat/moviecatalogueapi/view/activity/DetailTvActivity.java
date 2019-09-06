package com.android.anggiat.moviecatalogueapi.view.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.anggiat.moviecatalogueapi.BuildConfig;
import com.android.anggiat.moviecatalogueapi.R;
import com.android.anggiat.moviecatalogueapi.data.api.ApiClient;
import com.android.anggiat.moviecatalogueapi.data.api.ApiService;
import com.android.anggiat.moviecatalogueapi.data.local.database.MovieDatabase;
import com.android.anggiat.moviecatalogueapi.data.local.entity.MovieEntity;
import com.android.anggiat.moviecatalogueapi.data.models.TvShow;
import com.android.anggiat.moviecatalogueapi.data.models.TvShowDetails;
import com.android.anggiat.moviecatalogueapi.data.models.TvShowGenres;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailTvActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String EXTRA_TV = "extra_tv";
    private MovieDatabase movieDatabase;

    @BindView(R.id.image_movie_poster)
    ImageView imageMoviePoster;
    @BindView(R.id.text_movie_name)
    TextView textMovieName;
    @BindView(R.id.text_movie_genre)
    TextView textMovieGenre;
    @BindView(R.id.text_movie_runtime)
    TextView textMovieRuntime;
    @BindView(R.id.text_movie_rating)
    TextView textMovieRating;
    @BindView(R.id.image_movie_backdrop)
    ImageView imageMovieBackdrop;
    @BindView(R.id.text_movie_language)
    TextView textMovieLanguage;
    @BindView(R.id.text_movie_overview)
    TextView textMovieOverview;
    @BindView(R.id.text_movie_released)
    TextView textMovieReleased;
    @BindView(R.id.text_movie_revenue)
    TextView textMovieRevenue;

    private final String TAG = DetailMovieActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private static final String API_KEY = BuildConfig.API_KEY;
    private ArrayList<TvShowGenres> tvGenresList = new ArrayList<>();
    private TvShowDetails detailsTv = new TvShowDetails();

    private void setActionBarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String title;
        title = "";
        setActionBarTitle(title);

        if (savedInstanceState != null){
            detailsTv = savedInstanceState.getParcelable("details_tv");
            tvGenresList = savedInstanceState.getParcelableArrayList("tv_genres_list");
            if (tvGenresList != null) {
                textMovieGenre = findViewById(R.id.text_movie_genre);
                textMovieGenre.setText(tvGenresList.get(0).getName());
            }if ( tvGenresList == null ) {
                Toast.makeText(this, "tvGenreList Null", Toast.LENGTH_SHORT).show();
            }
        } else {
            tvShowData();
        }

        ButterKnife.bind(this);

        movieDatabase = Room.databaseBuilder(
                getApplicationContext(), MovieDatabase.class,
                "dbFavoriteMovie").build();

        textMovieName.setText(detailsTv.getName());
        String getNumberEpisodeTv = String.valueOf(detailsTv.getNumberOfEpisodes());
        textMovieRuntime.setText(getNumberEpisodeTv);//number episodes
        String getVoteAverageTv = String.valueOf(detailsTv.getVoteAverage());
        textMovieRating.setText(getVoteAverageTv);
        textMovieLanguage.setText(detailsTv.getOriginalLanguage());
        textMovieOverview.setText(detailsTv.getOverview());
        textMovieReleased.setText(detailsTv.getFirstAirDate());
        String getPopularity = String.valueOf(detailsTv.getPopularity());
        textMovieRevenue.setText(getPopularity);//popularity

        Glide.with(DetailTvActivity.this)
                .load("https://image.tmdb.org/t/p/w500" + detailsTv.getBackdropPath())
                .into(imageMovieBackdrop);
        Glide.with(DetailTvActivity.this)
                .load("https://image.tmdb.org/t/p/w154" + detailsTv.getPosterPath())
                .into(imageMoviePoster);
    }

    @SuppressLint("StaticFieldLeak")
    private void insertDataMovie(final MovieEntity movieEntity) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                return movieDatabase.movieDao().insertMovieFavorite(movieEntity);
            }

            @Override
            protected void onPostExecute(Long aLong) {
                String saveItem = movieEntity.getTitle()+"\n"+getString(R.string.success_to_save);
                Toast.makeText(DetailTvActivity.this, saveItem, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void tvShowData() {
        TvShow tvShow = getIntent().getParcelableExtra(EXTRA_TV);
        MovieEntity movieEntity = (MovieEntity)getIntent().getSerializableExtra("detail");

        if (tvShow != null){
            callMovieMain();
        } else if (movieEntity != null){
            callMovieFavorite();
        } else {
            Toast.makeText(this, "Else call Movie", Toast.LENGTH_SHORT).show();
        }

    }

    private void callMovieFavorite() {
        final MovieEntity movieEntity = (MovieEntity)getIntent().getSerializableExtra("detail");

        Button btnFavorite = findViewById(R.id.btn_favorite);
        btnFavorite.setOnClickListener(this);
        btnFavorite.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(DetailTvActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Waiting");
        progressDialog.show();

        Log.d(TAG, "Load detailsMovie Tv Show");
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        final Call<TvShowDetails> detailTvCall = apiService.getDetailTv(movieEntity.getId(), API_KEY);

        detailTvCall.enqueue(new Callback<TvShowDetails>() {
            @Override
            public void onResponse(Call<TvShowDetails> call, Response<TvShowDetails> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    detailsTv = response.body();
                    tvGenresList = response.body().getGenres();

                    Log.d(TAG, "Get detailsMovie Success :) ");
                    Toast.makeText(DetailTvActivity.this, detailsTv.getName(), Toast.LENGTH_SHORT).show();

                    textMovieName.setText(detailsTv.getName());
                    String getNumberEpisodeTv = String.valueOf(detailsTv.getNumberOfEpisodes());
                    textMovieRuntime.setText(getNumberEpisodeTv);//number episodes
                    String getVoteAverageTv = String.valueOf(detailsTv.getVoteAverage());
                    textMovieRating.setText(getVoteAverageTv);
                    textMovieLanguage.setText(detailsTv.getOriginalLanguage());
                    textMovieOverview.setText(detailsTv.getOverview());
                    textMovieReleased.setText(detailsTv.getFirstAirDate());
                    String getPopularity = String.valueOf(detailsTv.getPopularity());
                    textMovieRevenue.setText(getPopularity);//popularity
                    textMovieGenre.setText(tvGenresList.get(0).getName());

                    Glide.with(DetailTvActivity.this)
                            .load("https://image.tmdb.org/t/p/w500" + detailsTv.getBackdropPath())
                            .into(imageMovieBackdrop);
                    Glide.with(DetailTvActivity.this)
                            .load("https://image.tmdb.org/t/p/w154" + detailsTv.getPosterPath())
                            .into(imageMoviePoster);
                } else {
                    Log.d(TAG, "Get detailsMovie into else :) ");
                    Toast.makeText(DetailTvActivity.this, "Else", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TvShowDetails> call, Throwable t) {
                Log.d(TAG, "Get detailsMovie Failed :)");
                Toast.makeText(DetailTvActivity.this, "Something Wrong When Parsing JSON", Toast.LENGTH_SHORT).show();
            }
        });
        ButterKnife.bind(this);

    }

    private void callMovieMain() {
        TvShow tvShow = getIntent().getParcelableExtra(EXTRA_TV);

        Button btnFavorite = findViewById(R.id.btn_favorite);
        btnFavorite.setOnClickListener(this);
        btnFavorite.setText(R.string.save_data);

        progressDialog = new ProgressDialog(DetailTvActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Waiting");
        progressDialog.show();

        Log.d(TAG, "Load detailsMovie Tv Show");
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        final Call<TvShowDetails> detailTvCall = apiService.getDetailTv(tvShow.getId(), API_KEY);

        detailTvCall.enqueue(new Callback<TvShowDetails>() {
            @Override
            public void onResponse(Call<TvShowDetails> call, Response<TvShowDetails> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    detailsTv = response.body();
                    tvGenresList = response.body().getGenres();

                    Log.d(TAG, "Get detailsMovie Success :) ");
                    Toast.makeText(DetailTvActivity.this, detailsTv.getName(), Toast.LENGTH_SHORT).show();

                    textMovieName.setText(detailsTv.getName());
                    String getNumberEpisodeTv = String.valueOf(detailsTv.getNumberOfEpisodes());
                    textMovieRuntime.setText(getNumberEpisodeTv);//number episodes
                    String getVoteAverageTv = String.valueOf(detailsTv.getVoteAverage());
                    textMovieRating.setText(getVoteAverageTv);
                    textMovieLanguage.setText(detailsTv.getOriginalLanguage());
                    textMovieOverview.setText(detailsTv.getOverview());
                    textMovieReleased.setText(detailsTv.getFirstAirDate());
                    String getPopularity = String.valueOf(detailsTv.getPopularity());
                    textMovieRevenue.setText(getPopularity);//popularity
                    textMovieGenre.setText(tvGenresList.get(0).getName());

                    Glide.with(DetailTvActivity.this)
                            .load("https://image.tmdb.org/t/p/w500" + detailsTv.getBackdropPath())
                            .into(imageMovieBackdrop);
                    Glide.with(DetailTvActivity.this)
                            .load("https://image.tmdb.org/t/p/w154" + detailsTv.getPosterPath())
                            .into(imageMoviePoster);
                } else {
                    Log.d(TAG, "Get detailsMovie into else :) ");
                    Toast.makeText(DetailTvActivity.this, "Else", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TvShowDetails> call, Throwable t) {
                Log.d(TAG, "Get detailsMovie Failed :)");
                Toast.makeText(DetailTvActivity.this, "Something Wrong When Parsing JSON", Toast.LENGTH_SHORT).show();
            }
        });
        ButterKnife.bind(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("details_tv", detailsTv);
        outState.putParcelableArrayList("tv_genres_list", tvGenresList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_favorite){
            MovieEntity movieEntity = new MovieEntity();
            movieEntity.setId(detailsTv.getId());
            movieEntity.setTitle(detailsTv.getName());
            movieEntity.setReleaseDate(detailsTv.getFirstAirDate());
            movieEntity.setRating(String.valueOf(detailsTv.getVoteAverage()));
            movieEntity.setPosterPath(detailsTv.getPosterPath());
            movieEntity.setCategory("Tv Series");
            insertDataMovie(movieEntity);
        }
    }
}

