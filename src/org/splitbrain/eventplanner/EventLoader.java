package org.splitbrain.eventplanner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.splitbrain.simpleical.SimpleIcalEvent;
import org.splitbrain.simpleical.SimpleIcalParser;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;
import android.os.AsyncTask;


public class EventLoader extends AsyncTask<Void, Integer, Integer>{
    private Context context;
    
    public EventLoader(Context context){
	this.context = context;
    }

    @Override
    protected void onPreExecute(){
	Log.e("eventloader","preExecute");
	//db.begin();
    }
    
    @Override
    protected Integer doInBackground(Void... params) {
	Log.e("eventloader","doInBackground");
	return new Integer(fetchEvents());
    }

    @Override
    protected void onCancelled(){
        Toast toast = Toast.makeText(context, "Loading cancelled", Toast.LENGTH_LONG);
        toast.show();
    }
    
    @Override
    protected void onPostExecute(Integer count){
        Toast toast = Toast.makeText(context, "Loaded "+count+" events", Toast.LENGTH_LONG);
        toast.show();
    }
    
    
    // FIXME take URL as  parameter
    public int fetchEvents(){
        int count = 0;

	DBAdapter db = new DBAdapter(context);
	db.open();
	db.begin();

        // http://re-publica.de/11/rp2011.ics
        try{
    	    db.deleteEvents();
            
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("rp2011.ics");

            SimpleIcalParser ical = new SimpleIcalParser(inputStream);
            SimpleIcalEvent event = null;
            while((event = ical.nextEvent()) != null){
        	// build record
        	EventRecord record = getEventRecord(event);
                if(record == null) continue;
                
                // add to database:
                db.addEventRecord(record);
                count++;
                
                // progress feedback
                publishProgress(count);
                
                // abort if cancelled
                if(isCancelled()){
                    Log.e("eventloader","cancel");
                    db.rollback();
                    return 0;
                }
                
                // FIXME
        	Log.e("eventloader",event.get("SUMMARY"));
            }
            db.commit();
        } catch (Exception e) {
            Log.e("calender","Failed to open Asset File. "+e.toString());
            db.rollback();
        }
        return count;
    }


    private EventRecord getEventRecord(SimpleIcalEvent event){
	// mandatory fields
        String uid     = event.get("UID");
        String summary = event.get("SUMMARY");
        Date dateStart = event.getStartDate();
        if(uid == null) return null;
        if(summary == null) return null;
        if(dateStart == null) return null;

        // optional fields
        Date dateEnd     = event.getEndDate();
        String location  = event.get("LOCATION");
        String organizer = event.get("ORGANIZER");

        // create event
        EventRecord record = new EventRecord();
        record.id     = uid;
        record.title  = summary;
        record.starts = dateStart.getTime()/1000;
        if(dateEnd   != null) record.ends     = dateEnd.getTime()/1000;
        if(location  != null) record.location = location;
        if(organizer != null) record.speaker  = organizer;

        return record;
    }
}
