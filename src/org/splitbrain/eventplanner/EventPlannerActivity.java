package org.splitbrain.eventplanner;

import java.io.IOException;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.ListView;
import android.util.Log;

public class EventPlannerActivity extends Activity {
	private DBAdapter db;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	setContentView(R.layout.main);
        	ListView list = (ListView) findViewById(R.id.listView1);

        	db = new DBAdapter(this);
        	db.open();
        	db.close();
    }
}