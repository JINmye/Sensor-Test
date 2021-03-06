package mj.project.acc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    public GroundView ground;
    private Bitmap tree2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(),"오른쪽에 부딪치면 깨져요!", Toast.LENGTH_SHORT).show();
        // References service
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Configure screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.
        // No system buttons and title on screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
        ground = new GroundView(this);
        setContentView(ground);
        tree2 = BitmapFactory.decodeResource(getResources(), R.drawable.tree2);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        /*
            values[0]: acceleration in axis X
            values[1]: acceleration in axis Y
            values[2]: acceleration in axis Z
         */
        ground.updateMe(event.values[1], event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register listener
        // Frequency SENSOR_DELAY_GAME, second biggest. Try also SENSOR_DELAY_FASTEST, SENSOR_DELAY_NORMAL
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // delete listener
        mSensorManager.unregisterListener(this);
    }

    public class GroundView extends SurfaceView implements SurfaceHolder.Callback{

        /* Ball paremeters */
        private float cx = 10;
        private float cy = 10;
        // Last increment of position
        private float lastGX = 0;
        private float lastGY = 0;
        // Ball graphic size
        private  int pictureHeight = 0;
        private  int pictureWidht = 0;
        private Bitmap icon = null;

        // Screen size
        private int width;
        private int height;
        // touch border?
        private boolean noBorderX = false;
        private boolean noBorderY = false;
        private Vibrator vibratorService = null;
        private DrawThread thread;

        private Timer timer;
        public GroundView(Context context) {
            super(context);
            // Assign himself like callback of SurfView object
            getHolder().addCallback(this);
            // new thread, just created, no run yet
            thread = new DrawThread(getHolder(), this);
            // Get references and sizes to draw
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            width = display.getWidth();
            height = display.getHeight();
            icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            pictureHeight = icon.getHeight();
            pictureWidht = icon.getWidth();
            vibratorService = (Vibrator)(getSystemService(Service.VIBRATOR_SERVICE));
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // When area of SurfaceView are ready thread will draw on this
            thread.setRunning(true);
            thread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        @Override
        protected void onDraw(Canvas canvas) {
            if(canvas != null){
                //canvas.drawBitmap(tree2,500,500,null);
                canvas.drawColor(0xFFAAABBB);
                canvas.drawBitmap(icon, cx, cy, null);
            }
        }

        public void updateMe(float inx, float iny) {
            // Update acceleration
            lastGX += inx;
            lastGY += iny;
            // Update position
            cx += (lastGX);
            cy += (lastGY);
            // Check borders
            // If smash with borders vibrate and set acceleration = 0
            // x axis
            if(cx > (width -pictureWidht)){
                cx = width - pictureWidht;
                lastGX = 0;
                if (noBorderX){
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.r4);
                    vibratorService.vibrate(100);
                    noBorderX = false;
                    if(cx>=30){
                        if(noBorderX != false)
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                        Toast.makeText(getApplicationContext(),"짝짝짝! 부화!", Toast.LENGTH_SHORT).show();
                        Log.d("cx", cx+"");
                    }
                }
            }
            else if (cx < 0){
                cx = 0;
                lastGX = 0;
                if (noBorderX) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.r4);
                    vibratorService.vibrate(100);
                    noBorderX = false;
                    Toast.makeText(getApplicationContext(), "다시 계란!", Toast.LENGTH_SHORT).show();
                }
                icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            }else{
                noBorderX = true;
            }
            // y axis
            if(cy > (height -pictureHeight)){
                cy = height - pictureHeight;
                lastGY = 0;
                if (noBorderY){
                    vibratorService.vibrate(100);
                    noBorderY = false;
                }
            }
            else if (cy < 0){
                cy = 0;
                lastGY = 0;
                if (noBorderY){
                    vibratorService.vibrate(100);
                    noBorderY = false;
                }
            }else{
                noBorderY = true;
            }
            invalidate();
        }
    }

    class DrawThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private  GroundView panel;
        private boolean run = false;

        public DrawThread(SurfaceHolder surfaceHolder, GroundView panel){
            this.surfaceHolder = surfaceHolder;
            this.panel = panel;
        }

        public void setRunning(boolean run){
            this.run = run;
        }

        @SuppressLint("WrongCall")
        @Override
        public void run() {
            Canvas canvas;
            while (run){
                canvas = null;
                try{
                    canvas = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder){
                        panel.onDraw(canvas);
                    }
                }finally {
                    if(canvas != null){
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
    private class MyTimer extends TimerTask{
        @Override
        public void run(){

        }
    }
}
