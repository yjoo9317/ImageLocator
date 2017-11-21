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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yjoo9_000 on 2017-11-19.
 */

public class LocatorFragment extends SupportMapFragment {
    private static final String TAG = "LocatorFragment";
    private static final int MAX_COUNT = 20;

    private ImageView mImageView;
    private GoogleApiClient mClient;
    private Bitmap mMapImage;
    private GalleryItem mMapItem;
    private static Location mCurrentLocation;
    private static GoogleMap mGoogleMap;
    private static List<Bitmap> mNearByPhotos;
    private static List<GalleryItem> mGalleryItems;

    public static LocatorFragment newInstance(){
        return new LocatorFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        mNearByPhotos = new ArrayList<>();
        mGalleryItems = new ArrayList<>();
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

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                Log.i(TAG, "GoogleMap object obtained..");
                updateUI();
            }
        });
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

    private void updateUI() {
        Log.i(TAG, "Update Map UI.");
        if(mGoogleMap == null || mGalleryItems.size() == 0 ){//|| mMapImage == null){
            Log.e(TAG, "google map or nearby photos not valid");
            return;
        }
        placeMarkers();
/*
        LatLng itemPoint = new LatLng(mMapItem.getLat(), mMapItem.getLon());
        LatLng myPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        BitmapDescriptor photo = BitmapDescriptorFactory.fromBitmap(mMapImage);
        MarkerOptions photoMarker = new MarkerOptions()
                .position(itemPoint)
                .icon(photo);

        MarkerOptions myLocationMarker = new MarkerOptions()
                .position(myPoint);

        mGoogleMap.addMarker(photoMarker);
        mGoogleMap.addMarker(myLocationMarker);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(itemPoint)
                .include(myPoint)
                .build();
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mGoogleMap.animateCamera(update);*/
    }

    private void placeMarkers(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i < MAX_COUNT; i++){
            GalleryItem item = mGalleryItems.get(i);
            BitmapDescriptor photo = BitmapDescriptorFactory.fromBitmap(mNearByPhotos.get(i));
            LatLng position = new LatLng(item.getLat(), item.getLon());
            MarkerOptions marker = new MarkerOptions()
                    .position(position)
                    .icon(photo);
            mGoogleMap.addMarker(marker);
            builder.include(position);
        }
        LatLng myPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        builder.include(myPosition);

        MarkerOptions myMarker = new MarkerOptions()
                .position(myPosition);
        mGoogleMap.addMarker(myMarker);

        LatLngBounds bounds = builder.build();
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mGoogleMap.animateCamera(update);
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





    private class SearchTask extends AsyncTask<Location, Void, Void> {
        private GalleryItem mGalleryItem;
        private Bitmap mBitmap;
        private Location mLocation;

        @Override
        protected Void doInBackground(Location...params){
            FlickerFetcher fetcher = new FlickerFetcher();
            mLocation = params[0];
            mGalleryItems.clear();
            mGalleryItems.addAll(fetcher.searchPhotos(params[0]));
            //List<GalleryItem> items = fetcher.searchPhotos(params[0]);

            if(mGalleryItems.size() == 0){
                Log.i(TAG, "Nearby Photo Not found.");
                return null;
            }
            //mGalleryItem = items.get(0);
            mNearByPhotos.clear();
            Log.i(TAG, "downloading nearby photos.");
            for(int i = 0; i < MAX_COUNT; i++){
                GalleryItem item = mGalleryItems.get(i);
                try {
                    byte[] bytes = fetcher.getUrlBytes(item.getUrl());
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    mNearByPhotos.add(bitmap);
                }catch(IOException e){
                    Log.e(TAG, "Failed to download image");
                }

            }
            Log.i(TAG, "finish downloading");
            /*
            try{
                byte[] bytes = fetcher.getUrlBytes(mGalleryItem.getUrl());
                mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Log.i(TAG, "bitmap created: "+bytes.length);
            }catch(IOException e){
                Log.i(TAG, "Unable to download bitmap at "+mGalleryItem.getUrl());
            }*/
            return null;
        }

        @Override
        protected void onPostExecute(Void result){

           /* Picasso.with(getActivity())
                    .load(url)
                    .into(mImageView);*/
            /*
            if(mBitmap != null) {
                mImageView.setImageBitmap(mBitmap);
                Log.i(TAG, "setting bitmap resource");
            } else {
                Log.i(TAG, "Not valid bitmap");
            }*/

            /*mMapImage = mBitmap;
            mMapItem = mGalleryItem;*/
            mCurrentLocation = mLocation;
            updateUI();
        }
    }

}
