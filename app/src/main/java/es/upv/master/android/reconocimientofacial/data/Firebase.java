package es.upv.master.android.reconocimientofacial.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.model.Photo;
import es.upv.master.android.reconocimientofacial.ui.take_photo.TakePhotoActivity;

public class Firebase {
   static public final String COLLECTION = "photosPrueba"; //"Mascarillas"
   static public String REFERENCE_FIRESTORAGE = "gs://mascarilla-440d4.appspot.com";
   public static StorageReference storageRef;
   public static boolean subiendoDatos =false;

   public static void registrarFoto(final long creation_date, final String id, String url){
      Photo photo = new Photo(creation_date,false, url);
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      db.collection(COLLECTION).document(id).set(photo);
   }

   public static void subirFotos(final Activity activity, final ArrayList<Bitmap> photos, final ArrayList<String> idphotos) {

      if (!photos.isEmpty()){
         //Condición
         //La función es recursiva por eso esta variable me permite enumerar las fotos que se van subiendo a firebase
         final int numPhotoUp = Math.abs(photos.size() - idphotos.size())+1;

         final ProgressDialog progresoSubida = new ProgressDialog(activity);
         progresoSubida.setTitle("Subiendo... Foto"+ numPhotoUp +"/"+2);
         progresoSubida.setMessage("Espere...");
         progresoSubida.setCancelable(true);
         progresoSubida.setCanceledOnTouchOutside(false);

         Bitmap bitmap = photos.get(0);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
         byte[] data = baos.toByteArray();

         FirebaseStorage storage = FirebaseStorage.getInstance();
         storageRef = storage.getReferenceFromUrl( REFERENCE_FIRESTORAGE);
         final int index = numPhotoUp-1;
         StorageReference imagenRef = storageRef.child(COLLECTION).child(idphotos.get(index)+".jpg");
         UploadTask uploadTask = imagenRef.putBytes(data);
         uploadTask .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
               subiendoDatos = false;
               //mostrarDialogo(getApplicationContext(), "Ha ocurrido un error al" +
               //    " subir la imagen o el usuario ha cancelado la subida.");
               Snackbar.make( activity.getCurrentFocus(), R.string.error_upload_photos, Snackbar.LENGTH_INDEFINITE)
                       .setAction("SI", new View.OnClickListener()
                       { @Override public void onClick(View view) {
                          if (!photos.isEmpty()){
                             subirFotos(activity, photos, idphotos);
                          }
                       } })
                       .setAction("NO",new View.OnClickListener()
                       { @Override public void onClick(View view) {  /*settingToStart();*/ } }).show();

            }
         })
                 .addOnSuccessListener(
                         new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                               Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                               firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                  @Override
                                  public void onSuccess(Uri uri) {

                                     String url = uri.toString();
                                     Log.e("Almacenamiento:", "the url is: " + url);
                                     //String ref = imagenRef.getName();
                                     registrarFoto(System.currentTimeMillis(), idphotos.get(index), url);
                                     progresoSubida.dismiss();
                                     subiendoDatos =false;
                                     if(!photos.isEmpty())
                                        photos.remove(0);
                                     subirFotos(activity, photos, idphotos);
                                  }
                               });

                            }
                         })
                 .addOnProgressListener(
                         new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                               if (!subiendoDatos) {
                                  progresoSubida.show();
                                  subiendoDatos =true;
                               } else {
                                  if (taskSnapshot.getTotalByteCount()>0)
                                     progresoSubida.setMessage("Espere... " +
                                             String.valueOf(100*taskSnapshot.getBytesTransferred()
                                                     /taskSnapshot.getTotalByteCount())+"%");
                               }
                            }
                         });


      }else{
         //Si ya está vacío el arreglo, se ha enviado todas las fotos
         String title = activity.getResources().getString(R.string.title_mostrar_dialogo);
         String mensaje = activity.getResources().getString(R.string.message_mostrar_dialogo);
         showDialogFireStorage(activity,title, mensaje);
      }


   }

   public static void showDialogFireStorage(final Activity activity, final String title,
                               final String message) {
      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder.setTitle(title)
              .setMessage(message)
              .setOnCancelListener(new DialogInterface.OnCancelListener() {
                 @Override
                 public void onCancel(DialogInterface dialog) {
                    activity.finish();
                 }
              })
              .setNegativeButton("OK",
                      new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                         }
                      });
      builder.create().show();
   }


}
