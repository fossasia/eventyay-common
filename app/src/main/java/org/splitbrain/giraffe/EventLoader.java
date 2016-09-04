package org.splitbrain.giraffe;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.widget.Toast;

import org.splitbrain.simpleical.SimpleIcalEvent;
import org.splitbrain.simpleical.SimpleIcalParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class EventLoader extends AsyncTask<URL, String, String> {
    private final OptionsActivity context;
    private DBAdapter db = null;
    private boolean ignoreSSLCerts = false;

    public EventLoader(OptionsActivity context) {
        this.context = context;
    }


    /**
     * @param ignoreSSLCerts the ignoreSSLCerts to set
     */
    public void setIgnoreSSLCerts(boolean ignoreSSLCerts) {
        this.ignoreSSLCerts = ignoreSSLCerts;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onCancelled() {
        if (db != null) db.close();
        Toast toast = Toast.makeText(context, "Loading cancelled", Toast.LENGTH_LONG);
        toast.show();
        context.resetLayout();
    }

    @Override
    protected void onPostExecute(String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
        context.resetLayout();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        context.writeProgress(values[0]);
    }


    @Override
    protected String doInBackground(URL... urls) {
        int count = 0;

        publishProgress("Opening database...");
        db = new DBAdapter(context);
        db.open();
        db.begin();

        // http://re-publica.de/11/rp2011.ics
        try {
            publishProgress("Connecting to iCal URL...");

            // connect without SSL verification
            HttpURLConnection http;
            if (urls[0].getProtocol().toLowerCase().equals("https")) {
                if (this.ignoreSSLCerts) trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) urls[0].openConnection();
                if (this.ignoreSSLCerts) https.setHostnameVerifier(DO_NOT_VERIFY);
                http = https;
            } else {
                http = (HttpURLConnection) urls[0].openConnection();
            }
            InputStream inputStream = http.getInputStream();


            publishProgress("Clearing database...");
            db.deleteEvents();

            // abort if cancelled
            if (isCancelled()) {
                db.rollback();
                db.close();
                return "Cancelled";
            }

            //AssetManager assetManager = context.getAssets();
            //InputStream inputStream = assetManager.open("rp2011.ics");

            SimpleIcalParser ical = new SimpleIcalParser(inputStream);
            SimpleIcalEvent event;
            while ((event = ical.nextEvent()) != null) {
                // build record
                EventRecord record = getEventRecord(event);
                if (record == null) continue;

                // add to database:
                db.addEventRecord(record);
                count++;

                // progress feedback
                publishProgress("Loaded " + count + " events...");

                // abort if cancelled
                if (isCancelled()) {
                    db.rollback();
                    db.close();
                    return "Cancelled";
                }
            }
            db.commit();
        } catch (Exception e) {
            db.rollback();
            db.close();
            return "Failed to read from " + e.toString();
        }
        db.close();
        return "Sucessfully loaded " + count + " entries.";
    }


    private EventRecord getEventRecord(SimpleIcalEvent event) {
        // mandatory fields
        String uid = event.get("UID");
        String summary = event.get("SUMMARY");
        Date dateStart = event.getStartDate();
        if (summary == null) return null;
        if (dateStart == null) return null;

        // fake UID
        if (uid == null) {
            uid = summary + dateStart.getTime();
        }

        // optional fields
        Date dateEnd = event.getEndDate();
        String location = event.get("LOCATION");
        String organizer = event.get("ORGANIZER");
        String url = event.get("URL");
        String description = event.get("DESCRIPTION");

        // create event
        EventRecord record = new EventRecord();
        record.id = uid;
        record.title = summary;
        record.starts = dateStart.getTime() / 1000;
        if (dateEnd != null) record.ends = dateEnd.getTime() / 1000;
        if (location != null) record.location = location;
        if (organizer != null) record.speaker = organizer;
        if (url != null) record.url = url;
        if (description != null) record.description = description;

        return record;
    }


    /**
     * always verify the host - don't check for certificate
     *
     * @link http://stackoverflow.com/a/9133562/172068
     */
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @SuppressLint("BadHostnameVerifier")
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Trust every server - don't check for any certificate
     *
     * @link http://stackoverflow.com/a/9133562/172068
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @SuppressLint("TrustAllX509TrustManager")
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
            }

            @SuppressLint("TrustAllX509TrustManager")
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
