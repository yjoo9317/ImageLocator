package com.youngjoo.imagelocator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.IOException;
import java.util.List;

/**
 * Created by yjoo9_000 on 2017-11-19.
 */

public class LocatorFragment extends SupportMapFragment {
    private static final String TAG = "LocatorFragment";

    private ImageView mImageView;
    private GoogleApiClient mClient;
    private Bitmap mMapImage;
    private GalleryItem mMapItem;
    private Location mCurrentLocation;

    public static LocatorFragment newInstance(){
        return new LocatorFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
    }


    @Override
    public void onStart(){
        super.onStart();

        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }

    @Override
    public void onStop(){
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_locator, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_locate);
        searchMenuItem.setEnabled(mClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate :
                findImage();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }




    private void findImage(){
        Log.i(TAG, "findImage called..");
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getActivity());
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted..");
            client.requestLocationUpdates(request, mLocationCallback, Looper.myLooper());
        }
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult result){
            for(Location location : result.getLocations()){
                Log.i(TAG, "Received Location : "+location);
                new SearchTask().execute(location);
            }
        }
    };

    private class SearchTask extends AsyncTask<Location, Void, String> {
        private GalleryItem mGalleryItem;
        private Bitmap mBitmap;
        private Location mLocation;

        @Override
        protected  String doInBackground(Location...params){
            FlickerFetcher fetcher = new FlickerFetcher();
            mLocation = params[0];
            List<GalleryItem> items = fetcher.searchPhotos(params[0]);

            if(items.size() == 0){
                Log.i(TAG, "Nearby Photo Not found.");
                return null;
            }
            mGalleryItem = items.get(0);
            try{
                byte[] bytes = fetcher.getUrlBytes(mGalleryItem.getUrl());
                mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Log.i(TAG, "bitmap created: "+bytes.length);
            }catch(IOException e){
                Log.i(TAG, "Unable to download bitmap at "+mGalleryItem.getUrl());
            }
            return mGalleryItem.getUrl();
        }

        @Override
        protected void onPostExecute(String url){

           /* Picasso.with(getActivity())
                    .load(url)
                    .into(mImageView);*/
            /*
            if(mBitmap != null) {
                mImageView.setImageBitmap(mBitmap);
                Log.i(TAG, "setting bitmap resource");
            } else {
                Log.i(TAG, "Not valid bitmap");
            }
            */
        }
    }

}
