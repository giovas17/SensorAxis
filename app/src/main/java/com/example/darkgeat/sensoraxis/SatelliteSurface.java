package com.example.darkgeat.sensoraxis;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;

/**
 * Created by DarkGeat on 1/26/2016.
 */
public class SatelliteSurface extends SurfaceView {

    private Context context;
    private int width = 400;
    private int height = 800;
    private float centerX = 0;
    private float centerY = 0;
    private float displacementX = 0, displacementY = 0;
    private float initX,endX;
    private float initY,endY;
    private float x = 0,y = 0;
    Paint brushRed = new Paint();

    public SatelliteSurface(Context context) {
        super(context);
        this.context = context;
        brushRed.setAntiAlias(true);
        brushRed.setStrokeWidth(2f);
        brushRed.setColor(Color.RED);
        brushRed.setStyle(Paint.Style.STROKE);
        brushRed.setStrokeJoin(Paint.Join.ROUND);
    }

    public SatelliteSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        brushRed.setAntiAlias(true);
        brushRed.setStrokeWidth(2f);
        brushRed.setColor(Color.RED);
        brushRed.setStyle(Paint.Style.STROKE);
        brushRed.setStrokeJoin(Paint.Join.ROUND);
    }

    public SatelliteSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        brushRed.setAntiAlias(true);
        brushRed.setStrokeWidth(2f);
        brushRed.setColor(Color.RED);
        brushRed.setStyle(Paint.Style.STROKE);
        brushRed.setStrokeJoin(Paint.Join.ROUND);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SatelliteSurface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        brushRed.setAntiAlias(true);
        brushRed.setStrokeWidth(2f);
        brushRed.setColor(Color.RED);
        brushRed.setStyle(Paint.Style.STROKE);
        brushRed.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight,myWidth;
        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        /*if(metrics.densityDpi >= DisplayMetrics.DENSITY_XXHIGH){
            width = 300;
            height = 300;
            textSize = 24f;
        }*/
        if(hSpecMode == MeasureSpec.EXACTLY){
            height = hSpecSize;
            endY = height;
        }
        myHeight = height;
        if(wSpecMode == MeasureSpec.EXACTLY){
            width = wSpecSize;
        }
        myWidth = width;
        setMeasuredDimension(myWidth, myHeight);
    }

    public void setCenter(float newX, float newY){
        centerX = newX;
        centerY = newY;
        displacementX = centerX - 180;
        displacementY = newY;
        Log.e("Traking", "desplazamiento en x: " + displacementX + ", desplazamiento en y: " + displacementY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(x, 0, x, (float) height, brushRed);
        canvas.drawLine(0, y, (float)width, y, brushRed);
        canvas.drawCircle((float)width/2,(float) height/2,12f,brushRed);
        super.onDraw(canvas);
    }

    public void UpdateLines(float x, float y){
        this.x = normalizeToDegrees(x, Coordinate.X);
        float pathDisplacementX, pathDisplacementY,truthPath;
        pathDisplacementY = normalizeToDegrees(Math.abs(y),Coordinate.Y);
        this.y = pathDisplacementY;
        if (displacementY >= 0) {
            if (y >= 0) {
                this.y = (height / 2) + normalizeToDegrees(displacementY,Coordinate.Y) - pathDisplacementY;
            } else {
                this.y = y < (-90 + displacementY) ? normalizeToDegrees(displacementY,Coordinate.Y) - pathDisplacementY : (height / 2) + normalizeToDegrees(displacementY,Coordinate.Y) + pathDisplacementY;
            }
        }
        if (displacementX <= 0){ //The center is smaller than 180
            pathDisplacementX = normalizeToDegrees(360 + displacementX,Coordinate.X);
            truthPath = width - pathDisplacementX;
            this.x = x > (360 + displacementX) ? normalizeToDegrees(x-(360+displacementX),Coordinate.X) : this.x + truthPath;
        }else {
            pathDisplacementX = normalizeToDegrees(360 - displacementX,Coordinate.X);
            truthPath = width - pathDisplacementX;
            this.x = x <= displacementX ? this.x + pathDisplacementX : this.x - truthPath;
        }
        invalidate();
    }

    private float normalizeToDegrees(float value, Coordinate isX) {
        return isX == Coordinate.X ? (value * width) / 360 : (value * height) / 180;
    }

    public enum Coordinate{
        X,Y
    }
}
