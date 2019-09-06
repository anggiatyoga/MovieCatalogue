package com.android.anggiat.moviecatalogueapi.view.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.anggiat.moviecatalogueapi.R;
import com.android.anggiat.moviecatalogueapi.data.models.Movie;
import com.bumptech.glide.Glide;

import java.util.concurrent.ExecutionException;

import static android.provider.VoicemailContract.Status.CONTENT_URI;
import static com.android.anggiat.moviecatalogueapi.view.widget.MovieBannerWidget.EXTRA_ITEM;

public class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    private Movie movies;
    private Context mContext;
    private Cursor cursor;
    int appWidgetId;

    public StackRemoteViewsFactory(Context mContext, Intent intent) {
        this.mContext = mContext;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
//        cursor = mContext.getContentResolver().query(CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onDataSetChanged() {
        final long token = Binder.clearCallingIdentity();
        cursor = mContext.getContentResolver().query(CONTENT_URI, null, null, null, null);
        Binder.restoreCallingIdentity(token);
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        if (cursor.moveToPosition(position)) {
            movies = new Movie();
            Bitmap bitmap;
            try {
                bitmap = Glide.with(mContext)
                        .asBitmap()
                        .load("https://image.tmdb.org/t/p/w154" + movies.getPosterPath())
                        .into(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL, com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                        .get();
                remoteViews.setImageViewBitmap(R.id.image_widget, bitmap);
                remoteViews.setTextViewText(R.id.text_movie_title_widget, movies.getTitle());
            } catch (InterruptedException | ExecutionException e) {
                Log.d("Failed to load Widget", "Error");
            }
        }
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_ITEM, position);
        Intent fillIntetnt = new Intent();
        fillIntetnt.putExtras(extras);

        remoteViews.setOnClickFillInIntent(R.id.image_widget, fillIntetnt);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
