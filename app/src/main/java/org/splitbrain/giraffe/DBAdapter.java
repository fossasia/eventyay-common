package org.splitbrain.giraffe;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DBAdapter {
    private static final String DATABASE_NAME = "EventPlanner";
    private static final int DATABASE_VERSION = 1;

    private final DatabaseHelper DBHelper;
    private SQLiteDatabase db = null;
    ;


    public final static String EVENT_ID = "events._id";
    public final static String FAVORITE_ID = "favorites._id";

    public final static String STARTS = "starts";
    public final static String ENDS = "ends";
    public final static String TITLE = "title";
    public final static String DESCRIPTION = "description";
    public final static String LOCATION = "location";
    public final static String SPEAKER = "speaker";
    public final static String URL = "url";
    public final static String FAVORITE = "favorite";

    /**
     * Database filed names as read in getEvent(s)
     */
    private final String[] FIELDS = {
            EVENT_ID,
            STARTS,
            ENDS,
            TITLE,
            DESCRIPTION,
            LOCATION,
            SPEAKER,
            URL,
            FAVORITE
    };

    public DBAdapter(Context context) {
        DBHelper = new DatabaseHelper(context);
    }

    /**
     * Open the Database
     *
     * @return this
     * @throws SQLException
     */
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    /**
     * Open the Database
     *
     * @return this
     * @throws SQLException
     */
    public DBAdapter openReadOnly() throws SQLException {
        db = DBHelper.getReadableDatabase();
        return this;
    }

    /**
     * Close the database
     */
    public void close() {
        if (db != null) DBHelper.close();
        db = null;
    }

    /**
     * Begin a transaction
     */
    public void begin() {
        if (db == null) open();
        db.beginTransaction();
    }

    /**
     * Commit all changes made during the current transaction and end it
     */
    public void commit() {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Revert all changes made during the current transaction and end it
     */
    public void rollback() {
        db.endTransaction();
    }

    /**
     * Sets/unsets the favorite state of the given event
     *
     * @param id ID of the entry to favorite
     * @return the new state
     */
    public boolean toggleFavorite(String id) {
        if (db == null) open();

        // read current state
        boolean fav = false;
        Cursor result = db.query("favorites", new String[]{"favorite"}, "_id=?", new String[]{id}, null, null, null);
        if (result.moveToFirst()) {
            if (result.getInt(0) > 0) fav = true;
        }
        result.close();

        // flip
        fav = !fav;

        // save
        ContentValues row = new ContentValues();
        row.put("_id", id);
        row.put("favorite", fav);
        db.replace("favorites", null, row);

        return fav;
    }

    /**
     * Fetch a single event identified by its id from the database
     *
     * @param id ID of the record to return
     * @return the record
     */
    public EventRecord getEvent(String id) {
        EventRecord record = new EventRecord();

        if (db == null) open();
        Cursor result = db.query("events LEFT OUTER JOIN favorites ON (events._id = favorites._id)",
                FIELDS, "events._id=?", new String[]{id}, null, null, null);

        if (result.moveToFirst()) {
            record = getEventFromCursor(result);
        }
        result.close();

        return record;
    }

    /**
     * Creates an Event record from the given Cursor
     * <p/>
     * Results need to be in order of FIELDS
     *
     * @param row The row cursor
     * @return The record the cursor is pointing to
     */
    public static EventRecord getEventFromCursor(Cursor row) {
        EventRecord record = new EventRecord();
        record.id = row.getString(row.getColumnIndex("_id")); //getColumnIndex doesn't work with table names WTF!?
        record.starts = row.getLong(row.getColumnIndex(STARTS));
        record.ends = row.getLong(row.getColumnIndex(ENDS));
        record.title = row.getString(row.getColumnIndex(TITLE));
        record.description = row.getString(row.getColumnIndex(DESCRIPTION));
        record.location = row.getString(row.getColumnIndex(LOCATION));
        record.speaker = row.getString(row.getColumnIndex(SPEAKER));
        record.url = row.getString(row.getColumnIndex(URL));
        if (row.getInt(row.getColumnIndex(FAVORITE)) > 0) {
            record.favorite = true;
        }
        return record;
    }

    public Cursor getEventsCursor(String where) {
        if (db == null) open();
        return db.query("events LEFT OUTER JOIN favorites ON (" + EVENT_ID + " = " + FAVORITE_ID + ")",
                FIELDS, where, null, null, null, STARTS);
    }

    //FIXME add event name later
    public void deleteEvents() {
        if (db == null) open();
        db.delete("events", "1", null);
    }

    public void addEventRecord(EventRecord record) {
        if (db == null) open();

        ContentValues row = new ContentValues();
        row.put("_id", record.id);
        row.put("starts", record.starts);
        row.put("ends", record.ends);
        row.put("title", record.title);
        row.put("description", record.description);
        row.put("location", record.location);
        row.put("speaker", record.speaker);
        row.put("url", record.url);

        db.insert("events", null, row);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private final Context context;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        /**
         *
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            if (executeQueryFile(db, "db/1.sql")) {
                Toast toast = Toast.makeText(context, "Database created", Toast.LENGTH_LONG);
                toast.show();
            }
        }

        /**
         *
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for (int version = oldVersion + 1; version <= newVersion; version++) {
                if (executeQueryFile(db, "db/" + version + ".sql")) {
                    Toast toast = Toast.makeText(context, "Database updated from " + oldVersion + " to " + newVersion, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }

        /**
         * Execute the given file as a series of SQL queries within a transaction
         *
         * @param db       - the SQLite database reference
         * @param filename - the path of the file to load from assets
         * @return true if the execution was successful
         */
        private boolean executeQueryFile(SQLiteDatabase db, String filename) {
            boolean ok = false;
            String[] queries = loadQueryFile(filename);
            if (queries == null) return false;

            db.beginTransaction();
            String sql;
            try {
                for (String querie : queries) {
                    sql = querie.trim();
                    if (sql.length() == 0) continue;
                    db.execSQL(sql);
                }
                db.setTransactionSuccessful();
                ok = true;
            } catch (Exception ignored) {
            } finally {
                db.endTransaction();
            }

            return ok;
        }

        /**
         * Load SQL queries from the given Assetfile
         *
         * @return separate queries
         */
        private String[] loadQueryFile(String filename) {

            AssetManager assetManager = context.getAssets();
            InputStream inputStream;
            try {
                inputStream = assetManager.open(filename);
            } catch (IOException e) {
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                return null;
            }

            return outputStream.toString().split(";[\r\n]+");
        }

    }
}
