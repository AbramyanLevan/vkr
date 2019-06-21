package com.example.myapplication;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.Update;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;


@Entity(tableName = "Limits")
public  class Table {
    @Override
    public String toString() {
        return String.format("{id: %d, appName: %s, appLimit: %d}", id, appName, appLimit);
    }

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @ColumnInfo(name = "appName")
    private String appName;


    @ColumnInfo(name = "appPackage")
    private String appPackage;

    @ColumnInfo(name = "appLimit")
    private long appLimit;

    public String getAppName(){
        return appName;
    }
    public void  setAppName(String appName){
        this.appName = appName;
    }

    public long getAppLimit(){
        return appLimit;
    }
    public void  setAppLimit(long appLimit){
        this.appLimit = appLimit;
    }
    @NonNull
    public int getId(){
        return id;
    }
    public void  setId(@NonNull int id){
        this.id = id;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    @Dao
    public interface LimitDAO {
        @Insert
        public void insert(Table... limits);

        @Update
        public void update(Table... limit);

        @Delete
        public void delete(Table table);

        @Query("SELECT * FROM Limits")
          List<Table> getAll();

        @Query("SELECT * FROM Limits WHERE appName LIKE :name")
        Table  getCurrent(String name);

    }
    @Database(entities = {Table.class}, version = 1)
    public abstract static class AppDatabase extends RoomDatabase {

        private static AppDatabase INSTANCE;

        public abstract LimitDAO getLimitDAO();

        static final Migration MIGRATION_1_2 = new Migration(1, 2) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
// Since we didn't alter the table, there's nothing else to do here.
            }
        };

    }



}