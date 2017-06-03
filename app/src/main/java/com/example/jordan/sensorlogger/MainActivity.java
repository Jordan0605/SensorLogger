package com.example.jordan.sensorlogger;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;

import static android.location.LocationManager.GPS_PROVIDER;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_FINE_LOCATION = 0x01 << 1;
    public static final int REQUEST_COARSE_LOCATION = 0x01 << 2;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    DecimalFormat df = new DecimalFormat("#.####");

    private TextView gps;
    private TextView Mx;
    private TextView My;
    private TextView Mz;
    private TextView Gx;
    private TextView Gy;
    private TextView Gz;
    private TextView Ax;
    private TextView Ay;
    private TextView Az;
    private Button btn;

    private Sensor[] SensorList = new Sensor[3];
    private LocationManager mLocationManager;
    private SensorManager mSensorManager;
    private boolean flag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        turnGPSOn();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        mLocationManager.getBestProvider(criteria, true);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);criteria.setAccuracy(Criteria.ACCURACY_FINE);
        flag = false;
        sharedPreferences = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        gps = (TextView) findViewById(R.id.GPSlog);
        Mx = (TextView) findViewById(R.id.Mxlog);
        My = (TextView) findViewById(R.id.Mylog);
        Mz = (TextView) findViewById(R.id.Mzlog);
        Ax = (TextView) findViewById(R.id.Axlog);
        Ay = (TextView) findViewById(R.id.Aylog);
        Az = (TextView) findViewById(R.id.Azlog);
        Gx = (TextView) findViewById(R.id.Gxlog);
        Gy = (TextView) findViewById(R.id.Gylog);
        Gz = (TextView) findViewById(R.id.Gzlog);
        btn = (Button) findViewById(R.id.button);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            verifyFineLocationPermissions(this);
            verifyCoaseLocationPermissions(this);
        }


        /*if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }*/

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag == false) {
                    btn.setText("Stop");
                    mLocationManager.requestLocationUpdates(GPS_PROVIDER, 1000, 0, locationlistener); // 讓locationlistener處理資料有變化時的事情
                    SensorList[0] = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    mSensorManager.registerListener(mysensorListener, SensorList[0], SensorManager.SENSOR_DELAY_FASTEST);
                    SensorList[1] = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                    mSensorManager.registerListener(mysensorListener, SensorList[1], SensorManager.SENSOR_DELAY_FASTEST);
                    SensorList[2] = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                    mSensorManager.registerListener(mysensorListener, SensorList[2], SensorManager.SENSOR_DELAY_FASTEST);
                    flag = true;
                }
                else if(flag == true){
                    btn.setText("Start");
                    mLocationManager.removeUpdates(locationlistener);
                    mSensorManager.unregisterListener(mysensorListener);
                    flag = false;

                }
            }
        });



    }

    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            Log.e("Tag", "gps off");
            final AlertDialog.Builder builder =  new AlertDialog.Builder(this);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            final String message = "GPS未開啟！！！";

            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();
        }
    }

    private final LocationListener locationlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            gps.setText(df.format(lat) + "," + df.format(lng));
        }


        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

    };

    private final SensorEventListener mysensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            String[] data = new String [3];
            int idx = 0;
            for(float val : event.values){
                data[idx] = String.valueOf(val);
                idx++;
            }
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    Ax.setText(data[0]);
                    Ay.setText(data[1]);
                    Az.setText(data[2]);
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    Gx.setText(data[0]);
                    Gy.setText(data[1]);
                    Gz.setText(data[2]);
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    Mx.setText(data[0]);
                    My.setText(data[1]);
                    Mz.setText(data[2]);
                    break;

                default:
                    Log.d("Tag", "unexcepted sensor type");

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public static void verifyFineLocationPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }
    }

    public static void verifyCoaseLocationPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "coarse location permission granted");
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since coarse location access has not been granted, this app will not be able to discover beacons when in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }
                } else {
                    Log.w(TAG, "no permission granted!!");
                }
                break;
            case REQUEST_FINE_LOCATION:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "coarse location permission granted");
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since fine location access has not been granted, this app will not be able to discover beacons when in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }
                } else {
                    Log.w(TAG, "no permission granted!!");
                }
                break;
        }
    }
}
