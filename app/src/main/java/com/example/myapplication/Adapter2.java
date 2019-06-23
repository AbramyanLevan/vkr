package com.example.myapplication;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.graphics.drawable.Drawable;

import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Adapter2 extends RecyclerView.Adapter<Adapter2.MyViewHolder> {
    private List<String> adNameList = new ArrayList<>();
    private Table.LimitDAO limitDAO;
    private List<Drawable> iconsList = new ArrayList<>();
    private OnNoteListener mOnNoteListener;
    private Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView firstText;
        public TextView limittext;
        public ImageView imageView;
        OnNoteListener onNoteListener;

        public MyViewHolder(View v,OnNoteListener onNoteListener) {
            super(v);
            firstText = v.findViewById(R.id.nameapp);
            limittext = v.findViewById(R.id.hour);
            imageView = v.findViewById(R.id.appicon);
            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);

        }
        @Override
        public void onClick(View v){ onNoteListener.onNoteClick(getAdapterPosition());}



    }


    public Adapter2(List<String> nameList, List<Drawable> icons,OnNoteListener onNoteListener,Context context2) {
        adNameList = nameList;
        context = context2;
        iconsList = icons;
        this.mOnNoteListener = onNoteListener;



    }

    @Override
    public Adapter2.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.app2_item, viewGroup, false);
        MyViewHolder vh = new MyViewHolder(v,mOnNoteListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
        Table.AppDatabase database = Room.databaseBuilder(context, Table.AppDatabase.class, "db-contacts1")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build();
        limitDAO = database.getLimitDAO();

        myViewHolder.firstText.setText(adNameList.get(i));

        myViewHolder.limittext.setText(Table.precall(adNameList.get(i),context));
        myViewHolder.imageView.setImageDrawable(iconsList.get(i));


    }


    @Override
    public int getItemCount() {
        return adNameList.size();
    }
    public interface OnNoteListener{
        void onNoteClick(int position);
    }
}