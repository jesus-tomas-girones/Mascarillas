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

import static es.upv.master.android.reconocimientofacial.RecognitionActivity.NUM_PHOTOS;
import static es.upv.master.android.reconocimientofacial.RecognitionActivity.listBitmapPhotos;

public class ShowPhotoActivity extends AppCompatActivity {
    private ImageView fotoFinal, imagShowMask;
    private Button aceptar, cancelar, girarMask;
    private Bitmap bitmapShowFoto;
    private String getTypePhoto;
    //private int getTypeCamera;
    private boolean isTurnedMask = true;
    private boolean isFrontal = true;
    private int numPerfil;
    //EStas variables corresponde a las claves para pasar información de una actividad a otra
    public static final String TypePhoto = "TypePhotoShow";
    public static final String TypeCamera = "TypeCameraShow";
    public static final String CaraGirada = "CaraGirada";

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
        //getTypeCamera = extras.getInt(TypeCamera) ;
        isFrontal = getTypePhoto.equals("F");
        //Verifico si es cámara frontal o trasera
/*        if(CameraSource.CAMERA_FACING_FRONT == getTypeCamera){
            //Cambio el tipo de máscara que se ubica sobre la foto si es invertida cara_1
            imagShowMask.setImageResource(isFrontal ? R.drawable.cara_f : R.drawable.cara_p1);
            //Boton cambiar el perfil de la mascara solo se observa solo si es de perfil la foto y cámara frontal
        }else{
            //Cambio el tipo de máscara que se ubica sobre la foto
            imagShowMask.setImageResource(isFrontal ? R.drawable.cara_f : R.drawable.cara_p);
        }*/

        if(isFrontal)
            imagShowMask.setImageResource(R.drawable.cara_f);
        else{
            girarMask.setVisibility(View.VISIBLE);
            int numeroCara = extras.getInt(CaraGirada);
            numPerfil = numeroCara == R.drawable.cara_p ? R.drawable.cara_p1 : R.drawable.cara_p ;
            imagShowMask.setImageResource(numPerfil);
        }


        bitmapShowFoto = isFrontal ? listBitmapPhotos.get(0):  listBitmapPhotos.get(1);

        if(bitmapShowFoto != null)
            fotoFinal.setImageBitmap(bitmapShowFoto);

    }

    public void cancel(View view){
        if(!listBitmapPhotos.isEmpty())
            listBitmapPhotos.remove(isFrontal ? 0:1);
        finish();
    }

    public void acept(View view){
        NUM_PHOTOS++;
        finish();
    }

    public void girarMascara(View view){
        if(numPerfil == R.drawable.cara_p) {
            imagShowMask.setImageResource(R.drawable.cara_p1);
            numPerfil = R.drawable.cara_p1;
        }
        else {
            imagShowMask.setImageResource(R.drawable.cara_p);
            numPerfil = R.drawable.cara_p;
        }
    }

}
