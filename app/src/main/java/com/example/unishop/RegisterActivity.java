package com.example.unishop;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements LocationListener {
    Spinner spinloc;
    EditText edMatricno,edPhone,edName,edEmail,edPass;
    Button btnReg;
    ImageButton btnmapwindown;
    TextView tvlogin, tvlocation;
    ImageView imgprofile;
    User user;
    Dialog myDialogMap;
    private GoogleMap mMap;
    String slatitude, slongitude,longitude,latitude;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUserInput();
            }
        });
        tvlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        imgprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogTakePicture();
            }
        });
        btnmapwindown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMapWindow();
            }
        });
    }

    private void dialogTakePicture() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(this.getResources().getString(R.string.dialogtakepicture));

        alertDialogBuilder
                .setMessage(this.getResources().getString(R.string.dialogtakepicturea))
                .setCancelable(false)
                .setPositiveButton(this.getResources().getString(R.string.yesbutton),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, 1);
                        }
                    }
                })
                .setNegativeButton(this.getResources().getString(R.string.nobutton),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap,400,500);
            imgprofile.setImageBitmap(imageBitmap);
            imgprofile.buildDrawingCache();
            ContextWrapper cw = new ContextWrapper(this);
            File pictureFileDir = cw.getDir("basic", Context.MODE_PRIVATE);
            if (!pictureFileDir.exists()) {
                pictureFileDir.mkdir();
            }
            Log.e("FILE NAME", "" + pictureFileDir.toString());
            if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
                return;
            }
            FileOutputStream outStream = null;
            String photoFile = "profile.jpg";
            File outFile = new File(pictureFileDir, photoFile);
            try {
                outStream = new FileOutputStream(outFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
                //hasimage = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    //get information filled
    private void registerUserInput() {
        String matricno,phone,username,email,password,location;
        matricno = edMatricno.getText().toString();
        phone = edPhone.getText().toString();
        username = edName.getText().toString();
        email = edEmail.getText().toString();
        password = edPass.getText().toString();
        location = spinloc.getSelectedItem().toString();
        //error checking here
        user = new User(matricno,phone,username,email,password,location,slatitude,slongitude);
        //insertData(user);
        registerUserDialog();
    }

    //insert new user into db
    private void insertData() {
        class RegisterUser extends AsyncTask<Void,Void,String>{

            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(RegisterActivity.this,
                        "Registration","...",false,false);
            }

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("matricno",user.matricno);
                hashMap.put("phone",user.phone);
                hashMap.put("username",user.username);
                hashMap.put("email",user.email);
                hashMap.put("password",user.password);
                hashMap.put("location",user.location);
                hashMap.put("latitude",user.latitude);
                hashMap.put("longitude",user.longitude);
                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest
                        ("http://www.simplehlife.com/uniShop/php/newregister.php",hashMap);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if (s.equalsIgnoreCase("success")){
                    //Toast.makeText(RegisterActivity.this, "Registration Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                    RegisterActivity.this.finish();
                    startActivity(intent);

                }else if (s.equalsIgnoreCase("nodata")){
                    Toast.makeText(RegisterActivity.this, "Please fill in data first", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                }

            }
        }

        RegisterUser registerUser = new RegisterUser();
        registerUser.execute();
    }


    private void registerUserDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(this.getResources().getString(R.string.registerfor)+" "+user.username+"?");

        alertDialogBuilder
                .setMessage(this.getResources().getString(R.string.registerdialognew))
                .setCancelable(false)
                .setPositiveButton(this.getResources().getString(R.string.yesbutton),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        //Toast.makeText(getActivity(), "DELETE "+jobid, Toast.LENGTH_SHORT).show();
                        new Encode_image().execute(getDir(),user.phone+".jpg");
                        Toast.makeText(RegisterActivity.this, getResources().getString(R.string.registrationprocess), Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton(this.getResources().getString(R.string.nobutton),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public String getDir(){
        ContextWrapper cw = new ContextWrapper(this);
        File pictureFileDir = cw.getDir("basic", Context.MODE_PRIVATE);
        if (!pictureFileDir.exists()) {
            pictureFileDir.mkdir();
        }
        Log.d("GETDIR",pictureFileDir.getAbsolutePath());
        return pictureFileDir.getAbsolutePath()+"/profile.jpg";
    }

    private void initView() {
        spinloc = findViewById(R.id.spinLoc);
        edMatricno = findViewById(R.id.txtmatricno);
        edPhone =findViewById(R.id.txtphone);
        edName = findViewById(R.id.txtname);
        edEmail = findViewById(R.id.txtEmail);
        edPass = findViewById(R.id.txtpassword);
        btnReg = findViewById(R.id.btn_register);
        tvlogin = findViewById(R.id.tvregister);
        imgprofile = findViewById(R.id.imageView);
        tvlocation  = findViewById(R.id.textLoc);
        btnmapwindown = findViewById(R.id.btnMap);
    }


    //image/picture
    public class Encode_image extends AsyncTask<String,String,Void> {
        private String encoded_string, image_name;
        Bitmap bitmap;

        @Override
        protected Void doInBackground(String... args) {
            String filname = args[0];
            image_name = args[1];
            bitmap = BitmapFactory.decodeFile(filname);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] array = stream.toByteArray();
            encoded_string = Base64.encodeToString(array, 0);
            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            makeRequest(encoded_string, image_name);
        }

        private void makeRequest(final String encoded_string, final String image_name) {
            class UploadAll extends AsyncTask<Void, Void, String> {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected String doInBackground(Void... params) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("encoded_string", encoded_string);
                    map.put("image_name", image_name);
                    RequestHandler rh = new RequestHandler();//request server connection
                    String s = rh.sendPostRequest("http://www.simplehlife.com/uniShop/php/upload_image.php", map);

                    return s;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (s.equalsIgnoreCase("Success")) {
                        //Toast.makeText(RegisterActivity.this, "Success Upload Image", Toast.LENGTH_SHORT).show();
                        insertData();
                    }else{
                        Toast.makeText(RegisterActivity.this, "Failed Registration ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            UploadAll uploadall = new UploadAll();
            uploadall.execute();
        }
    }

    //google map
    private void loadMapWindow() {
        myDialogMap = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);//Theme_DeviceDefault_Dialog_NoActionBar
        myDialogMap.setContentView(R.layout.map_window);
        myDialogMap.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Button btnsavemap = myDialogMap.findViewById(R.id.btnclosemap);
        MapView mMapView = myDialogMap.findViewById(R.id.mapView);
        MapsInitializer.initialize(this);
        mMapView.onCreate(myDialogMap.onSaveInstanceState());
        mMapView.onResume();
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                LatLng allpos;
                LatLng posisiabsen = new LatLng(6.413395, 100.426868);  ////your lat lng
                googleMap.addMarker(new MarkerOptions().position(posisiabsen).title("HOME").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))).showInfoWindow();
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(posisiabsen));
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
                if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        googleMap.clear();
                        slatitude = String.valueOf(latLng.latitude);
                        slongitude = String.valueOf(latLng.longitude);
                        googleMap.addMarker(new MarkerOptions().position(latLng).title("New Home").icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))).showInfoWindow();
                    }
                });
            }
        });
        btnsavemap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slatitude.length()>5){
                    latitude = slatitude;
                    longitude = slongitude;
                    myDialogMap.dismiss();
                    tvlocation.setText("https://www.google.com/maps/@"+latitude+","+longitude+",15z");
                    locationManager.removeUpdates(RegisterActivity.this);
                }else{
                    Toast.makeText(RegisterActivity.this, "Please select home location", Toast.LENGTH_SHORT).show();
                }

            }
        });
        myDialogMap.show();
        enableLocation();
    }


    private void enableLocation() {
        if (!checkLocationPermission()) {
            //Toast.makeText(this, "PLEASE ALLOW PERMISSION FOR APP TO ACCESS GPS", Toast.LENGTH_SHORT).show();
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkPermission()) {
            //linkloc.setText(getResources().getString(R.string.waithomeloc));
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
        } else {
            //requestPermission();
        }
        if (!checkLocationPermission()) {
            //Toast.makeText(this, "PLEASE ALLOW PERMISSION FOR APP TO ACCESS GPS", Toast.LENGTH_SHORT).show();
            //startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    //check permission
    public boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            99);

                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            99);
                }
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (result == PackageManager.PERMISSION_GRANTED) {
                //LocationAvailable = true;
                return true;
            } else {
                //LocationAvailable = false;
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 999:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPermission()) {
                        if (locationManager!=null){
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
                        }

                    } else {
                        //requestPermission();
                    }
                } else {
                }
                break;
        }
    }

    //change location
    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}