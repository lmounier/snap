package ai.deepar.example;

import android.Manifest;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ai.deepar.ar.AREventListener;
import ai.deepar.ar.CameraGrabber;
import ai.deepar.ar.CameraGrabberListener;
import ai.deepar.ar.DeepAR;

public class MainActivity extends PermissionsActivity implements AREventListener, SurfaceHolder.Callback {

    private final String TAG = MainActivity.class.getSimpleName();

    private SurfaceView arView;
    private CameraGrabber cameraGrabber;
    private ImageButton screenshotBtn;
    private ImageButton switchCamera;
    private ImageButton nextMask;
    private ImageButton previousMask;

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

    ArrayList<AREffect> masks;
    ArrayList<AREffect> effects;
    ArrayList<AREffect> filters;

    private DeepAR deepAR;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        masks.add(new AREffect("aviators", AREffect.EffectTypeMask));
        masks.add(new AREffect("bigmouth", AREffect.EffectTypeMask));
        masks.add(new AREffect("dalmatian", AREffect.EffectTypeMask));
        masks.add(new AREffect("flowers", AREffect.EffectTypeMask));
        masks.add(new AREffect("koala", AREffect.EffectTypeMask));
        masks.add(new AREffect("lion", AREffect.EffectTypeMask));
        masks.add(new AREffect("smallface", AREffect.EffectTypeMask));
        masks.add(new AREffect("teddycigar", AREffect.EffectTypeMask));
        masks.add(new AREffect("kanye", AREffect.EffectTypeMask));
        masks.add(new AREffect("tripleface", AREffect.EffectTypeMask));
        masks.add(new AREffect("sleepingmask", AREffect.EffectTypeMask));
        masks.add(new AREffect("fatify", AREffect.EffectTypeMask));
        masks.add(new AREffect("obama", AREffect.EffectTypeMask));
        masks.add(new AREffect("mudmask", AREffect.EffectTypeMask));
        masks.add(new AREffect("pug", AREffect.EffectTypeMask));
        masks.add(new AREffect("slash", AREffect.EffectTypeMask));
        masks.add(new AREffect("twistedface", AREffect.EffectTypeMask));
        masks.add(new AREffect("grumpycat", AREffect.EffectTypeMask));

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
        previousMask = (ImageButton)findViewById(R.id.previousMask);
        nextMask = (ImageButton)findViewById(R.id.nextMask);

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

                // Video recording example
                if (recording) {
                    deepAR.stopVideoRecording();
                    recording = false;
                } else {
                    deepAR.startVideoRecording(Environment.getExternalStorageDirectory().toString() + File.separator + "video.mp4", 1f);
                    recording = true;
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
    }

    @Override
    public void screenshotTaken(final Bitmap screenshot) {
        CharSequence now = DateFormat.format("yyyy_MM_dd_hh_mm_ss", new Date());
        try {
            File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/DeepAR_" + now + ".jpg");
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

}
