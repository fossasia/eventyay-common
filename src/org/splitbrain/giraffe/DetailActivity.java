package org.splitbrain.giraffe;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends Activity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        this.context = this;

        String id = "";
        Bundle extras = getIntent().getExtras();
        if(extras != null) id = extras.getString("id");

        DBAdapter db = new DBAdapter(this);
        db.open();
        EventRecord record = db.getEvent(id);
        db.close();

        TextView tv;
        tv = (TextView) findViewById(R.id.detail_title);
        tv.setText(record.title);

        tv = (TextView) findViewById(R.id.detail_text);
        tv.setText(record.description);

        tv = (TextView) findViewById(R.id.detail_speaker);
        if(record.speaker.length() > 0){
            tv.setText(record.speaker);
        }else{
            tv.setVisibility(View.GONE);
        }

        tv = (TextView) findViewById(R.id.detail_location);
        if(record.location.length() > 0){
            tv.setText(record.location);
        }else{
            tv.setVisibility(View.GONE);
        }


        tv = (TextView) findViewById(R.id.detail_url);
        if(record.url.length() > 0){
            tv.setTag(record.url);
            tv.setOnClickListener(click_url);
            tv.setVisibility(View.VISIBLE);
        }else{
            tv.setVisibility(View.GONE);
        }

        String dt = "";
        SimpleDateFormat df = new SimpleDateFormat("E, MMM d HH:mm");
        dt = df.format(new Date(record.starts*1000));
        if(record.ends > 0){
            df = new SimpleDateFormat("HH:mm");
            dt += " - "+df.format(new Date(record.ends*1000));
        }

        tv = (TextView) findViewById(R.id.detail_timeslot);
        tv.setText(dt);

        ImageView iv = (ImageView) findViewById(R.id.detail_favorite);
        iv.setClickable(true);
        if(record.favorite){
            iv.setImageResource(R.drawable.icon);
        }else{
            iv.setImageResource(R.drawable.icongray);
        }
        iv.setTag(record.id);
        iv.setOnClickListener(click_favorite);

    }

    /**
     * Handles clicks on the favorite Icon
     *
     * @FIXME duplicates code from EventItemAdapter
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

    /**
     * Handles clicks on the URL
     *
     * Opens the web browser
     */
    private final OnClickListener click_url = new OnClickListener(){
        public void onClick(View link) {
            TextView tv = (TextView) link;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse((String) tv.getTag()));
            startActivity(i);
        }
    };
}
