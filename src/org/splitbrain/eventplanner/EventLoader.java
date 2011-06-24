package org.splitbrain.eventplanner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class EventLoader {
    public ArrayList<EventRecord> records;


    public EventLoader(Context context){
        // FIXME take URL as second parameter
        records = new ArrayList<EventRecord>();

        // http://re-publica.de/11/rp2011.ics
        try{
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("rp2011.ics");

            // read Calendar file
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(new InputStreamReader(inputStream));
            ComponentList callist = calendar.getComponents();

            // open Database
            DBAdapter db = new DBAdapter(context);
            db.open();
            
            // remove previous entries
            db.deleteEvents();
            
            // iterate through events
            Iterator<Component> itr = callist.iterator();
            while(itr.hasNext()){
                Component component = itr.next();
                if(component instanceof VEvent){
                    EventRecord record = getEventRecord(component.getProperties());
                    if(record == null) continue;
                    records.add(record); //FIXME remove later
                    
                    // add to database:
                    db.addEventRecord(record);
                }
            }

            db.close();
            
        } catch (Exception e) {
            Log.e("calender","Failed to open Asset File. "+e.toString());
        }
    }


    private EventRecord getEventRecord(PropertyList data){
        Uid uid = (Uid) data.getProperty(Property.UID);
        if(uid == null) return null;

        Summary summary = (Summary) data.getProperty(Property.SUMMARY);
        if(summary == null) return null;

        DtStart dateStart = (DtStart) data.getProperty(Property.DTSTART);
        if(dateStart == null) return null;

        DtEnd dateEnd = (DtEnd) data.getProperty(Property.DTEND);
        Location location = (Location) data.getProperty(Property.LOCATION);
        Organizer organizer = (Organizer) data.getProperty(Property.ORGANIZER);

        EventRecord record = new EventRecord();
        record.id = uid.getValue();
        record.title = summary.getValue();
        record.starts = dateStart.getDate().getTime()/1000;
        if(dateEnd != null)   record.ends = dateEnd.getDate().getTime()/1000;
        if(location != null)  record.location = location.getValue();
        if(organizer != null) record.speaker  = organizer.getParameter(Parameter.CN).getValue();

        return record;
    }
}
