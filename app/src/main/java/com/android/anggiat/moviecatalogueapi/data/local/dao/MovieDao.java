package com.android.anggiat.moviecatalogueapi.data.local.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.android.anggiat.moviecatalogueapi.data.local.entity.MovieEntity;

@Dao
public interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMovieFavorite(MovieEntity movieEntity);

    @Query("SELECT * FROM tMovieEntity")
    MovieEntity[] readDataMovie();

    @Delete
    void deleteMovieEntity(MovieEntity movieEntity);

    @Query("SELECT * FROM tMovieEntity WHERE id = :id LIMIT 1")
    MovieEntity selectDetailMovieEntity(int id);


}
