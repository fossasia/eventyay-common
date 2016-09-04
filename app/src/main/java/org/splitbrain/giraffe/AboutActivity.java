package org.splitbrain.giraffe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        // display the version info
        TextView tv = (TextView) findViewById(R.id.version);
        String hash = getResources().getString(R.string.app_git_hash);
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;
            hash = String.format("v%s - %s (%d)", versionName, hash, versionNumber);
        } catch (PackageManager.NameNotFoundException ignored) {
            // this shouldn't happen
        }
        tv.setText(hash);
    }
}
