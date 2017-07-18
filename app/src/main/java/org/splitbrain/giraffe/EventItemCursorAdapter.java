package org.splitbrain.giraffe;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventItemCursorAdapter extends RecyclerView.Adapter<EventItemCursorAdapter.ViewHolder> {
    private Context context;
    private Cursor c;

    public EventItemCursorAdapter(Context context, Cursor c) {

        this.context = context;
        this.c = c;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTV;
        TextView locationTV;
        TextView speakerTV;
        TextView dayTV;
        TextView timeTV;
        ImageView favIV;
        LinearLayout tableLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTV = (TextView) itemView.findViewById(R.id.title);
            locationTV = (TextView) itemView.findViewById(R.id.location);
            speakerTV = (TextView) itemView.findViewById(R.id.speaker);
            dayTV = (TextView) itemView.findViewById(R.id.day);
            timeTV = (TextView) itemView.findViewById(R.id.time);
            favIV = (ImageView) itemView.findViewById(R.id.favorite);
            tableLayout = (LinearLayout) itemView.findViewById(R.id.tableLayout1);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.listitem, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        EventRecord record = DBAdapter.getEventFromCursor(c);

        holder.titleTV.setText(record.title);
        holder.locationTV.setText(record.location);
        holder.speakerTV.setText(record.speaker);
        holder.favIV.setClickable(true);

        if (record.favorite) {
            holder.favIV.setImageResource(R.drawable.icon);
        } else {
            holder.favIV.setImageResource(R.drawable.icongray);
        }
        holder.favIV.setTag(record.id);
        holder.favIV.setOnClickListener(click_favorite);

        SimpleDateFormat df;
        df = new SimpleDateFormat("E");
        holder.dayTV.setText(df.format(new Date(record.starts * 1000)));

        df = new SimpleDateFormat("HH:mm");
        holder.timeTV.setText(df.format(new Date(record.starts * 1000)));


        holder.tableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String id = Integer.toString(position);
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("id", id);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
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

}
