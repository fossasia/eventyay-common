package org.splitbrain.eventplanner;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EventPlannerActivity extends Activity {
    Context context; 
    DBAdapter db;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);

            this.context = this;
            
            ListView list = (ListView) findViewById(R.id.listView1);

            TextView titlebar = (TextView) findViewById(R.id.titlebar);
            titlebar.setOnClickListener(onRefresh);
            
            //String[] items = {"red","blue","green"};
            //list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));

            // fake 1st entry
            //EventLoader el = new EventLoader(this);

            db = new DBAdapter(this);
            db.open();
            ArrayList<EventRecord> records = db.getEvents();
            //db.close();

            /*
            new ArrayList<EventRecord>();
            for(int i=0; i<30; i++){
                EventRecord record = new EventRecord();
                record.event = "Test"+i;
                record.location = "Test"+i;
                record.speaker = "Test"+i;
                records.add(record);
            }
            */



            list.setAdapter(new EventItemAdapter(this,R.layout.listitem,records));
    }      

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item1 = menu.add("Options");
        item1.setIcon(android.R.drawable.ic_menu_preferences);
        
        MenuItem item2 = menu.add("About");
        item2.setIcon(android.R.drawable.ic_menu_info_details);
        item2.setOnMenuItemClickListener(clickAbout);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    private OnMenuItemClickListener clickAbout = new OnMenuItemClickListener(){
	public boolean onMenuItemClick(MenuItem arg0) {
	    AlertDialog ad = new AlertDialog.Builder(context).create();
	    ad.setCanceledOnTouchOutside(true);
	    ad.setTitle(R.string.app_name);
	    ad.setMessage(context.getText(R.string.app_about));  
	    ad.show();
	    return true;
	}
    };
    
    private OnClickListener onRefresh = new OnClickListener() {
	public void onClick(View v){
	    ProgressBar prg = (ProgressBar) findViewById(R.id.progress);
	    prg.setVisibility(ProgressBar.VISIBLE);
	    
	    EventLoader ep = new EventLoader(context);
	    ep.execute();
	}
    };
}
