
package com.pulkit.musicplayer.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pulkit.musicplayer.R;

/**
 * @author PULKIT MITAL
 * @date 31/7/2016
 */

public class MusicPlayerDBHelper extends SQLiteOpenHelper {
    private Context context_;
    private static String DATABASE_NAME = "";
    private static int DATABASE_VERSION = 0;
    private String DB_PATH = "";
    private SQLiteDatabase db;

    public MusicPlayerDBHelper(Context context) {
        super(context, context.getResources().getString(R.string.DataBaseName), null, Integer.parseInt(context.getResources().getString(R.string.DataBaseName_Version)));
        this.context_ = context;
        DATABASE_NAME = context.getResources().getString(R.string.DataBaseName);
        DATABASE_VERSION = Integer.parseInt(context.getResources().getString(R.string.DataBaseName_Version));
        DB_PATH = context.getDatabasePath(DATABASE_NAME).getPath();
        context.openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE, null);
    }

    public SQLiteDatabase getDB() {
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(sqlForCreateMostPlay());
            db.execSQL(sqlForCreateFavoritePlay());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + MostRecentTableHelper.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + FavoriteTableHelper.TABLE_NAME);
            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {
        if (getDB() != null)
            getDB().close();

        super.close();
    }

    public void openDataBase() throws SQLException {
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String sqlForCreateMostPlay() {
        String sql = "CREATE TABLE " + MostRecentTableHelper.TABLE_NAME + " (" + MostRecentTableHelper.ID + " INTEGER NOT NULL PRIMARY KEY,"
                + MostRecentTableHelper.ALBUM_ID + " INTEGER NOT NULL,"

                + MostRecentTableHelper.ARTIST + " TEXT NOT NULL,"
                + MostRecentTableHelper.TITLE + " TEXT NOT NULL,"
                + MostRecentTableHelper.DISPLAY_NAME + " TEXT NOT NULL,"
                + MostRecentTableHelper.DURATION + " TEXT NOT NULL,"
                + MostRecentTableHelper.PATH + " TEXT NOT NULL,"
                + MostRecentTableHelper.AUDIOPROGRESS + " TEXT NOT NULL,"
                + MostRecentTableHelper.AUDIOPROGRESSSEC + " INTEGER NOT NULL,"
                + MostRecentTableHelper.LastPlayTime + " TEXT NOT NULL,"
                + MostRecentTableHelper.PLAYCOUNT + " INTEGER NOT NULL);";
        return sql;
    }

    public static String sqlForCreateFavoritePlay() {
        String sql = "CREATE TABLE " + FavoriteTableHelper.TABLE_NAME + " (" + FavoriteTableHelper.ID + " INTEGER NOT NULL PRIMARY KEY,"
                + FavoriteTableHelper.ALBUM_ID + " INTEGER NOT NULL,"

                + FavoriteTableHelper.ARTIST + " TEXT NOT NULL,"
                + FavoriteTableHelper.TITLE + " TEXT NOT NULL,"
                + FavoriteTableHelper.DISPLAY_NAME + " TEXT NOT NULL,"
                + FavoriteTableHelper.DURATION + " TEXT NOT NULL,"
                + FavoriteTableHelper.PATH + " TEXT NOT NULL,"
                + FavoriteTableHelper.AUDIOPROGRESS + " TEXT NOT NULL,"
                + FavoriteTableHelper.AUDIOPROGRESSSEC + " INTEGER NOT NULL,"
                + FavoriteTableHelper.LastPlayTime + " TEXT NOT NULL,"
                + FavoriteTableHelper.ISFAVORITE + " INTEGER NOT NULL);";
        return sql;
    }

}
