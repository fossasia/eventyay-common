package org.splitbrain.giraffe;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
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

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        resetLayout();

        // attach Event listeners
        Button btn_refresh = (Button) findViewById(R.id.opt_btn_refresh);
        btn_refresh.setOnClickListener(click_refresh);
        Button btn_cancel = (Button) findViewById(R.id.opt_btn_cancel);
        btn_cancel.setOnClickListener(click_cancel);
        Button btn_barcode = (Button) findViewById(R.id.opt_btn_barcode);
        btn_barcode.setOnClickListener(click_barcode);

    }

    public void resetLayout(){
        // set URL field from intent or preferences
        EditText txturl = (EditText) findViewById(R.id.opt_txt_url);
        Uri intenturl = getIntent().getData();
        if(intenturl != null){
            txturl.setText(intenturl.toString().replaceAll("^webcal://", "http://"));
        }else{
            txturl.setText(prefs.getString("url", ""));
        }

        // hide progress panel
        LinearLayout ll = (LinearLayout) findViewById(R.id.opt_panel_running);
        ll.setVisibility(View.GONE);

        // empty output
        TextView txtprg = (TextView) findViewById(R.id.opt_txt_progress);
        txtprg.setText("");

        // show refresh button
        Button btn_refresh = (Button) findViewById(R.id.opt_btn_refresh);
        btn_refresh.setVisibility(View.VISIBLE);

        Button btn_barcode = (Button) findViewById(R.id.opt_btn_barcode);
        btn_barcode.setVisibility(View.VISIBLE);
    }

    public void writeProgress(String text){
        TextView txtprg = (TextView) findViewById(R.id.opt_txt_progress);
        txtprg.setText(text);
    }

    private final OnClickListener click_cancel = new OnClickListener() {
        public void onClick(View v){
            eventloader.cancel(false);
        }
    };

    private final OnClickListener click_barcode = new OnClickListener() {
        public void onClick(View v){
            Intent barcodeScanner = new Intent("com.google.zxing.client.android.SCAN");
            barcodeScanner.putExtra("FORMATS", "QR_CODE,DATA_MATRIX");
            // See if we have the zxing app installed. If we do, we handle the results of this later.
            try {
                startActivityForResult(barcodeScanner, 0);
            }
            // if the app isn't installed, we ask if we can download it
            catch(ActivityNotFoundException e) {
                AlertDialog.Builder notFoundBuilder = new AlertDialog.Builder(context);
                notFoundBuilder.setMessage(R.string.opt_zxing_text)
                .setTitle(R.string.opt_zxing_title)
                .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try{
                            // if so, get it from the market...
                            Intent getApp = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_market_zxing)));
                            startActivity(getApp);
                        }
                        // ...or from the website if we don't have the Market
                        catch(ActivityNotFoundException e) {
                            Intent getAppAlt = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_web_zxing)));
                            startActivity(getAppAlt);
                        }
                    }
                })
                .setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                notFoundBuilder.show();
            }
        }
    };

    private final OnClickListener click_refresh = new OnClickListener() {
        public void onClick(View v){
            // check if URL was set
            EditText txturl = (EditText) findViewById(R.id.opt_txt_url);
            String url = txturl.getText().toString().trim();
            if(url.length() == 0){
                Toast toast = Toast.makeText(context, R.string.opt_error_no_url, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            URL link = null;
            try {
                link = new URL(url);
            } catch (MalformedURLException e) {
                Toast toast = Toast.makeText(context, R.string.opt_error_bad_url, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            // save the URL
            Editor edit = prefs.edit();
            edit.putString("url", url);
            edit.commit();

            // hide the buttons
            v.setVisibility(View.GONE);
            Button btn_barcode = (Button) findViewById(R.id.opt_btn_barcode);
            btn_barcode.setVisibility(View.GONE);

            // show the progress
            LinearLayout ll = (LinearLayout) findViewById(R.id.opt_panel_running);
            ll.setVisibility(View.VISIBLE);

            eventloader = new EventLoader(OptionsActivity.this);
            eventloader.setIgnoreSSLCerts(((CheckBox) findViewById(R.id.opt_ignoressl)).isChecked()) ;
            eventloader.execute(link);
        }
    };

    // This is the callback that handles barcode scans.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // First, check to see if the scanner returned successfully
        if(resultCode == RESULT_OK) {
            // now, we see if there's any barcode data...
            String barcodeText = data.getStringExtra("SCAN_RESULT");
            if (barcodeText != "") {
                // Success! Now to fill in the new URL. The user still needs to press the load button to commit it.
                EditText txturl = (EditText) findViewById(R.id.opt_txt_url);
                txturl.setText(barcodeText);
                Toast toast = Toast.makeText(context, R.string.opt_barcode_ok, Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                // No barcode data was found. Bizarre, but the user should know.
                Toast toast = Toast.makeText(context, R.string.opt_barcode_fail, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else if(resultCode != RESULT_CANCELED) {
            // If the user didn't back out of the app and we've arrived here, something's gone wrong.
            Toast toast = Toast.makeText(context, R.string.opt_barcode_fail, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
