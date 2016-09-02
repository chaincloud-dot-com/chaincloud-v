package com.chaincloud.chaincloudv.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.chaincloud.chaincloudv.model.Address;
import com.chaincloud.chaincloudv.model.AddressBatch;
import com.chaincloud.chaincloudv.model.Channel;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by zhumingu on 16/6/16.
 */
public class ORMLiteDBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "chaincloud-v.db";
    private static final int DATABASE_VERSION = 3;

    /**
     * 构造方法
     * @param context
     */
    public ORMLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Channel.class);
            TableUtils.createTable(connectionSource, AddressBatch.class);
            TableUtils.createTable(connectionSource, Address.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Address.class, true);
            TableUtils.dropTable(connectionSource, AddressBatch.class, true);

            TableUtils.createTable(connectionSource, AddressBatch.class);
            TableUtils.createTable(connectionSource, Address.class);

            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
