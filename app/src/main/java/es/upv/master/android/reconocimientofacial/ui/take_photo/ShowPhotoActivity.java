package es.upv.master.android.reconocimientofacial.ui.take_photo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.camera.CameraSource;

import static es.upv.master.android.reconocimientofacial.ui.take_photo.TakePhotoActivity.NUM_PHOTOS;
import static es.upv.master.android.reconocimientofacial.ui.take_photo.TakePhotoActivity.listBitmapPhotos;

public class ShowPhotoActivity extends AppCompatActivity {
    private ImageView fotoFinal, imagShowMask;
    private Button aceptar, cancelar, girarMask;
    private Bitmap bitmapShowFoto;
    private String getTypePhoto;
    private int getTypeCamera;
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
        getTypeCamera = extras.getInt(TypeCamera) ;
        isFrontal = getTypePhoto.equals("F");

        //Verifico si es foto de perfil
        if(isFrontal)
            imagShowMask.setImageResource(R.drawable.mask_frontal);
        else{
            girarMask.setVisibility(View.VISIBLE);
            int numeroCara = extras.getInt(CaraGirada);
            //Verifico si es cámara frontal o trasera, porque si es frontal invierto perfil caso contrario se mantien
            if(CameraSource.CAMERA_FACING_FRONT == getTypeCamera){
                numPerfil = numeroCara == R.drawable.mask_perfil_der ? R.drawable.mask_perfil_izq : R.drawable.mask_perfil_der;
            }else{
                numPerfil = numeroCara == R.drawable.mask_perfil_der ? R.drawable.mask_perfil_der : R.drawable.mask_perfil_izq;
            }
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
        if(numPerfil == R.drawable.mask_perfil_der) {
            imagShowMask.setImageResource(R.drawable.mask_perfil_izq);
            numPerfil = R.drawable.mask_perfil_izq;
        }
        else {
            imagShowMask.setImageResource(R.drawable.mask_perfil_der);
            numPerfil = R.drawable.mask_perfil_der;
        }
    }

}
