package org.splitbrain.giraffe;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
    Context context;
    DBAdapter db;
    SharedPreferences prefs;
    int filterstate = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            this.context = this;

            db = new DBAdapter(this);
            db.openReadOnly();

            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Cursor cursor = null;
            EventItemCursorAdapter listAdapter = new EventItemCursorAdapter(this,cursor);
            setListAdapter(listAdapter);
            setFilter(0);


            ImageView iv;
            iv = (ImageView) findViewById(R.id.filterbtn_fav);
            iv.setOnClickListener(click_filter);
            iv = (ImageView) findViewById(R.id.filterbtn_future);
            iv.setOnClickListener(click_filter);
    }

    /**
     * Refresh list view with current data whenever the view is shown again
     *
     * @FIXME we could store a dirty flag in the application context
     */
    @Override
    public void onResume() {
	EventItemCursorAdapter eica = (EventItemCursorAdapter) getListAdapter();
	Cursor cursor = eica.getCursor();
	if(cursor != null){
	    cursor.requery();
	}
        super.onResume();
    }


    /**
     * Set the filter state and initialize the appropriate cursor
     *
     * @param filter 0 (for init) 1 (for favs) 2 (for future)
     */
    private void setFilter(int filter){
	if(filter == 0){
	    // no filter changed, load from preferences
	    filterstate = prefs.getInt("filterstate", 0);
	}else{
	    if((filterstate & filter) > 0){
		// filter is on -> switch off
                filterstate = filterstate - filter;
	    }else{
		// filter is off -> switch on
                filterstate = filterstate + filter;
	    }
	    // save changed filter
	    Editor edit = prefs.edit();
	    edit.putInt("filterstate", filterstate);
	    edit.commit();

	    // show info
	    Resources res = getResources();
	    String msg = res.getString(res.getIdentifier("org.splitbrain.giraffe:string/filter"+filterstate, null, null));
	    Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.show();
	}
	// update button images
	ImageView iv1 = (ImageView) findViewById(R.id.filterbtn_fav);
	ImageView iv2 = (ImageView) findViewById(R.id.filterbtn_future);
	if((filterstate & 1) > 0){
	    iv1.setImageResource(R.drawable.filter1_on);
	}else{
	    iv1.setImageResource(R.drawable.filter1_off);
	}
	if((filterstate & 2) > 0){
	    iv2.setImageResource(R.drawable.filter2_on);
	}else{
	    iv2.setImageResource(R.drawable.filter2_off);
	}

	// create WHERE clause
	String where = "";
	if((filterstate & 1) > 0){
	    where += DBAdapter.FAVORITE+" > 0 ";
	}
	if((filterstate & 2) > 0){
	    if(where.length() > 0){
		    where += " AND ";
	    }
	    where += "(datetime("+DBAdapter.STARTS+",'unixexpoch') > datetime('now') OR datetime("+DBAdapter.ENDS+",'unixepoch') > datetime('now') )";
	}

	// apply the filter
	Cursor cursor = db.getEventsCursor(where);
	startManagingCursor(cursor);
	EventItemCursorAdapter eica = (EventItemCursorAdapter) getListAdapter();
	eica.changeCursor(cursor);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item1 = menu.add(R.string.menu_urlsetup);
        item1.setIcon(android.R.drawable.ic_menu_preferences);
        item1.setOnMenuItemClickListener(click_options);

        MenuItem item2 = menu.add(R.string.menu_about);
        item2.setIcon(android.R.drawable.ic_menu_info_details);
        item2.setOnMenuItemClickListener(click_about);

        return super.onCreateOptionsMenu(menu);
    }

    private final OnMenuItemClickListener click_about = new OnMenuItemClickListener(){
	public boolean onMenuItemClick(MenuItem arg0) {
	    Intent i = new Intent(context,AboutActivity.class);
	    startActivity(i);
	    return true;
	}
    };

    private final OnMenuItemClickListener click_options = new OnMenuItemClickListener(){
	public boolean onMenuItemClick(MenuItem arg0) {
	    Intent i = new Intent(context,OptionsActivity.class);
	    startActivity(i);
	    return true;
	}
    };

    private final OnClickListener click_filter = new OnClickListener(){
	public void onClick(View v) {
	    int state = Integer.parseInt((String) v.getTag());
	    setFilter(state);
	}
    };
}
