package es.upv.master.android.reconocimientofacial.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.model.Photo;

import static android.content.Context.MODE_PRIVATE;

public class DataBase {
   static public final String COLLECTION = "photosPrueba"; //"Mascarillas"
   static public String REFERENCE_FIRESTORAGE = "gs://mascarilla-440d4.appspot.com";
   public static StorageReference storageRef;
   public static SharedPreferences preferencesLabels = null;
   public static boolean subiendoDatos = false;

   public static void registrarFoto(final long creation_date, final String id, String url) {
      Photo photo = new Photo(creation_date, false, url);
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      db.collection(COLLECTION).document(id).set(photo);
   }

   public static void subirFotos(final Activity activity, final ArrayList<Bitmap> photos, final ArrayList<String> idphotos) {

      if (!photos.isEmpty()) {
         //Condición
         //La función es recursiva por eso esta variable me permite enumerar las fotos que se van subiendo a firebase
         final int numPhotoUp = Math.abs(photos.size() - idphotos.size()) + 1;

         final ProgressDialog progresoSubida = new ProgressDialog(activity);
         progresoSubida.setTitle("Subiendo... Foto" + numPhotoUp + "/" + 2);
         progresoSubida.setMessage("Espere...");
         progresoSubida.setCancelable(true);
         progresoSubida.setCanceledOnTouchOutside(false);

         Bitmap bitmap = photos.get(0);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
         byte[] data = baos.toByteArray();

         FirebaseStorage storage = FirebaseStorage.getInstance();
         storageRef = storage.getReferenceFromUrl(REFERENCE_FIRESTORAGE);
         final int index = numPhotoUp - 1;
         StorageReference imagenRef = storageRef.child(COLLECTION).child(idphotos.get(index) + ".jpg");
         UploadTask uploadTask = imagenRef.putBytes(data);
         uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
               subiendoDatos = false;

               Snackbar.make(activity.getCurrentFocus(), R.string.error_upload_photos, Snackbar.LENGTH_INDEFINITE)
                       .setAction("SI", new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                             if (!photos.isEmpty()) {
                                subirFotos(activity, photos, idphotos);
                             }
                          }
                       })
                       .setAction("NO", new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {  /*settingToStart();*/ }
                       }).show();

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
                                     //Recupero la URL donde se guardan las fotos
                                     String url = uri.toString();
                                     Log.e("Almacenamiento:", "the url is: " + url);
                                     registrarFoto(System.currentTimeMillis(), idphotos.get(index), url);
                                     progresoSubida.dismiss();
                                     subiendoDatos = false;
                                     if (!photos.isEmpty())
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
                                  subiendoDatos = true;
                               } else {
                                  if (taskSnapshot.getTotalByteCount() > 0)
                                     progresoSubida.setMessage("Espere... " +
                                             String.valueOf(100 * taskSnapshot.getBytesTransferred()
                                                     / taskSnapshot.getTotalByteCount()) + "%");
                               }
                            }
                         });


      } else {
         //Si ya está vacío el arreglo, se ha enviado todas las fotos
         String title = activity.getResources().getString(R.string.title_mostrar_dialogo);
         String mensaje = activity.getResources().getString(R.string.message_mostrar_dialogo);
         showDialogFireStorage(activity, title, mensaje);
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

   public static void updateLabel(String id, String label, float x, float y, int indexLabel) {
      DocumentReference photoRef = getCollectionReferencePhotos().document(id);
      Map<String, Object> dataLabel = new HashMap<>();
         dataLabel.put("label" + indexLabel, label);
         dataLabel.put("x" + indexLabel, x);
         dataLabel.put("y" + indexLabel, y);
         dataLabel.put("labelled", true);
      photoRef.update(dataLabel);
   }

   public static void releaseLabel(String id, String label, float x, float y, int indexLabel) {
      DocumentReference photoRef = getCollectionReferencePhotos().document(id);
      Map<String, Object> dataLabel = new HashMap<>();
         dataLabel.put("label" + indexLabel, FieldValue.delete());
         dataLabel.put("x" + indexLabel, FieldValue.delete());
         dataLabel.put("y" + indexLabel, FieldValue.delete());
      photoRef.update(dataLabel);
   }

   public static void loadLabelsToPreference(final Context context, String id) {
      DocumentReference PhotoRef = getCollectionReferencePhotos().document(id);
      preferencesLabels = context.getSharedPreferences(
              "es.upv.master.android.reconocimientofacial.labels", MODE_PRIVATE);
      for (int i = 0; i < 9; i++) {
         final int index = i + 1;
         Task<DocumentSnapshot> query = PhotoRef.get()
                 .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                       if (task.isSuccessful()) {
                          String label = task.getResult().getString("label"+index);
                          if(label != null){
                             double x = task.getResult().getDouble("x" + index);
                             double y = task.getResult().getDouble("y" + index);

                             SharedPreferences.Editor editor = preferencesLabels.edit();
                             editor.putFloat("x" + index, (float) x);
                             editor.putFloat("y" + index, (float) y);
                             editor.putString("label"+ index, label);
                             editor.putInt("indexLabel"+ index, index);
                             editor.commit();

                          }
                       }
                    }
                 });
      }

   }

   public static ArrayList<Double> loadLabels(String id, final int index) {
      DocumentReference PhotoRef = getCollectionReferencePhotos().document(id);
      final Map<String, Object>  dataLabel = new HashMap<>();
//      for (int i = 0; i < 9; i++) {
 //        final int index = i + 1;
      final ArrayList<Double> listPoint = new ArrayList<Double>();
         Task<DocumentSnapshot> query = PhotoRef.get()
                 .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                       if (task.isSuccessful()) {
                          String label = task.getResult().getString("label"+index);
                          if(label != null){
                            // Map<String, Object> dataLabel = new HashMap<>();
                             Double x = task.getResult().getDouble("x" + index);
                             Double y = task.getResult().getDouble("y" + index);
                             dataLabel.put("label"+ index, label);
                             dataLabel.put("x" + index, task.getResult().getDouble("x" + index));
                             dataLabel.put("y" + index, task.getResult().getDouble("y" + index));
                             dataLabel.put("indexLabel", index);
                             //listLabels.add(dataLabel);
                             Log.d("ETIQUETAS", "x" + index + ": " + x + ",y" + index + ": " + y);
                             System.out.println( "x" + index + ": " + x + ",y" + index + ": " + y);
                             listPoint.add(x);
                             listPoint.add(y);
                          }
                       }
                    }
                 });
 //     }

      System.out.println(dataLabel.get(0));
      return listPoint;
   }

   public static CollectionReference getCollectionReferencePhotos(){
      return FirebaseFirestore.getInstance().collection(COLLECTION);
   }

}
