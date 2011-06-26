package org.splitbrain.giraffe;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OptionsActivity extends Activity {
    Context context;
    SharedPreferences prefs;
    EventLoader eventloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.options);
	this.context = this;

	resetLayout();

	// prepare event loader
	eventloader = new EventLoader(this);

	// attach Event listeners
	Button btn_refresh = (Button) findViewById(R.id.opt_btn_refresh);
	btn_refresh.setOnClickListener(click_refresh);
	Button btn_cancel = (Button) findViewById(R.id.opt_btn_cancel);
	btn_cancel.setOnClickListener(click_cancel);
    }

    public void resetLayout(){
	// set URL field from preferences
	prefs = PreferenceManager.getDefaultSharedPreferences(this);
	EditText txturl = (EditText) findViewById(R.id.opt_txt_url);
	txturl.setText(prefs.getString("url", ""));

	// hide progress panel
	LinearLayout ll = (LinearLayout) findViewById(R.id.opt_panel_running);
	ll.setVisibility(View.GONE);

	// empty output
	TextView txtprg = (TextView) findViewById(R.id.opt_txt_progress);
	txtprg.setText("");

	// show refresh button
	Button btn_refresh = (Button) findViewById(R.id.opt_btn_refresh);
	btn_refresh.setVisibility(View.VISIBLE);
    }

    public void writeProgress(String text){
	TextView txtprg = (TextView) findViewById(R.id.opt_txt_progress);
	txtprg.setText(text);
    }

    private final OnClickListener click_cancel = new OnClickListener() {
	public void onClick(View v){
	    eventloader.cancel(true);
	}
    };

    private final OnClickListener click_refresh = new OnClickListener() {
	public void onClick(View v){
	    // check if URL was set
	    EditText txturl = (EditText) findViewById(R.id.opt_txt_url);
	    String url = txturl.getText().toString().trim();
	    if(url.length() == 0){
		Toast toast = Toast.makeText(context, "No valid URL given", Toast.LENGTH_SHORT);
	        toast.show();
		return;
	    }

	    URL link = null;
	    try {
		link = new URL(url);
	    } catch (MalformedURLException e) {
		Toast toast = Toast.makeText(context, "Not valid URL given", Toast.LENGTH_SHORT);
	        toast.show();
		return;
	    }

	    // save the URL
	    Editor edit = prefs.edit();
	    edit.putString("url", url);

	    // hide the button
	    v.setVisibility(View.GONE);

	    // show the progress
	    LinearLayout ll = (LinearLayout) findViewById(R.id.opt_panel_running);
	    ll.setVisibility(View.VISIBLE);

	    // run the action
	    eventloader.execute(link);
	}
    };


}
