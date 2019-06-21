package com.example.myapplication;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SensorRestartBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = SensorRestartBroadcastReceiver.class.getSimpleName();
    public SensorRestartBroadcastReceiver() { }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Service Stops, let's restart again.");
        context.stopService(new Intent(context, Watcher.class));
        Intent i = new Intent(context, Watcher.class);
        Table.AppDatabase database = Room.databaseBuilder(context, Table.AppDatabase.class, "db-contacts1")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build();
        Table.LimitDAO limitDAO = database.getLimitDAO();
        List<Table> tableList = new ArrayList<>();
        tableList.addAll(limitDAO.getAll());
        Gson gson = new Gson();
        String json = gson.toJson(tableList);
        i.putExtra("inf", json);
        ContextCompat.startForegroundService(context, i);
    }
}