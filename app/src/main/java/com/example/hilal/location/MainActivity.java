package com.example.hilal.location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements DapatkanAlamatTask.onTaskSelesai{

    private Button mLocationButton;
    private TextView mLocationTextView;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;

    private boolean mTrackingLocation;

    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationButton = (Button) findViewById(R.id.button_location);
        mLocationTextView = (TextView) findViewById(R.id.textview_location);
        mAndroidImageView = (ImageView) findViewById(R.id.imageview_android);

        // Untuk animasi
        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTrackingLocation) {
                    mulaiTrackingLokasi();
                } else {
                    stopTrackingLokasi();
                }
            }
        });

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                //jika tracking aktif proses reserve geocode menjadi data alamat
                if (mTrackingLocation){
                    new DapatkanAlamatTask(MainActivity.this,
                            MainActivity.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };
    }

    private void mulaiTrackingLokasi(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // Log.d("GETPERMISI", "getLocation: permissions granted");
//            mFusedLocationClient.getLastLocation().addOnSuccessListener(
//                    new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            if (location != null){
////                                mLastLocation = location;
////                                mLocationTextView.setText(
////                                        getString(R.string.location_text,
////                                                mLastLocation.getLatitude(),
////                                                mLastLocation.getLongitude(),
////                                                mLastLocation.getTime()));
//
//                                // lakukan reverse geocode AsyncTask
//                                new DapatkanAlamatTask(MainActivity.this,
//                                        MainActivity.this).execute(location);
//                            } else {
//                                mLocationTextView.setText("Lokasi Ra Ono");
//                            }
//                        }
//                    }
//            );

            mFusedLocationClient.requestLocationUpdates
                    (getLocationRequest(), mLocationCallback, null);

            mLocationTextView.setText(getString(R.string.alamat_text,
                    "sedang mencari alamat",
                    System.currentTimeMillis()));
            mTrackingLocation = true;
            mLocationButton.setText("Stop Tracking Lokasi");
            mRotateAnim.start();
        }
    }

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
            // jika permissions diijinkan getLocation()
            // jika tidak tampilkan toast
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mulaiTrackingLokasi();
            } else {
                Toast.makeText(this, "Permission tak didapat", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    @Override
    public void onTaskCompleted(String result){
        if (mTrackingLocation) {
            // update UI dengan tampilan hasil alamat
            mLocationTextView.setText(getString(R.string.alamat_text,
                    result, System.currentTimeMillis()));
        }
    }

    private void stopTrackingLokasi(){
        if (mTrackingLocation){
            mTrackingLocation = false;
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            mLocationButton.setText("Mulai Tracking Lokasi");
            mLocationTextView.setText("Tracking Sedang Dihentikan");
            mRotateAnim.end();
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


}
