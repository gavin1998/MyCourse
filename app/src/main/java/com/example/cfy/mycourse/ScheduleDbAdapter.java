package com.example.cfy.mycourse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by cfy on 2015/2/11.
 */
public class ScheduleDbAdapter {
    //数据库对应的列
    public static final String COL_ID = "_id";
    public static final String COL_CONTENT = "content";

    //以及它们对应的序列号
    public static final int INDEX_ID = 0;
    public static final int INDEX_CONTENT = INDEX_ID + 1;

    //用于日志
    private static final String TAG = "ScheduleDbAdapter";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private static final String DATABASE_NAME = "dba_myCourse";
    private static final String TABLE_NAME = "tbl_schedule";
    private static final int DATABASE_VERSION = 1;
    private final Context mCtx;
    //SQL statement used to create the database
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    COL_CONTENT + " TEXT);";

    public ScheduleDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    //open
    public void open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
    }
    //close
    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    //CREATE
    //note that the id will be created for you automatically
    public void createSchedule(String name) {
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT, name);
        mDb.insert(TABLE_NAME, null, values);
    }
    //overloaded to take a Schedule
    public long createSchedule(Schedule schedule) {
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT, schedule.getContent());
        // Inserting Row
        return mDb.insert(TABLE_NAME, null, values);
    }
    //READ
    public Schedule fetchScheduleById(int id) {

        Cursor cursor = mDb.query(TABLE_NAME, new String[]{COL_ID,
                        COL_CONTENT}, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        if (cursor != null)
            cursor.moveToFirst();
        return new Schedule(
                cursor.getInt(INDEX_ID),
                cursor.getString(INDEX_CONTENT)
        );
    }
    public Cursor fetchAllSchedule() {
        Cursor mCursor = mDb.query(TABLE_NAME, new String[]{COL_ID,
                        COL_CONTENT},
                null, null, null, null, null
        );
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    //UPDATE
    public void updateSchedule(Schedule schedule) {
        ContentValues values = new ContentValues();
        values.put(COL_CONTENT, schedule.getContent());

        mDb.update(TABLE_NAME, values,
                COL_ID + "=?", new String[]{String.valueOf(schedule.getId())});
    }
    //DELETE
    public void deleteScheduleById(int nId) {
        mDb.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(nId)});
    }
    public void deleteAllSchedule() {
        mDb.delete(TABLE_NAME, null, null);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}

