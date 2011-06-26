package org.splitbrain.giraffe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EventItemAdapter extends ArrayAdapter<EventRecord> {
    private final Context context;
    private final ArrayList<EventRecord> records;

    public EventItemAdapter(Context context, int textViewResourceId, ArrayList<EventRecord> records) {
        super(context, textViewResourceId, records);
        this.records = records;
        this.context = context;
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
            ImageView favIV	= (ImageView) v.findViewById(R.id.favorite);

            titleTV.setText(record.title);
            locationTV.setText(record.location);
            speakerTV.setText(record.speaker);
    	    favIV.setClickable(true);

            if(record.favorite){
        	favIV.setImageResource(R.drawable.icon);
            }else{
        	favIV.setImageResource(R.drawable.icongray);
            }
            favIV.setTag(record.id);
            favIV.setOnClickListener(click_favorite);

            SimpleDateFormat df;
            df = new SimpleDateFormat("E");
            dayTV.setText( df.format(new Date(record.starts*1000)));

            df = new SimpleDateFormat("HH:mm");
            timeTV.setText( df.format(new Date(record.starts*1000)));
        }
        return v;
    }

    /**
     * Handles clicks on the favorite Icon
     */
    private final OnClickListener click_favorite = new OnClickListener(){
	public void onClick(View fav) {
	    ImageView favIV = (ImageView) fav;

	    // toggle fav
	    DBAdapter db = new DBAdapter(context);
	    db.open();
	    boolean isfav = db.toggleFavorite((String) favIV.getTag());
	    if(isfav){
        	favIV.setImageResource(R.drawable.icon);
            }else{
        	favIV.setImageResource(R.drawable.icongray);
            }
	    db.close();
	}
    };
}
