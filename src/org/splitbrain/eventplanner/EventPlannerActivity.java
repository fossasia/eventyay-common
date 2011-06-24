package org.splitbrain.eventplanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;

public class EventPlannerActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);

            ListView list = (ListView) findViewById(R.id.listView1);

            //String[] items = {"red","blue","green"};
            //list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));

            // fake 1st entry
            //EventLoader el = new EventLoader(this);

            DBAdapter db = new DBAdapter(this);
            db.open();
            ArrayList<EventRecord> records = db.getEvents();
            db.close();

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
}
