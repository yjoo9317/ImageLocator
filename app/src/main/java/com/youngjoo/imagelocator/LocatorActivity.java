package com.youngjoo.imagelocator;

import android.app.Dialog;
import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class LocatorActivity extends SingleFragmentActivity {
    private static final String TAG = "LocatorActivity";

    private static final int REQUEST_ERROR = 0;


    @Override
    protected Fragment createFragment(){
        return LocatorFragment.newInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_locator);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
    }

    @Override
    protected void onResume(){
        super.onResume();
        GoogleApiAvailability service = GoogleApiAvailability.getInstance();
        int errorCode = service.isGooglePlayServicesAvailable(this);
        if(errorCode != ConnectionResult.SUCCESS){
            Dialog errorDialog = service.getErrorDialog(this, errorCode, REQUEST_ERROR, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finish();
                }
            });
            errorDialog.show();
        } else {
            Log.i(TAG, "Connected to Google Sevice");
        }
    }

}
