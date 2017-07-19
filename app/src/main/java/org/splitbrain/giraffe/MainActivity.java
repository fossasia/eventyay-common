package org.splitbrain.giraffe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Context context;
    DBAdapter db;
    SharedPreferences prefs;
    int filterstate = 0;
    ListView listview;
    TextView title;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.context = this;

        setTitle("");

        db = new DBAdapter(this);
        db.openReadOnly();

        listview = (ListView) findViewById(R.id.list);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        EventItemCursorAdapter listAdapter = new EventItemCursorAdapter(this, null);
        listview.setAdapter(listAdapter);
        setFilter(0);


        title = (TextView) findViewById(R.id.titlebar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        // pass URL intents to the option activity
        Uri intentdata = getIntent().getData();
        if (intentdata != null) {
            Intent i = new Intent(context, OptionsActivity.class);
            i.setData(intentdata);
            startActivity(i);
        }

        if (prefs.getString("url", "").equals("")) {
            AlertDialog.Builder noFeedBuilder = new AlertDialog.Builder(context);
            noFeedBuilder.setMessage(R.string.main_no_feed_text)
                    .setTitle(R.string.main_no_feed_title)
                    .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(context, OptionsActivity.class);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            noFeedBuilder.show();
        }
    }

    /**
     * Refresh list view with current data whenever the view is shown again
     *
     * @fixme we could store a dirty flag in the application context
     */
    @Override
    public void onResume() {

        setFilter(0);
        EventItemCursorAdapter eica = (EventItemCursorAdapter) listview.getAdapter();
        Cursor cursor = eica.getCursor();
        if (cursor != null) {
            cursor.requery();
        }
        super.onResume();
    }


    /**
     * Set the filter state and initialize the appropriate cursor
     *
     * @param filter 0 (for init) 1 (for favs) 2 (for future)
     */
    private void setFilter(int filter) {
        if (filter == 0) {
            // no filter changed, load from preferences
            filterstate = prefs.getInt("filterstate", 0);
        } else {
            if ((filterstate & filter) > 0) {
                // filter is on -> switch off
                filterstate = filterstate - filter;
            } else {
                // filter is off -> switch on
                filterstate = filterstate + filter;
            }
            // save changed filter
            Editor edit = prefs.edit();
            edit.putInt("filterstate", filterstate);
            edit.commit();

            // show info
            Resources res = getResources();
            String msg = res.getString(res.getIdentifier("org.splitbrain.giraffe:string/filter" + filterstate, null, null));
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.show();
        }

        // create WHERE clause
        String where = "";
        if ((filterstate & 1) > 0) {
            where += DBAdapter.FAVORITE + " > 0 ";
        }
        if ((filterstate & 2) > 0) {
            if (where.length() > 0) {
                where += " AND ";
            }
            where += "(datetime(" + DBAdapter.STARTS + ",'unixexpoch') > datetime('now') OR datetime(" + DBAdapter.ENDS + ",'unixepoch') > datetime('now') )";
        }

        // apply the filter
        Cursor cursor = db.getEventsCursor(where);
        startManagingCursor(cursor);
        EventItemCursorAdapter eica = (EventItemCursorAdapter) listview.getAdapter();
        eica.changeCursor(cursor);
    }

    public void titleOption(View view) {

        new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setTitle("Options")
                .setMessage("Select the option to change URL or read about the app.")
                .setPositiveButton("URL",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this, OptionsActivity.class);
                                startActivity(i);
                            }
                        })
                .setNegativeButton("About",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this, AboutActivity.class);
                                startActivity(i);
                            }
                        }).create().show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_bookmark) {
            setFilter(1);
        } else if (id == R.id.action_filter) {
            setFilter(2);
        }
        return super.onOptionsItemSelected(item);
    }

}
