package es.upv.master.android.reconocimientofacial;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.upv.master.android.reconocimientofacial.camera.CameraSource;
import static es.upv.master.android.reconocimientofacial.RecognitionActivity.listBitmapPhotos;

public class ShowPhotoActivity extends AppCompatActivity {
    private ImageView fotoFinal, imagShowMask;
    private Button aceptar, cancelar, girarMask;
    private Bitmap bitmapShowFoto;
    private String getTypePhoto;
    private int getTypeCamera;
    private boolean isTurnedMask = true;
    //EStas variables corresponde a las claves para pasar información de una actividad a otra
    public static final String TypePhoto = "TypePhotoShow";
    public static final String TypeCamera = "TypeCameraShow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);
        //Parte gráfica
        fotoFinal= findViewById(R.id.imgPhotoFinal);
        imagShowMask = findViewById(R.id.imgShowMask);
        aceptar = findViewById(R.id.btn_acept);
        cancelar = findViewById(R.id.btn_cancel);
        girarMask = findViewById(R.id.btn_girar_mascara);

        Bundle extras = getIntent().getExtras();
        getTypePhoto = (String) extras.get(TypePhoto);
        getTypeCamera = extras.getInt(TypeCamera) ;

        //Verifico si es cámara frontal o trasera
        if(CameraSource.CAMERA_FACING_FRONT == getTypeCamera){
            //Cambio el tipo de máscara que se ubica sobre la foto si es invertida cara_1
            boolean isPerfil = getTypePhoto.equals("F");
            imagShowMask.setImageResource(isPerfil ? R.drawable.cara_f : R.drawable.cara_p1);
            //Boton cambiar el perfil de la mascara solo se observa solo si es de perfil la foto y cámara frontal
            if(!isPerfil)
            girarMask.setVisibility(View.VISIBLE);
        }else{
            //Cambio el tipo de máscara que se ubica sobre la foto
            imagShowMask.setImageResource(getTypePhoto.equals("F") ? R.drawable.cara_f : R.drawable.cara_p);
        }

        bitmapShowFoto = getTypePhoto.equals("F") ? listBitmapPhotos.get(0):  listBitmapPhotos.get(1);

        if(bitmapShowFoto != null)
            fotoFinal.setImageBitmap(bitmapShowFoto);



    }

    public void cancel(View view){
        //bitmapShowFoto = null;
        if(!listBitmapPhotos.isEmpty())
            listBitmapPhotos.remove(getTypePhoto.equals("F") ? 0:1);
        finish();
    }

    public void acept(View view){
        //bitmapPhoto = null;
        //TYPE_PHOTO++;
        finish();
    }

    public void girarMascara(View view){
        if(isTurnedMask) isTurnedMask = false;
        else isTurnedMask = true;
        imagShowMask.setImageResource(isTurnedMask ? R.drawable.cara_p1 : R.drawable.cara_p);
    }


    //Funció me permite guardar en memoria externa las fotos, siempre y cuando existan los permisos en el manifests
   /* public void savePhotosExternalStorage(String nameFolder, String nameFile, Bitmap photo, Context context){
        try {
            File imageFile;
            String state = Environment.getExternalStorageState();
            File folder = null;
            if (state.contains(Environment.MEDIA_MOUNTED)) {
                folder = new File(Environment
                        .getExternalStorageDirectory() + "/"+nameFolder);
            } else {
                folder = new File(Environment
                        .getExternalStorageDirectory() + "/"+nameFolder);
            }

            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                imageFile = new File(folder.getAbsolutePath()
                        + File.separator
                        + nameFile+".jpg");


                imageFile.createNewFile();

            } else {
                Toast.makeText(getBaseContext(), "Image Not saved",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            // save image into gallery
            photo = resize(photo, 800, 600);
            photo.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

            FileOutputStream fout = new FileOutputStream(imageFile);
            fout.write(ostream.toByteArray());
            fout.close();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN,
                    System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA,
                    imageFile.getAbsolutePath());

            context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            setResult(Activity.RESULT_OK); //add this


        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


}
