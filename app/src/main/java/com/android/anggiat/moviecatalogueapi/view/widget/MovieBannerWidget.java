package com.android.anggiat.moviecatalogueapi.view.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.anggiat.moviecatalogueapi.R;

import java.util.Objects;

/**
 * Implementation of App Widget functionality.
 */
public class MovieBannerWidget extends AppWidgetProvider {

    private static final String TOAST_ACTION = "com.android.anggiat.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.android.anggiat.EXTRA_ITEM";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = new Intent(context, StackWidgetService.class);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.movie_banner_widget);

        views.setRemoteAdapter(R.id.stack_view, intent);

        Intent toastIntent = new Intent(context, MovieBannerWidget.class);

//        views.setEmptyView(R.id.stack_view, R.id.empty_view);

        toastIntent.setAction(MovieBannerWidget.TOAST_ACTION);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {


        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

//    @Override
//    public void onReceive(Context context, Intent intent) {
//        super.onReceive(context, intent);
//        if (intent.getAction() != null) {
//            if (intent.getAction().equals(TOAST_ACTION)) {
//                int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
//                Toast.makeText(context, "Touch view" + viewIndex, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager.getInstance(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Objects.requireNonNull(intent.getAction()).equals(TOAST_ACTION)){
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
                int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
                Toast.makeText(context, "Touched View "+ viewIndex, Toast.LENGTH_SHORT).show();
            }
        }
        super.onReceive(context, intent);
    }
}

