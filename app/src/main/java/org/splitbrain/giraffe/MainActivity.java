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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Context context;
    DBAdapter db;
    SharedPreferences prefs;
    int filterstate = 0;
    RecyclerView recyclerView;
    EventItemCursorAdapter listAdapter;
    LinearLayoutManager layoutManager;
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

        title = (TextView) findViewById(R.id.titlebar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        listAdapter = new EventItemCursorAdapter(this, null);
        recyclerView.setAdapter(listAdapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        setFilter(0);

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
            AlertDialog.Builder noFeedBuilder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
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

        recyclerView = (RecyclerView) findViewById(R.id.list);
        listAdapter = new EventItemCursorAdapter(this, null);
        recyclerView.setAdapter(listAdapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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
        // update button images
        /*ImageView iv1 = (ImageView) findViewById(R.id.filterbtn_fav);
        ImageView iv2 = (ImageView) findViewById(R.id.filterbtn_future);*/
        /*if ((filterstate & 1) > 0) {
            iv1.setImageResource(R.drawable.filter1_on);
        } else {
            iv1.setImageResource(R.drawable.filter1_off);
        }
        if ((filterstate & 2) > 0) {
            iv2.setImageResource(R.drawable.filter2_on);
        } else {
            iv2.setImageResource(R.drawable.filter2_off);
        }*/

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
        recyclerView = (RecyclerView) findViewById(R.id.list);
        listAdapter = new EventItemCursorAdapter(this, cursor);
        recyclerView.setAdapter(listAdapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void titleOption(View view) {

        new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setTitle("Options")
                .setMessage("Select the option to see options or read about the app.")
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

    private final OnClickListener click_filter = new OnClickListener() {
        public void onClick(View v) {
            int state = Integer.parseInt((String) v.getTag());
            setFilter(state);
        }
    };

    private final OnClickListener click_title = new OnClickListener() {
        public void onClick(View view) {
            openOptionsMenu();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_bookmark) {
            int state = Integer.parseInt("1");
            setFilter(state);
        } else if (id == R.id.action_filter) {
            int state = Integer.parseInt("2");
            setFilter(state);
        }
        return super.onOptionsItemSelected(item);
    }

}
