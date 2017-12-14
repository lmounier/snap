package ai.deepar.example;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import ai.deepar.ar.AREventListener;
import ai.deepar.ar.CameraGrabber;
import ai.deepar.ar.CameraGrabberListener;
import ai.deepar.ar.DeepAR;
import ai.deepar.example.ChronoM;

import static java.lang.Math.round;

public class MainActivity extends PermissionsActivity implements AREventListener, SurfaceHolder.Callback {

    private final String TAG = MainActivity.class.getSimpleName();

    private SurfaceView arView;
    private CameraGrabber cameraGrabber;
    private ImageButton screenshotBtn;
    private ImageButton switchCamera;
    private ImageButton nextMask;
    private ImageButton previousMask;
    private ImageButton shareButton;
    private ImageButton galleryButton;
    private ImageButton returnButton;
    private ImageButton effectButton;
    private ImageButton switchButton;
    private ImageButton recordButton;
    private ImageButton videoButton;

    private LinearLayout radioLayout;
    private RadioButton radioMasks;
    private RadioButton radioEffects;
    private RadioButton radioFilters;

    private final static String SLOT_MASKS = "masks";
    private final static String SLOT_EFFECTS = "effects";
    private final static String SLOT_FILTER = "filters";


    private String currentSlot = SLOT_MASKS;

    private int currentMask=0;
    private int currentEffect=0;
    private int currentFilter=0;
    private int time =0;

    ArrayList<AREffect> masks;
    ArrayList<AREffect> effects;
    ArrayList<AREffect> filters;

    private DeepAR deepAR;
    private String nameFile;
    private TextView chrono;
    Handler handler2;
    final Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            time++;
            handler2.postDelayed(this, 1000);
            String time2;
            if(time >= 60){
                int minute = round(time/60);
                int seconde = time%60;
                String seconde2 = "";
                if(seconde < 10){
                    seconde2 = "0" + seconde;
                } else{
                    seconde2 = seconde + "";
                }
                time2 = "0" + minute + ":" + seconde2;
            }else{
                String seconde;
                if(time < 10){
                    seconde = "0" + time;
                } else{
                    seconde = time + "";
                }
                time2 = "00:" + seconde;
            }
            chrono.setText(time2);
        }
    };

    Handler handler = new Handler();
    final  Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            stopRecording();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        deepAR = new DeepAR();
        deepAR.initialize(this, this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        String cameraPermission = getResources().getString(R.string.camera_permission);
        String externalStoragePermission = getResources().getString(R.string.external_permission);

        checkMultiplePermissions(
                Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO),
                cameraPermission + " " + externalStoragePermission,
                100,
                new PermissionsActivity.MultiplePermissionsCallback() {
            @Override
            public void onAllPermissionsGranted() {
                setContentView(R.layout.activity_main);
                setImage();
                setupViews();

            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                Log.d("MainActity", "Permissions Denied!");
            }
        });

    }

    @Override
    protected void onStop() {
        cameraGrabber.setFrameReceiver(null);
        cameraGrabber.stopPreview();
        cameraGrabber.releaseCamera();
        cameraGrabber = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deepAR.release();
    }

    private void setupEffects() {

        masks = new ArrayList<>();
        masks.add(new AREffect("none", AREffect.EffectTypeMask));
        masks.add(new AREffect("dark_vador", AREffect.EffectTypeMask));
        masks.add(new AREffect("casque_xwing", AREffect.EffectTypeMask));
        masks.add(new AREffect("grievoushead", AREffect.EffectTypeMask));
        masks.add(new AREffect("stormtroop", AREffect.EffectTypeMask));

        effects = new ArrayList<>();
        effects.add(new AREffect("none", AREffect.EffectTypeAction));
        effects.add(new AREffect("fire", AREffect.EffectTypeAction));
        effects.add(new AREffect("rain", AREffect.EffectTypeAction));
        effects.add(new AREffect("heart", AREffect.EffectTypeAction));
        effects.add(new AREffect("blizzard", AREffect.EffectTypeAction));

        filters = new ArrayList<>();
        filters.add(new AREffect("none", AREffect.EffectTypeFilter));
        filters.add(new AREffect("oilpaint", AREffect.EffectTypeFilter));
        filters.add(new AREffect("filmcolorperfection", AREffect.EffectTypeFilter));
        filters.add(new AREffect("tv80", AREffect.EffectTypeFilter));
        filters.add(new AREffect("drawingmanga", AREffect.EffectTypeFilter));
        filters.add(new AREffect("sepia", AREffect.EffectTypeFilter));
        filters.add(new AREffect("bleachbypass", AREffect.EffectTypeFilter));
        filters.add(new AREffect("sharpen", AREffect.EffectTypeFilter));
        filters.add(new AREffect("realvhs", AREffect.EffectTypeFilter));


    }

    private void radioButtonClicked() {
        if (radioMasks.isChecked()) {
            currentSlot = SLOT_MASKS;
        } else if (radioEffects.isChecked()) {
            currentSlot = SLOT_EFFECTS;
        } else if (radioFilters.isChecked()) {
            currentSlot = SLOT_FILTER;
        }
    }

    private ArrayList<AREffect> getActiveList() {
        if (currentSlot.equals(SLOT_MASKS)) {
            return masks;
        } else if (currentSlot.equals(SLOT_EFFECTS)) {
            return effects;
        } else {
            return filters;
        }
    }

    private int getActiveIndex() {
        if (currentSlot.equals(SLOT_MASKS)) {
            return currentMask;
        }
        else if (currentSlot.equals(SLOT_EFFECTS)) {
            return currentEffect;
        }
        else {
            return currentFilter;
        }
    }

    private void setActiveIndex(int index) {
        if (currentSlot.equals(SLOT_MASKS)) {
            currentMask = index;
        } else if (currentSlot.equals(SLOT_EFFECTS)) {
            currentEffect = index;
        } else {
            currentFilter = index;
        }
    }


    private void gotoNext() {
        ArrayList<AREffect> activeList = getActiveList();
        int index = getActiveIndex();
        index = index+1;
        if (index >= activeList.size()) {
            index = 0;
        }
        setActiveIndex(index);
        deepAR.switchEffect(currentSlot, activeList.get(index).getPath());
    }

    private void gotoPrevious() {
        ArrayList<AREffect> activeList = getActiveList();
        int index = getActiveIndex();
        index = index-1;
        if (index < 0) {
            index = activeList.size()-1;
        }
        setActiveIndex(index);
        deepAR.switchEffect(currentSlot, activeList.get(index).getPath());
    }

    boolean recording = false;

    private void setupViews() {
        final Context context = this;
        previousMask = (ImageButton)findViewById(R.id.previousMask);
        nextMask = (ImageButton)findViewById(R.id.nextMask);

        switchButton = (ImageButton)findViewById(R.id.switchCam);
        videoButton = (ImageButton)findViewById(R.id.videoButton);
        recordButton = (ImageButton)findViewById(R.id.recordButton);

        effectButton =  (ImageButton)findViewById(R.id.EffectButton);
        radioLayout = (LinearLayout)findViewById(R.id.linearLayout);
        radioMasks = (RadioButton)findViewById(R.id.masks);
        radioEffects = (RadioButton)findViewById(R.id.effects);
        radioFilters = (RadioButton)findViewById(R.id.filters);

        arView = (SurfaceView) findViewById(R.id.surface);
        arView.getHolder().addCallback(this);

        // Surface might already be initialized, so we force the call to onSurfaceChanged
        arView.setVisibility(View.GONE);
        arView.setVisibility(View.VISIBLE);


        cameraGrabber = new CameraGrabber();
        cameraGrabber.initCamera(new CameraGrabberListener() {
            @Override
            public void onCameraInitialized() {
                cameraGrabber.setFrameReceiver(deepAR);
                cameraGrabber.startPreview();
            }

            @Override
            public void onCameraError(String errorMsg) {
                Log.e(TAG, errorMsg);
            }


        });

        setupEffects();

        screenshotBtn = (ImageButton)findViewById(R.id.recordButton);
        screenshotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deepAR.takeScreenshot();
            }
        });

        videoButton = (ImageButton)findViewById(R.id.videoButton);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording) {
                    stopRecording();
                } else {
                    deepAR.takeScreenshot();
                    new File(Environment.getExternalStorageDirectory().toString() + File.separator + "snap").mkdir();
                    Date date = new Date();
                    nameFile = new SimpleDateFormat("yyyy-MM-dd_hhmmss").format(date);
                    deepAR.startVideoRecording(Environment.getExternalStorageDirectory().toString() + File.separator + "snap" + File.separator + nameFile + ".mp4", 1f);
                    recording = true;

                    handler2.post(runnableCode);
                    handler.postDelayed(runnable2, 120000);

                }
            }
        });



        galleryButton = (ImageButton)findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GalleryActivity.class);
                startActivity(intent);
            }
        });

        shareButton = (ImageButton)findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fichier = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "snap");
                File[] listefichiers = fichier.listFiles();
                if(listefichiers.length > 0){
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "test");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(listefichiers[1].getPath())));
                    sharingIntent.setType("video/mp4");
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    v.getContext().startActivity(Intent.createChooser(sharingIntent,"Share"));
                }
            }
        });

        switchCamera = (ImageButton) findViewById(R.id.switchCamera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cameraDevice = cameraGrabber.getCurrCameraDevice() ==  Camera.CameraInfo.CAMERA_FACING_FRONT ?  Camera.CameraInfo.CAMERA_FACING_BACK :  Camera.CameraInfo.CAMERA_FACING_FRONT;
                cameraGrabber.changeCameraDevice(cameraDevice);
            }
        });

        previousMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoPrevious();
            }
        });

        nextMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNext();
            }
        });

        radioMasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEffects.setChecked(false);
                radioFilters.setChecked(false);
                radioButtonClicked();
            }
        });
        radioEffects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioMasks.setChecked(false);
                radioFilters.setChecked(false);
                radioButtonClicked();
            }
        });
        radioFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEffects.setChecked(false);
                radioMasks.setChecked(false);
                radioButtonClicked();
            }
        });

        chrono = (TextView)findViewById(R.id.chrono);
        handler2 = new Handler();
        chrono.setText("00:00");

        effectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioLayout.getVisibility() == View.VISIBLE) {
                    radioLayout.setVisibility(View.INVISIBLE);
                } else {
                    radioLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoButton.getVisibility() == View.VISIBLE) {
                    videoButton.setVisibility(View.INVISIBLE);
                    recordButton.setVisibility(View.VISIBLE);
                } else if (videoButton.getVisibility() == View.INVISIBLE){
                    videoButton.setVisibility(View.VISIBLE);
                    recordButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void stopRecording() {
        deepAR.stopVideoRecording();
        recording = false;
        Intent myIntent = new Intent(videoButton.getContext(),
                VideoViewActivity.class);
        myIntent.putExtra("name_of_extra", Environment.getExternalStorageDirectory().toString() + File.separator + "snap" + File.separator + nameFile + ".mp4");
        videoButton.getContext().startActivity(myIntent);
        handler.removeCallbacks(runnable2);
        handler2.removeCallbacks(runnableCode);
        chrono.setText("00:00");
        time = 0;
    }


    @Override
    public void screenshotTaken(final Bitmap screenshot) {
        new File(Environment.getExternalStorageDirectory().toString() + File.separator + "snap").mkdir();
        Date date = new Date();
        try {
            File imageFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "snap" + File.separator + nameFile +  ".jpg");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            screenshot.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            MediaScannerConnection.scanFile(MainActivity.this, new String[]{imageFile.toString()}, null, null);
            Toast.makeText(MainActivity.this, getResources().getString(R.string.screenshot_saved), Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Override
    public void videoRecordingStarted() {

    }

    @Override
    public void videoRecordingFinished() {
    }

    @Override
    public void videoRecordingFailed() {

    }

    @Override
    public void initialized() {

    }

    @Override
    public void faceVisibilityChanged(boolean faceVisible) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        deepAR.setRenderSurface(surfaceHolder.getSurface(), width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        deepAR.setRenderSurface(null,0,0);
    }

    @Override
    public void videoRecordingPrepared() {}

    public void setImage(){
        ImageView bandeau = (ImageView)findViewById(R.id.bandeau);
        bandeau.setImageResource(R.drawable.bandeau);

        ImageView bandeauBas = (ImageView)findViewById(R.id.bandeauBas);
        bandeauBas.setImageResource(R.drawable.bandeau_du_bas);

        ImageView boutonShot = (ImageView)findViewById(R.id.videoButton);
        boutonShot.setImageResource(R.drawable.bouton_film);

        ImageView boutonVideo = (ImageView)findViewById(R.id.recordButton);
        boutonVideo.setImageResource(R.drawable.bouton_photo);

        ImageView boutonGallery = (ImageView)findViewById(R.id.galleryButton);
        boutonGallery.setImageResource(R.drawable.galerie);

        ImageView boutonEffect = (ImageView)findViewById(R.id.EffectButton);
        boutonEffect.setImageResource(R.drawable.bouton_custom);

        ImageView boutonShare = (ImageView)findViewById(R.id.shareButton);
        boutonShare.setImageResource(R.drawable.bouton_partage);

        ImageView boutonSwitch = (ImageView)findViewById(R.id.switchCam);
        boutonSwitch.setImageResource(R.drawable.switch_rouge);
    }
}
