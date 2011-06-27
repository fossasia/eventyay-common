package org.splitbrain.giraffe;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

public class MainActivity extends ListActivity {
    Context context;
    DBAdapter db;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            this.context = this;

            db = new DBAdapter(this);
            db.openReadOnly();
            Cursor cursor = db.getEventsCursor();
            startManagingCursor(cursor);


            EventItemCursorAdapter listAdapter = new EventItemCursorAdapter(this,cursor);
            setListAdapter(listAdapter);
    }

    /**
     * Refresh list view with current data whenever the view is shown again
     */
    @Override
    public void onResume() {
	EventItemCursorAdapter eica = (EventItemCursorAdapter) getListAdapter();
	eica.getCursor().requery();
        super.onResume();
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

}
