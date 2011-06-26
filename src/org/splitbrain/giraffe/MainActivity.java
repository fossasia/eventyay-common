package org.splitbrain.giraffe;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {
    Context context;
    DBAdapter db;
    EventItemAdapter listAdapter;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);

            this.context = this;

            ListView list = (ListView) findViewById(R.id.listView1);

            db = new DBAdapter(this);
            db.open();
            ArrayList<EventRecord> records = db.getEvents();
            db.close();

            listAdapter = new EventItemAdapter(this,R.layout.listitem,records);
            list.setAdapter(listAdapter);
    }

    /**
     * Refresh list view whenever the view is shown again
     *
     * @FIXME doesn't work
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
	/*
	if(hasFocus){
	    listAdapter.notifyDataSetChanged();
	}
	*/
        super.onWindowFocusChanged(hasFocus);
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
	    AlertDialog ad = new AlertDialog.Builder(context).create();
	    ad.setCanceledOnTouchOutside(true);
	    ad.setTitle(R.string.app_name);
	    ad.setMessage(context.getText(R.string.app_about));
	    ad.show();
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
