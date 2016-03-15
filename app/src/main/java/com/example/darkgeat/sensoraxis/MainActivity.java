package com.example.darkgeat.sensoraxis;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener,SurfaceHolder.Callback{

    private static int rotacion = 0;
    private TextView azimuth,pitch,roll, inclination;
    private SensorManager manager;
    private Sensor accelerometer, magnometer;
    private SatelliteSurface surface;
    private float[] Gravity,Geomagnetic;
    private Camera myCamera;
    private boolean previewRunning = false;
    private SurfaceHolder surfaceHolder;
    private Camera.CameraInfo info = new Camera.CameraInfo();
    private Camera.Parameters parameters;
    private int cameraId = 0;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        azimuth = (TextView) findViewById(R.id.azimuth);
        pitch = (TextView) findViewById(R.id.pitch);
        roll = (TextView) findViewById(R.id.roll);
        inclination = (TextView) findViewById(R.id.inclination);
        surface = (SatelliteSurface) findViewById(R.id.surfaceAxis);

        surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        surface.setCenter(45f,35f);
    }

    private boolean safeCameraOpen(int id){
        boolean opened = false;
        try{
            releaseCameraAndPreview();
            myCamera = Camera.open(id);
            opened = (myCamera != null);
        }catch (Exception e){
            Log.e(getString(R.string.app_name), "Failed to open Camera");
            e.printStackTrace();
        }
        return opened;
    }

    private void releaseCameraAndPreview(){

    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(this, magnometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            Gravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            Geomagnetic = event.values;
        }
        if (Gravity != null && Geomagnetic != null){
            float R[] = new float[9];
            float I[] = new float[9];
            final float rad2deg = (float)(180.0f/Math.PI);
            boolean success = SensorManager.getRotationMatrix(R, I, Gravity, Geomagnetic);
            if (success){
                count++;
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float elevation = SensorManager.getInclination(I);
                float azimuthValue = degreesto360(orientation[0]);
                float elevationValue = degreesElevation(orientation[2]);
                azimuth.setText(getString(com.example.darkgeat.sensoraxis.R.string.azimuth, azimuthValue));
                pitch.setText(getString(com.example.darkgeat.sensoraxis.R.string.pitch, orientation[1]*rad2deg));
                roll.setText(getString(com.example.darkgeat.sensoraxis.R.string.roll, elevationValue));
                inclination.setText(getString(com.example.darkgeat.sensoraxis.R.string.elevation, elevation));
                if (count >= 3) {
                    surface.UpdateLines(azimuthValue, elevationValue);
                    count = 0;
                }
            }
        }
    }

    public float degreesto360(float value){
        final float rad2deg = (float)(180.0f/Math.PI);
        float regreso = value * rad2deg;
        if (regreso < 0){
            regreso = 360 + regreso;
        }
        return regreso;
    }

    public float degreesElevation(float value){
        final float rad2deg = (float)(180.0f/Math.PI);
        float elevationValue = value * rad2deg * (-1) - 90;
        elevationValue = elevationValue >= 90 ? 90f : elevationValue;
        elevationValue = elevationValue <= -90 ? -90f : elevationValue;
        return elevationValue;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            myCamera = Camera.open(cameraId);
            parameters = myCamera.getParameters();
            myCamera.setPreviewDisplay(holder);
            setCameraDisplayOrientation(this, cameraId, myCamera);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            if(previewRunning){
                myCamera.stopPreview();
                previewRunning = false;
            }
            ObtenerParametros();
            myCamera.setPreviewDisplay(holder);
            myCamera.startPreview();
            previewRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        myCamera.stopPreview();
        myCamera.release();
    }

    public void ObtenerParametros(){
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(sizes, this.getResources().getDisplayMetrics().widthPixels, this.getResources().getDisplayMetrics().heightPixels);
        parameters.setPreviewSize(optimalSize.width,optimalSize.height);
        myCamera.setParameters(parameters);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w/h;

        if (sizes==null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info =  new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensa el efecto espejo
        } else {  // camara de atras
            result = (info.orientation - degrees + 360) % 360;
        }
        rotacion = rotation;
        camera.setDisplayOrientation(result);
    }
}
