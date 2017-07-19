package org.splitbrain.giraffe;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventItemCursorAdapter extends CursorAdapter {
    Context context;

    public EventItemCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
    }


    /**
     * fill a given view with the data at the cursor
     */
    private void updateView(View v, Cursor c) {
        TextView titleTV = (TextView) v.findViewById(R.id.title);
        TextView locationTV = (TextView) v.findViewById(R.id.location);
        TextView speakerTV = (TextView) v.findViewById(R.id.speaker);
        TextView dayTV = (TextView) v.findViewById(R.id.day);
        TextView timeTV = (TextView) v.findViewById(R.id.time);
        ImageView favIV = (ImageView) v.findViewById(R.id.favorite);

        EventRecord record = DBAdapter.getEventFromCursor(c);

        titleTV.setText(record.title);
        locationTV.setText(record.location);
        speakerTV.setText(record.speaker);
        favIV.setClickable(true);

        if (record.favorite) {
            favIV.setImageResource(R.drawable.icon);
        } else {
            favIV.setImageResource(R.drawable.icongray);
        }
        favIV.setTag(record.id);
        favIV.setOnClickListener(click_favorite);

        SimpleDateFormat df;
        df = new SimpleDateFormat("E");
        dayTV.setText(df.format(new Date(record.starts * 1000)));

        df = new SimpleDateFormat("HH:mm");
        timeTV.setText(df.format(new Date(record.starts * 1000)));

        v.setTag(record.id);
        v.setClickable(true);
        v.setOnClickListener(click_row);
    }

    /**
     * Create a new view from our XML template
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.listitem, parent, false);

        updateView(view, cursor);

        return view;
    }

    /**
     * Recycle a view
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        updateView(view, cursor);
    }

    /**
     * Handles clicks on the favorite Icon
     */
    private final OnClickListener click_favorite = new OnClickListener() {
        public void onClick(View fav) {
            ImageView favIV = (ImageView) fav;

            // toggle fav
            DBAdapter db = new DBAdapter(context);
            db.open();
            boolean isfav = db.toggleFavorite((String) favIV.getTag());
            if (isfav) {
                favIV.setImageResource(R.drawable.icon);
            } else {
                favIV.setImageResource(R.drawable.icongray);
            }
            db.close();
        }
    };

    /**
     * Handles clicks on the whole row
     */
    private final OnClickListener click_row = new OnClickListener() {
        public void onClick(View row) {
            String id = (String) row.getTag();
            Intent i = new Intent(context, DetailActivity.class);
            i.putExtra("id", id);
            context.startActivity(i);
        }
    };
}
