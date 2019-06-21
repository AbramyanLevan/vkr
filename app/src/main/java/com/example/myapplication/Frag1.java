package com.example.myapplication;


import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;


/**
 * A simple {@link Fragment} subclass.
 */
public class Frag1 extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1;
    private Context context;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout sr;
    private List<String> nameList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<Drawable> iconsList = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    long DayInMillis = 24 * 3600 * 1000;

    public Frag1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag1, container, false);


        sr = (SwipeRefreshLayout) view.findViewById(R.id.sr);
        sr.setOnRefreshListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        Context context = getContext();
        AppOpsManager appOps = (AppOpsManager)
                context.getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
        } else {
            Toast toast = Toast.makeText(context.getApplicationContext(),
                    "У приложения уже есть разрешение", Toast.LENGTH_SHORT);
            toast.show();
            showUsage();
        }

        adapter = new Adapter(nameList, timeList, iconsList);
        recyclerView.setAdapter(adapter);
        return view;

    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showUsage() {

        UsageStatsManager usageStatsManager;
        usageStatsManager = (UsageStatsManager) getActivity().getSystemService(context.USAGE_STATS_SERVICE);

        ZoneId z = ZoneId.of("Europe/Moscow");
        ZonedDateTime zdt = ZonedDateTime.now(z);
        LocalDate today = zdt.toLocalDate();
        ZonedDateTime zdtTodayStart = today.atStartOfDay(z);
        ZonedDateTime zdtTodayEnd = today.atStartOfDay(z).plusDays(1);
        long startMillis = zdtTodayStart.toEpochSecond() * 1000;
        long endMillis = zdtTodayEnd.toEpochSecond() * 1000;

        Map<String, UsageStats> queryUsageStats = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis);


        String[] args = new String[]{"com.vkontakte.android", "com.instagram.android", "com.facebook.android", "org.telegram.messenger",
                "com.viber.voip", "ru.ok.android", "com.google.android.youtube", "com.whatsapp", "com.snapchat.android", "tv.twitch.android.app"
                , "com.discord", "com.skype.raider", "com.tumblr", "com.twitter.android", "com.pinterest"};
        List<String> list = Arrays.asList(args);
        List<String> appsToShow = new ArrayList<>(list);


        queryUsageStats.forEach((String key, UsageStats usage) ->
        {
            if (appsToShow.contains(key)) {
                nameList.add(label(usage.getPackageName()));
                timeList.add(formater(usage.getTotalTimeInForeground()));
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
                timeList.clear();

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

    public String formater(long millis) {
        return String.format("%01dч %02dмин",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
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

}