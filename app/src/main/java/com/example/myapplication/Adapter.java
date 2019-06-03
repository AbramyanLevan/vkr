package com.example.myapplication;

import android.graphics.drawable.Drawable;

import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    private List<String> adNameList = new ArrayList<>();
    private List<String> adTimeList = new ArrayList<>();
    private List<Drawable> iconsList = new ArrayList<>();

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView firstText;
        public TextView secondText;
        public ImageView imageView;

        public MyViewHolder(View v) {
            super(v);
            firstText = v.findViewById(R.id.card_text);
            secondText = v.findViewById(R.id.card_text2);
            imageView = v.findViewById(R.id.imageView);
        }

    }


    public Adapter(List<String> nameList, List<String> timeList, List<Drawable> icons) {
        adNameList = nameList;
        adTimeList = timeList;
        iconsList = icons;

    }

    @Override
    public Adapter.MyViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.app_item, viewGroup, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {

        myViewHolder.firstText.setText(adNameList.get(i));

        myViewHolder.secondText.setText(adTimeList.get(i));
        myViewHolder.imageView.setImageDrawable(iconsList.get(i));

    }

    @Override
    public int getItemCount() {
        return adNameList.size();
    }
}