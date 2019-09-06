package com.android.anggiat.moviecatalogueapi.view.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.android.anggiat.moviecatalogueapi.data.models.Movie;
import com.android.anggiat.moviecatalogueapi.data.models.MovieDetails;
import com.android.anggiat.moviecatalogueapi.data.models.MovieGenres;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailMovieActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String EXTRA_MOVIE = "extra_movie";
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
    private ArrayList<MovieGenres> movieGenresList = new ArrayList<>();
    private MovieDetails detailsMovie = new MovieDetails();

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

            if (savedInstanceState != null) {
                detailsMovie = savedInstanceState.getParcelable("details_movie");
                movieGenresList = savedInstanceState.getParcelableArrayList("movie_genre_list");
                if (movieGenresList != null) {
                    textMovieGenre = findViewById(R.id.text_movie_genre);
                    textMovieGenre.setText(movieGenresList.get(0).getName());
                }if ( movieGenresList == null ) {
                    Toast.makeText(this, "movieGenreList Null", Toast.LENGTH_SHORT).show();
                }
            } else {
                moviesData();
            }

            ButterKnife.bind(this);
            Button btnFavorite = findViewById(R.id.btn_favorite);
            btnFavorite.setOnClickListener(this);

            movieDatabase = Room.databaseBuilder(
                    getApplicationContext(), MovieDatabase.class,
                    "dbFavoriteMovie").build();

            textMovieName.setText(detailsMovie.getTitle());
            textMovieRuntime.setText(detailsMovie.getRuntime());
            textMovieRating.setText(detailsMovie.getVoteAverage());
            textMovieLanguage.setText(detailsMovie.getOriginalLanguage());
            textMovieOverview.setText(detailsMovie.getOverview());
            textMovieReleased.setText(detailsMovie.getReleaseDate());
            textMovieRevenue.setText(detailsMovie.getRevenue());

            Glide.with(DetailMovieActivity.this)
                    .load("https://image.tmdb.org/t/p/w500" + detailsMovie.getBackdropPath())
                    .into(imageMovieBackdrop);
            Glide.with(DetailMovieActivity.this)
                    .load("https://image.tmdb.org/t/p/w154" + detailsMovie.getPosterPath())
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
                    Toast.makeText(DetailMovieActivity.this, saveItem, Toast.LENGTH_SHORT).show();
                }
            }.execute();
        }

    private void moviesData() {
        Movie movie = getIntent().getParcelableExtra(EXTRA_MOVIE);
        MovieEntity movieEntity = (MovieEntity)getIntent().getSerializableExtra("detail");

        if (movie != null){
            callMovieMain();
        } else if (movieEntity != null){
            callMovieFavorite();
        } else {
            Toast.makeText(this, "Else call Movie", Toast.LENGTH_SHORT).show();
        }


    }

    private void callMovieMain() {
        Movie movie = getIntent().getParcelableExtra(EXTRA_MOVIE);

        Button btnFavorite = findViewById(R.id.btn_favorite);
        btnFavorite.setOnClickListener(this);
        btnFavorite.setText(R.string.save_data);

        progressDialog = new ProgressDialog(DetailMovieActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Waiting");
        progressDialog.show();

        Log.d(TAG, "Load detailsMovie Movie");
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        final Call<MovieDetails> detailCall = apiService.getDetailMovie(movie.getId(), API_KEY);

        detailCall.enqueue(new Callback<MovieDetails>() {
            @Override
            public void onResponse(@NonNull Call<MovieDetails> call, @NonNull Response<MovieDetails> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    detailsMovie = response.body();
                    movieGenresList = response.body().getMovieGenres();

                    Log.d(TAG, "Get detailsMovie Success :) ");
                    Toast.makeText(DetailMovieActivity.this, detailsMovie.getTitle(), Toast.LENGTH_SHORT).show();

                    textMovieName.setText(detailsMovie.getTitle());
                    textMovieRuntime.setText(detailsMovie.getRuntime());
                    textMovieRating.setText(detailsMovie.getVoteAverage());
                    textMovieLanguage.setText(detailsMovie.getOriginalLanguage());
                    textMovieOverview.setText(detailsMovie.getOverview());
                    textMovieReleased.setText(detailsMovie.getReleaseDate());
                    textMovieRevenue.setText(detailsMovie.getRevenue());
                    textMovieGenre.setText(movieGenresList.get(0).getName());

                    Glide.with(DetailMovieActivity.this)
                            .load("https://image.tmdb.org/t/p/w500" + detailsMovie.getBackdropPath())
                            .into(imageMovieBackdrop);
                    Glide.with(DetailMovieActivity.this)
                            .load("https://image.tmdb.org/t/p/w154" + detailsMovie.getPosterPath())
                            .into(imageMoviePoster);
                } else {
                    Log.d(TAG, "Get detailsMovie into else :) ");
                    Toast.makeText(DetailMovieActivity.this, "Else", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetails> call, Throwable t) {
                Log.d(TAG, "Get detailsMovie Failed :)");
                Toast.makeText(DetailMovieActivity.this, "Something Wrong When Parsing JSON", Toast.LENGTH_SHORT).show();
            }
        });

        ButterKnife.bind(this);
    }

    private void callMovieFavorite() {
        MovieEntity movieEntity = (MovieEntity)getIntent().getSerializableExtra("detail");

        Button btnFavorite = findViewById(R.id.btn_favorite);
        btnFavorite.setOnClickListener(this);
        btnFavorite.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(DetailMovieActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Waiting");
        progressDialog.show();

        Log.d(TAG, "Load detailsMovie Movie");
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        final Call<MovieDetails> detailCall = apiService.getDetailMovie(movieEntity.getId(), API_KEY);

        detailCall.enqueue(new Callback<MovieDetails>() {
            @Override
            public void onResponse(@NonNull Call<MovieDetails> call, @NonNull Response<MovieDetails> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    detailsMovie = response.body();
                    movieGenresList = response.body().getMovieGenres();

                    Log.d(TAG, "Get detailsMovie Success :) ");
                    Toast.makeText(DetailMovieActivity.this, detailsMovie.getTitle(), Toast.LENGTH_SHORT).show();

                    textMovieName.setText(detailsMovie.getTitle());
                    textMovieRuntime.setText(detailsMovie.getRuntime());
                    textMovieRating.setText(detailsMovie.getVoteAverage());
                    textMovieLanguage.setText(detailsMovie.getOriginalLanguage());
                    textMovieOverview.setText(detailsMovie.getOverview());
                    textMovieReleased.setText(detailsMovie.getReleaseDate());
                    textMovieRevenue.setText(detailsMovie.getRevenue());
                    textMovieGenre.setText(movieGenresList.get(0).getName());

                    Glide.with(DetailMovieActivity.this)
                            .load("https://image.tmdb.org/t/p/w500" + detailsMovie.getBackdropPath())
                            .into(imageMovieBackdrop);
                    Glide.with(DetailMovieActivity.this)
                            .load("https://image.tmdb.org/t/p/w154" + detailsMovie.getPosterPath())
                            .into(imageMoviePoster);
                } else {
                    Log.d(TAG, "Get detailsMovie into else :) ");
                    Toast.makeText(DetailMovieActivity.this, "Else", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetails> call, Throwable t) {
                Log.d(TAG, "Get detailsMovie Failed :)");
                Toast.makeText(DetailMovieActivity.this, "Something Wrong When Parsing JSON", Toast.LENGTH_SHORT).show();
            }
        });

        ButterKnife.bind(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("details_movie", detailsMovie);
        outState.putParcelableArrayList("movie_genre_list", movieGenresList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_favorite) {
            MovieEntity movieEntity = new MovieEntity();
            movieEntity.setId(detailsMovie.getId());
            movieEntity.setTitle(detailsMovie.getTitle());
            movieEntity.setReleaseDate(detailsMovie.getReleaseDate());
            movieEntity.setRating(detailsMovie.getVoteAverage());
            movieEntity.setPosterPath(detailsMovie.getPosterPath());
            movieEntity.setCategory("Movie");
            insertDataMovie(movieEntity);
        }
    }
}
