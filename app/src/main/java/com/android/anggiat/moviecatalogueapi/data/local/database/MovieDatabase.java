package com.android.anggiat.moviecatalogueapi.data.local.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.android.anggiat.moviecatalogueapi.data.local.dao.MovieDao;
import com.android.anggiat.moviecatalogueapi.data.local.entity.MovieEntity;

@Database(entities = {MovieEntity.class}, version = 1)
public abstract class MovieDatabase extends RoomDatabase {
private static MovieDatabase INSTANCE;

    public abstract MovieDao movieDao();

public static MovieDatabase getMovieDatabase(final Context context) {
    if (INSTANCE == null) {
        synchronized (MovieDatabase.class) {
            INSTANCE = Room.databaseBuilder(context, MovieDatabase.class, "dbFavoriteMovie")
                    .allowMainThreadQueries().build();
        }
    }
    return INSTANCE;
}
}
