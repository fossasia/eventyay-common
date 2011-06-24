package org.splitbrain.eventplanner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EventItemAdapter extends ArrayAdapter<EventRecord> {
    private ArrayList<EventRecord> records;

    public EventItemAdapter(Context context, int textViewResourceId, ArrayList<EventRecord> records) {
        super(context, textViewResourceId, records);
        this.records = records;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.listitem, null);
        }

        EventRecord record = records.get(position);
        if (record != null) {
            TextView titleTV    = (TextView) v.findViewById(R.id.title);
            TextView locationTV = (TextView) v.findViewById(R.id.location);
            TextView speakerTV  = (TextView) v.findViewById(R.id.speaker);
            TextView dayTV	= (TextView) v.findViewById(R.id.day);
            TextView timeTV	= (TextView) v.findViewById(R.id.time);
            
            titleTV.setText(record.title);
            locationTV.setText(record.location);
            speakerTV.setText(record.speaker);
            
            SimpleDateFormat df;
            df = new SimpleDateFormat("E");
            dayTV.setText( df.format(new Date(record.starts*1000)));
            
            df = new SimpleDateFormat("HH:mm");
            timeTV.setText( df.format(new Date(record.starts*1000)));
        }
        return v;
    }
}
