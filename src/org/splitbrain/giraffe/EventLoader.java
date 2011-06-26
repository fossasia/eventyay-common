package org.splitbrain.giraffe;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.splitbrain.simpleical.SimpleIcalEvent;
import org.splitbrain.simpleical.SimpleIcalParser;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class EventLoader extends AsyncTask<URL, String, String>{
    private final OptionsActivity context;

    public EventLoader(OptionsActivity context){
	this.context = context;
    }

    @Override
    protected void onPreExecute(){
	Log.e("eventloader","preExecute");
	//db.begin();
    }

    @Override
    protected String doInBackground(URL... urls) {
	Log.e("eventloader","doInBackground");
	return fetchEvents(urls[0]);
    }

    @Override
    protected void onCancelled(){
        Toast toast = Toast.makeText(context, "Loading cancelled", Toast.LENGTH_LONG);
        toast.show();
        context.resetLayout();
    }

    @Override
    protected void onPostExecute(String msg){
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
        context.resetLayout();
    }

    @Override
    protected void onProgressUpdate(String... values) {
	context.writeProgress(values[0]);
    }

    // FIXME take URL as  parameter
    public String fetchEvents(URL url){
        int count = 0;

        publishProgress("Opening database...");
	DBAdapter db = new DBAdapter(context);
	db.open();
	db.begin();

        // http://re-publica.de/11/rp2011.ics
        try{
            publishProgress("Connecting to iCal URL...");
            InputStream inputStream = url.openStream();


            publishProgress("Clearing database...");
    	    db.deleteEvents();

            //AssetManager assetManager = context.getAssets();
            //InputStream inputStream = assetManager.open("rp2011.ics");

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
                publishProgress("Loaded "+count+" events...");

                // abort if cancelled
                if(isCancelled()){
                    Log.e("eventloader","cancel");
                    db.rollback();
                    return "Cancelled";
                }

                // FIXME
        	Log.e("eventloader",event.get("SUMMARY"));
            }
            db.commit();
        } catch (Exception e) {
            db.rollback();
            return "Failed to read from "+e.toString();
        }
        return "Sucessfully loaded "+count+"entries.";
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
