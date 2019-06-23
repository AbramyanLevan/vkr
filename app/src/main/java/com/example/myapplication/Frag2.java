package com.example.myapplication;


import android.annotation.TargetApi;

import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.arch.persistence.room.Room;
import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class Frag2 extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Adapter2.OnNoteListener{


    private Context context;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout sr;
    private List<String> nameList = new ArrayList<>();
    private List<String> packageList = new ArrayList<>();
    private List<Drawable> iconsList = new ArrayList<>();
    private Table.LimitDAO limitDAO;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    long DayInMillis = 24 * 3600 * 1000;
    String[] args = new String[]{"com.vkontakte.android", "com.instagram.android", "com.facebook.android", "org.telegram.messenger",
            "com.viber.voip", "ru.ok.android", "com.google.android.youtube", "com.whatsapp", "com.snapchat.android", "tv.twitch.android.app"
            , "com.discord", "com.skype.raider", "com.tumblr", "com.twitter.android", "com.pinterest"};

    public Frag2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag2, container, false);

        Table.AppDatabase database = Room.databaseBuilder(getActivity(), Table.AppDatabase.class, "db-contacts1")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build();
        limitDAO = database.getLimitDAO();
        sr = (SwipeRefreshLayout) view.findViewById(R.id.sr2);
        sr.setOnRefreshListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler2);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        showUsage();


        adapter = new Adapter2(nameList, iconsList, this,getContext());
        recyclerView.setAdapter(adapter);


        return view;

    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showUsage() {

        UsageStatsManager usageStatsManager;
        usageStatsManager = (UsageStatsManager) getActivity().getSystemService(context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        long startMillis = calendar.getTimeInMillis() - DayInMillis;
        long endMillis = calendar.getTimeInMillis();

        Map<String, UsageStats> queryUsageStats = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis);
        List<String> list = Arrays.asList(args);
        List<String> appsToShow = new ArrayList<>(list);
        queryUsageStats.forEach((String key, UsageStats usage) ->
        {
            if (appsToShow.contains(key)) {
                nameList.add(label(usage.getPackageName()));
                packageList.add(key);
                try {
                    Drawable icon = getActivity().getPackageManager().getApplicationIcon(usage.getPackageName());
                    iconsList.add(icon);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    public void onRefresh() {
        sr.setRefreshing(true);
        new AsyncTask() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            protected Object doInBackground(Object[] objects) {
                nameList.clear();
                ;
                showUsage();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                getActivity().runOnUiThread(() -> sr.setRefreshing(false));

                adapter.notifyDataSetChanged();

            }
        }.execute();


    }


    public String label(String pkg) {
        final String packageName = pkg;
        PackageManager packageManager = getActivity().getApplicationContext().getPackageManager();
        String appName = null;
        try {
            appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onNoteClick(int position) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View mView = getLayoutInflater().inflate(R.layout.dialog, null);
        TimePicker timePicker = (TimePicker) mView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        Table cur = limitDAO.getCurrent(nameList.get(position));
        if (cur != null) {
            long d = cur.getAppLimit() / 1000;
            timePicker.setHour((int)(d / 3600));
            timePicker.setMinute((int)((d - (d / 3600) * 3600)) / 60);
        }
        Button enter = (Button) mView.findViewById(R.id.entBut);
        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                limitDAO.getAll().forEach(
                        table -> {
                            Log.e("msg", table.toString());
                        }
                );

                long hinmil = timePicker.getHour();
                long minmil = timePicker.getMinute();



                Table cur = limitDAO.getCurrent(nameList.get(position));
                if (cur != null) {
                    limitDAO.delete(cur);
                }
                Table table = new Table();
                table.setAppName(nameList.get(position));
                table.setAppPackage(packageList.get(position));
                table.setAppLimit((hinmil * 3600 + minmil * 60) * 1000);
                limitDAO.insert(table);

                dialog.dismiss();

                Intent broadcastIntent = new Intent("com.example.action.CAT");
                broadcastIntent.setClass(getActivity(), SensorRestartBroadcastReceiver.class);
                getActivity().sendBroadcast(broadcastIntent);

            }
        });

        dialog.show();


    }


}
