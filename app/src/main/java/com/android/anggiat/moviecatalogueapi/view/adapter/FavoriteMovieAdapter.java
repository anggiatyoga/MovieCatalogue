package com.android.anggiat.moviecatalogueapi.view.adapter;

import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.anggiat.moviecatalogueapi.R;
import com.android.anggiat.moviecatalogueapi.data.local.database.MovieDatabase;
import com.android.anggiat.moviecatalogueapi.data.local.entity.MovieEntity;
import com.android.anggiat.moviecatalogueapi.view.activity.DetailMovieActivity;
import com.android.anggiat.moviecatalogueapi.view.activity.DetailTvActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoriteMovieAdapter extends RecyclerView.Adapter<FavoriteMovieAdapter.ViewHolder>{
    private ArrayList<MovieEntity> movieEntities;
    private MovieDatabase movieDatabase;
    private Context context;

    public FavoriteMovieAdapter(ArrayList<MovieEntity> movieEntities, Context context) {
        this.movieEntities = movieEntities;
        this.context = context;
        movieDatabase = Room.databaseBuilder(
                context.getApplicationContext(),
                MovieDatabase.class,"dbFavoriteMovie"
        ).allowMainThreadQueries().build();
    }

    @NonNull
    @Override
    public FavoriteMovieAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_favorite_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteMovieAdapter.ViewHolder holder, final int position) {
        final String getName = movieEntities.get(position).getTitle();
        final String getRelease = movieEntities.get(position).getReleaseDate();
        final String getRating = movieEntities.get(position).getRating();
        final String getPoster = movieEntities.get(position).getPosterPath();
        final String getCategory = movieEntities.get(position).getCategory();

        holder.tvName.setText(getName);
        holder.tvReleaseDate.setText(getRelease);
        holder.tvRating.setText(getRating);
        if (getCategory.equals("Movie")){
            holder.tvCategory.setText(R.string.title_movie);
        } else {
            holder.tvCategory.setText(R.string.title_tv_series);
        }

        Glide.with(context)
                .load("https://image.tmdb.org/t/p/w154"+getPoster)
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getMovieIntent = movieEntities.get(position).getCategory();

                MovieEntity movieEntity = movieDatabase.movieDao()
                        .selectDetailMovieEntity(movieEntities.get(position).getId());
                if (getMovieIntent.equals("Movie")){
                    context.startActivity(new Intent(context, DetailMovieActivity.class).putExtra("detail", movieEntity));
                } else if (getMovieIntent.equals("Tv Series")) {
                    context.startActivity(new Intent(context, DetailTvActivity.class).putExtra("detail", movieEntity));
                }

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CharSequence[] deleteItem = {context.getString(R.string.delete)};
                AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext())
                        .setTitle(R.string.delete_from_favorite)
                        .setItems(deleteItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDataMovie(position);
                            }
                        });
                dialog.create();
                dialog.show();
                return true;
            }
        });

    }

    private void deleteDataMovie(int position) {
        movieDatabase.movieDao().deleteMovieEntity(movieEntities.get(position));
        movieEntities.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, movieEntities.size());
    }

    @Override
    public int getItemCount() {
        return movieEntities == null ? 0 : movieEntities.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_item_poster)
        ImageView ivPoster;
        @BindView(R.id.text_item_name)
        TextView tvName;
        @BindView(R.id.text_item_genre1)
        TextView tvReleaseDate;
        @BindView(R.id.text_item_rating)
        TextView tvRating;
        @BindView(R.id.text_movie_category)
        TextView tvCategory;

        ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
