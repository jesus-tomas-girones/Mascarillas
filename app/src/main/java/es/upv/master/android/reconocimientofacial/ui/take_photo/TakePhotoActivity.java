package es.upv.master.android.reconocimientofacial.ui.take_photo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.android.gms.vision.CameraSource;
import es.upv.master.android.reconocimientofacial.FaceGraphic;
import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.camera.CameraSource;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.material.snackbar.Snackbar;

import es.upv.master.android.reconocimientofacial.camera.CameraSourcePreview;
import es.upv.master.android.reconocimientofacial.camera.PhotoRotation;
import es.upv.master.android.reconocimientofacial.camera.GraphicOverlay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static es.upv.master.android.reconocimientofacial.data.DataBase.subirFotos;
import static es.upv.master.android.reconocimientofacial.ui.take_photo.ShowPhotoActivity.CaraGirada;
import static es.upv.master.android.reconocimientofacial.ui.take_photo.ShowPhotoActivity.TypeCamera;
import static es.upv.master.android.reconocimientofacial.ui.take_photo.ShowPhotoActivity.TypePhoto;
import static es.upv.master.android.reconocimientofacial.camera.PhotoRotation.resize;
import static es.upv.master.android.reconocimientofacial.camera.PhotoRotation.rotateImage;


public class TakePhotoActivity extends AppCompatActivity {

    private static final String TAG = "TakePhoto";

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private Button btnFlas, btnGirarCamara, btnTakePhoto;
    private ImageView girarMascara;
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private ImageView diagramaCara, miniaturaFotoF, miniaturaFotoP;

    //Tipo de foto frontal (F), perfil (P)
    private String typePhoto = "F";
    private boolean voltearCamara = false;
    private boolean autoFocus, useFlash;
    private int idCamera = CameraSource.CAMERA_FACING_FRONT;;
    private int numPerfil;
    public static int NUM_PHOTOS = 0;

    public static ArrayList<Bitmap> listBitmapPhotos;
    private ArrayList<String> listIdPhotos;
    private String idphoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        //Parte Gráfica
        mPreview = (CameraSourcePreview) findViewById(R.id.facePreview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        girarMascara = findViewById(R.id.botonGirarMascara);
        diagramaCara = (ImageView)findViewById(R.id.imgCara);
        diagramaCara.setMaxHeight( mGraphicOverlay.getHeight());
        miniaturaFotoF = (ImageView)findViewById(R.id.imgPhotoF);
        miniaturaFotoP = (ImageView)findViewById(R.id.imgPhotoP);
        btnFlas = (Button) findViewById(R.id.btn_flash);
        btnGirarCamara = (Button) findViewById(R.id.btn_girarCamara);
        btnTakePhoto = (Button) findViewById(R.id.btn_takePhoto);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            //configurando camara
            autoFocus = true; useFlash = false;
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        listBitmapPhotos = new ArrayList<Bitmap>();
        listIdPhotos = new ArrayList<String>();

    }

    public void takeImage(View view) {
            try{
            //Aquí ejecutamos nuestras tareas costosas
            mCameraSource.takePicture(null, new CameraSource.PictureCallback() {

                @Override
                public void onPictureTaken(byte[] bytes) {
                    try {
                        // convert byte array into bitmap
                        Bitmap loadedImage = null;
                        Bitmap rotatedBitmap = null;
                        loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);
                        //Observo cuanto es el bitma que obtengo al tomar la foto
                        Log.d("BITMAP", loadedImage.getWidth() + "x" + loadedImage.getHeight());

                        // rotate Image
                        int orientation = PhotoRotation.getOrientation(bytes);

                        switch(orientation) {
                            case 90:
                                rotatedBitmap= rotateImage(loadedImage, 90);

                                break;
                            case 180:
                                rotatedBitmap= rotateImage(loadedImage, 180);

                                break;
                            case 270:
                                rotatedBitmap= rotateImage(loadedImage, 270);

                                break;
                            case 0:
                                // if orientation is zero we don't need to rotate this
                                rotatedBitmap = loadedImage;
                            default:
                                break;
                        }
                        //Fin de rotar Image

                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                        // save image into gallery
                        rotatedBitmap = resize(rotatedBitmap, 640,480); //800x600
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                        listBitmapPhotos.add(rotatedBitmap);

                        Intent i = new Intent(getApplicationContext(), ShowPhotoActivity.class);
                        i.putExtra(TypePhoto, typePhoto);//"F-P"
                        i.putExtra(TypeCamera, idCamera);//FRONT, BACK
                        i.putExtra(CaraGirada, numPerfil);
                        startActivity(i);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }catch (Exception ex){
            Log.e(TAG, "Error al capturar fotografia!");
        }

    }


    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mPreview, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    private void createCameraSource(boolean autoFocus, boolean useFlash) {

        Context context = this.getApplicationContext();

       FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        //final TextRecognizer detector = new TextRecognizer.Builder(getApplicationContext()).build();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }
        //Eligiendo la cámara
        idCamera = voltearCamara ? CameraSource.CAMERA_FACING_BACK :  CameraSource.CAMERA_FACING_FRONT;
        System.out.println("System Recognition: "+idCamera  );
        //Configurando recursos de vista
        settingCameraResource(idCamera);
        mCameraSource = new CameraSource.Builder(getApplicationContext(), detector)
                .setFacing(idCamera )
                .setRequestedPreviewSize(1280, 720) //1024, 768 //1280, 960 //1280, 720
//                .setRequestedPreviewSize(640, 480) //1024, 768 //1280, 960 //1280, 720
                .setRequestedFps(30.0f)
                //.setAutoFocusEnabled(true)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                .build();

        Snackbar.make( mPreview, R.string.snackbar_try_mach_face, Snackbar.LENGTH_LONG)
                .setAction("OK", new View.OnClickListener()
                { @Override public void onClick(View view) { } }) .show();
    }

    private void transiccionEntreActivities(){
        int opt = NUM_PHOTOS;
        switch (opt){
            case 0:
                settingToStart();
                typePhoto = "F";
                idphoto = null;
                girarMascara.setVisibility(View.INVISIBLE);
                if(listBitmapPhotos.size() == 1)
                    listBitmapPhotos.remove(0);
                break;
            case 1:
                if(listBitmapPhotos.size() == 2){
                    listBitmapPhotos.remove(1);
                    return;
                }
                typePhoto = "P";
                miniaturaFotoP.setVisibility(View.VISIBLE);
                miniaturaFotoF.setImageBitmap(listBitmapPhotos.get(0));
                numPerfil = R.drawable.mask_perfil_der;
                diagramaCara.setImageResource(numPerfil);
                girarMascara.setVisibility(View.VISIBLE);
                if(idphoto == null) idphoto = generateNamePhoto();
                if(listIdPhotos.isEmpty())
                listIdPhotos.add(0, idphoto+"_F");
                break;
            case 2:
                typePhoto = "F";
                NUM_PHOTOS = 0;
                diagramaCara.setImageResource(R.drawable.mask_frontal);
                miniaturaFotoP.setImageBitmap(listBitmapPhotos.get(1));
                girarMascara.setVisibility(View.INVISIBLE);
                listIdPhotos.add(1, idphoto+"_P");
                subirFotos(TakePhotoActivity.this,listBitmapPhotos, listIdPhotos);
                //subirAFirebaseStorage();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
        //según el usuario acepte las fotos se va configurando las vistas
        transiccionEntreActivities();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        //settingToStart();
        mPreview.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        settingToStart();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            autoFocus = true; useFlash = false;
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();

    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */

    private void startCameraSource()  throws SecurityException {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
               // openCamera(voltearCamara ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }



    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            //mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            //mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }


    ///
    public void rotarCamara(View view){
        //Informo que se desea voltear la cámara a través del atributo voltearCamara
        //voltearCamara = !voltearCamara;
        if(voltearCamara){
            voltearCamara = false;
        }
        else{
            voltearCamara = true;
        }
        //Libero la camara
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        //Comfiguro la nueva cámara  y la inicio
        createCameraSource(true, useFlash);
        startCameraSource();
    }

    public void settingCameraResource(int idCamera){
        if(idCamera == CameraSource.CAMERA_FACING_FRONT){
            useFlash = false;
            btnFlas.setVisibility(View.INVISIBLE);

        }else {
            useFlash = false;
            btnFlas.setVisibility(View.VISIBLE);
        }
    }


    public void activarFlash(View view){
        useFlash = !useFlash;
        if(useFlash){
            mCameraSource .setFlashMode( Camera.Parameters.FLASH_MODE_TORCH);// FLASH_MODE_OFF,  FLASH_MODE_ON,  FLASH_MODE_TORCH
            btnFlas.setBackgroundResource(R.drawable.ic_flash_on);
        }else{
            mCameraSource .setFlashMode( Camera.Parameters.FLASH_MODE_OFF);
            btnFlas.setBackgroundResource(R.drawable.ic_flash_off);
        }
    }

    public String generateNamePhoto(){
        int numeroRandom = (int)(Math.random()*1000);
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyMMddHHmmss");
        Date time = new Date(System.currentTimeMillis());
        return formatoFecha.format(time)+""+numeroRandom;
    }


    private void settingToStart(){
        //Restablezco los valores de inicio
        typePhoto = "F";
        NUM_PHOTOS = 0;
        idphoto = null;
        miniaturaFotoP.setVisibility(View.INVISIBLE);
        miniaturaFotoF.setImageResource(R.drawable.mask_frontal);
        miniaturaFotoP.setImageResource(R.drawable.mask_perfil_der);
        diagramaCara.setImageResource(R.drawable.mask_frontal);
        listBitmapPhotos.clear();
        listIdPhotos.clear();
    }

    public void girarMascaraPerfil(View view){
        if(numPerfil == R.drawable.mask_perfil_der) {
            diagramaCara.setImageResource(R.drawable.mask_perfil_izq);
            numPerfil = R.drawable.mask_perfil_izq;
        }
        else {
            diagramaCara.setImageResource(R.drawable.mask_perfil_der);
            numPerfil = R.drawable.mask_perfil_der;
        }
    }

}


