package es.upv.master.android.reconocimientofacial.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.model.Photo;

import static android.content.Context.MODE_PRIVATE;

public class DataBase {
   static public final String COLLECTION = "photos";
   static public final String LOCAL_DIRECTORY = "Mascarillas";
   private static final int MAX_LABELS = 100;
   static public String REFERENCE_FIRESTORAGE = "gs://mascarilla-440d4.appspot.com";
   public static StorageReference storageRef;
   public static boolean subiendoDatos, descargandoDatos = false;
   public static final String TAG = "Mascarillas";
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
         showDialogFireStorage(activity, title, mensaje, RETURN_MAIN_ACTIVITY_DIALOG);
      }
   }

   public static final int RETURN_MAIN_ACTIVITY_DIALOG = 0;
   public static final int DISMISS_DIALOG = 1;

   public static void showDialogFireStorage(final Activity activity, final String title,
                                            final String message, final int type_action) {
      /**
       * Dado el id de la colección photos, lee sus etiquetas y coordenadas por medio de un listener
       *
       * @param title título de la notificación
       * @param message mensaja de la notificación
       * @param type_action tipo de acción ha realizar al pulsar el botón "OK",
       *                    RETURN_MAIN_ACTIVITY_DIALOG = 0, DISMISS_DIALOG = 1
       */
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
                            switch (type_action){
                               case RETURN_MAIN_ACTIVITY_DIALOG:
                                  activity.finish();
                                  break;
                               case DISMISS_DIALOG:
                                  dialog.dismiss();
                                  break;
                            }

                         }
                      });
      builder.create().show();
   }


   public static void updateLabels(String id, List<String> label, List<Float> x, List<Float> y) {
      /**
       * Escribe en el documento id de la colección photos, un conjunto de etiquetas y sus coordenadas.
       * Las etiquetas no indicadas son eliminadas del documento.
       *
       * @param id nombre del documento a escribir sus etiquetas
       * @param label lista con las etiquetas
       * @param label lista con la coordenada x de cada una de las etiquetas
       * @param label lista con la coordenada y de cada una de las etiquetas
       */
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      DocumentReference photoRef = db.collection(COLLECTION).document(id);
      Map<String, Object> dataLabel = new HashMap<>();
      for (int i=0; i<label.size(); i++){
         dataLabel.put("label" + (i+1), label.get(i));
         dataLabel.put("x" + (i+1), x.get(i));
         dataLabel.put("y" + (i+1), y.get(i));
      }
      for (int i=label.size(); i<9; i++){
         dataLabel.put("label" + (i+1), FieldValue.delete());
         dataLabel.put("x" + (i+1), FieldValue.delete());
         dataLabel.put("y" + (i+1), FieldValue.delete());
      }
      dataLabel.put("labelled", true);
//      dataLabel.put("number_label", label.size());
      photoRef.update(dataLabel);
   }

   public interface LoadLabelsListener {
      void onLoad(List<String> label, List<Double> x, List<Double> y);
   }

   public interface LoadLabelledPhotosListener{
      void onLoadPhotos(List<Map<String, Object>> listLabelledPhotos);
   }

   public static void loadLabels(String id, final LoadLabelsListener listener) {
      /**
       * Dado el id de la colección photos, lee sus etiquetas y coordenadas por medio de un listener
       *
       * @param id nombre del documento a leer sus etiquetas
       * @param listener escuchador que llamaremos cuando se tengan las etiquetas
       */
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      DocumentReference photoRef = db.collection(COLLECTION).document(id);
      Task<DocumentSnapshot> query = photoRef.get()
                 .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                       if (task.isSuccessful()) {
                          List<String> label = new ArrayList<>();
                          List<Double> x = new ArrayList<>();
                          List<Double> y = new ArrayList<>();
                          for (int i = 1; i <= MAX_LABELS; i++) {
                             String s = task.getResult().getString("label"+i);
                             if (s != null) {
                                label.add(s);
                                x.add(task.getResult().getDouble("x"+i));
                                y.add(task.getResult().getDouble("y"+i));
                             }
                          }
                          listener.onLoad(label, x, y);
                       } else {
                         Log.e("MASCARILLA", "Error en Database.loadLabels() accediendo a Firebase"); //DOTO crear costante TAG
                       }
                    }
                 });
   }

   public static void searchLabelledPhoto(long inicialDate, long finalDate,
              final boolean withPhoto, final boolean withLabel, final LoadLabelledPhotosListener listener){
      /**
       * Dado el id de la colección photos, lee sus etiquetas y coordenadas por medio de un listener
       *
       * @param inicialDate
       * @param finalDate
       * @param withPhoto
       * @param withLabel
       */
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      Query photosRef = db.collection(COLLECTION)
                        .whereEqualTo("labelled", true)
                        .whereGreaterThanOrEqualTo("creation_date", inicialDate);
               if(finalDate>0)
                  photosRef.whereLessThanOrEqualTo("creation_date", finalDate);
                  photosRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                           List<DocumentSnapshot> listLabels = task.getResult().getDocuments();
                           List< Map<String, Object>> listLabelledPhotos = new ArrayList<Map<String, Object>>();
                           for(DocumentSnapshot labelDocument : listLabels){
                              Map<String, Object> labelData = new HashMap<>();
                              //Añadir a la búsqueda las etiquetas de las fotos
                              if(withLabel){
                                 List<String> label = new ArrayList<>();
                                 List<Double> x = new ArrayList<>();
                                 List<Double> y = new ArrayList<>();
                                 for (int i = 1; i <= MAX_LABELS; i++) {
                                    String s = labelDocument.getString("label"+i);
                                    if (s != null) {
                                       label.add(s);
                                       x.add(labelDocument.getDouble("x"+i));
                                       y.add(labelDocument.getDouble("y"+i));
                                    }
                                 }
                                 labelData.put("label", label);
                                 labelData.put("x", x);
                                 labelData.put("y", x);
                              }
                              //Añadir a la búsqueda las url de las fotos, podemos obtener también las id de los documentos
                              //que corresponde al mismo nombre de las fotos en el firestorage
                              if(withPhoto){
                                 String urlPhoto = labelDocument.getString("urlPhoto");
                                 labelData.put("uriPhoto", urlPhoto);
                                 String idPhoto = labelDocument.getId();
                                 labelData.put("idPhoto", idPhoto);
                              }

                              listLabelledPhotos.add(labelData);
                           }
                           listener.onLoadPhotos(listLabelledPhotos);
                        } else {
                           Log.e("MASCARILLA", "Error en Database.loadLabels() accediendo a Firebase"); //DOTO crear costante TAG
                        }
                     }
                  });
   }

   public static void downloadPhotosById(final Activity activity, final ArrayList<String> list_id,
                                         int initial_id, final ProgressDialog progressDownload){

      //Si entra al if significa que la descarga ha finalizado, muestra un dialogo y vacía la lista de id
      //para no concatenar nuevos elementos si se continúa realizando nuevas descargas
      final int index_id = initial_id;
      if(index_id >= list_id.size() && index_id != 0){
         list_id.clear();
         String title = activity.getResources().getString(R.string.title_mostrar_dialogo);
         String mensaje = activity.getResources().getString(R.string.message_mostrar_dialogo_descargas_finalizada);
         showDialogFireStorage(activity, title, mensaje, DISMISS_DIALOG);
         return;
      }

      //Barra de progreso de descarga
      final int id = index_id +1;
      progressDownload.setTitle("Descargando... Foto" + id + "/" + list_id.size());
      progressDownload.setMessage("Espere...");
      progressDownload.setCancelable(true);
      progressDownload.setCanceledOnTouchOutside(false);

      //Creo el ruta y l directorio donde se almacenará las fotos y lo monto en MediaStore
      // para que el usuario pueda observar el directorio desde galeria
      File rootPath = new File(Environment.getExternalStorageDirectory(), LOCAL_DIRECTORY);
      if(!rootPath.exists()) {
         rootPath.mkdirs();
      }
      final File localFile = new File(rootPath,list_id.get(index_id)+".jpg");
      ContentValues values = new ContentValues();
      values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
      values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
      values.put(MediaStore.MediaColumns.DATA, localFile.getAbsolutePath());
      activity.getContentResolver().insert(
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
      Log.d("Almacenamiento Interno", "creando fichero: " + rootPath.getAbsolutePath());

      //Instancio FirebaseStorage y le paso la referencia URL donde debe apuntar para descargar las fotos
      FirebaseStorage storage = FirebaseStorage.getInstance();
      storageRef = storage.getReferenceFromUrl(REFERENCE_FIRESTORAGE);
      StorageReference ficheroRef = storageRef.child(COLLECTION).child(list_id.get(index_id)+".jpg");

      ficheroRef.getFile(localFile)
              .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                 @Override
                 public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    if(taskSnapshot.getTask().isSuccessful()){
                       Log.d(TAG, "Database.downloadPhotosById() - Derscarga de la imagen: "+list_id.get(index_id));

                       if(index_id == list_id.size()-1){
                          progressDownload.dismiss();
                          descargandoDatos = false;
                       }
                       downloadPhotosById(activity, list_id, id, progressDownload);
                    }
                 }
              }).addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {
                     Log.e("MASCARILLA", "Error en Database.downloadPhotosById() accediendo a Firestore"); //DOTO crear costante TAG
                     String title = activity.getResources().getString(R.string.title_mostrar_dialogo_no_descargas);
                     String mensaje = activity.getResources().getString(R.string.message_mostrar_dialogo_descargas_fallidas);
                     showDialogFireStorage(activity, title, mensaje, DISMISS_DIALOG);
                  }
               }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                  @Override
                  public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                     if (!subiendoDatos) {
                        progressDownload.show();
                        descargandoDatos = true;
                     } else {
                        if (taskSnapshot.getTotalByteCount() > 0)
                           progressDownload.setMessage("Espere... " +
                                   String.valueOf(100 * taskSnapshot.getBytesTransferred()
                                           / taskSnapshot.getTotalByteCount()) + "%");
                     }
                  }
               });

   }

}
