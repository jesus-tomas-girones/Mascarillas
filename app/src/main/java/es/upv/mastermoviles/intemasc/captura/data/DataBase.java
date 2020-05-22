package es.upv.mastermoviles.intemasc.captura.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.opencsv.CSVWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upv.mastermoviles.intemasc.captura.R;
import es.upv.mastermoviles.intemasc.captura.model.Photo;

public class DataBase {
   static public final String COLLECTION = "photos";
   static public final String LOCAL_DIRECTORY = "Mascarillas";
   public static final int MAX_LABELS = 100;
   static public String REFERENCE_FIRESTORAGE = "gs://mascarilla-440d4.appspot.com";
   public static StorageReference storageRef;
   public static boolean subiendoDatos, descargandoDatos = false;
   public static final String TAG = "Mascarillas";
   public static StorageTask<FileDownloadTask.TaskSnapshot> taskSnapshot = null;
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
              final LoadLabelledPhotosListener listener){
      /**
       * Dado el id de la colección photos, lee sus etiquetas y coordenadas por medio de un listener
       *
       * @param inicialDate: fecha inicial de búsqueda, por ejemplo 01/05/2020
       * @param finalDate: fecha final de la búqueda, por ejemplo 18/05/2020
       * @param listener escuchador que llamaremos cuando se tengan todas las propiedades de las etiquetas
       */
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      Query photosRef = db.collection(COLLECTION)
                     .whereEqualTo("labelled", true)
                     .whereGreaterThanOrEqualTo("creation_date", inicialDate)
                     .whereLessThanOrEqualTo("creation_date", finalDate);

                  photosRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                           List<DocumentSnapshot> listLabels = task.getResult().getDocuments();
                           List< Map<String, Object>> listLabelledPhotos = new ArrayList<Map<String, Object>>();
                           for(DocumentSnapshot labelDocument : listLabels){
                              Map<String, Object> labelData = new HashMap<>();
                              //Añadir a la búsqueda las etiquetas de las fotos
                              //id, fecha, urlphoto, label1, x1, y1, label2, x2, y2, label3, x3, y3,...
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
                              //Añadir a la búsqueda las url de las fotos, podemos obtener también las id de los documentos
                              //que corresponde al mismo nombre de las fotos en el firestorage
                                 String urlPhoto = labelDocument.getString("urlPhoto");
                                 labelData.put("uriPhoto", urlPhoto);
                                 String idPhoto = labelDocument.getId();
                                 labelData.put("idPhoto", idPhoto);
                                 Double creation_date = labelDocument.getDouble("creation_date");
                                 labelData.put("creation_date", creation_date);
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
      /**
       * Dado el id de la colección photos, lee sus etiquetas y coordenadas por medio de un listener
       *
       * @param list_id: lista de id de cada foto a descargar
       * @param initial_id: desde que elemento (index) de la lista se empieza a recorrer
       * @param progressDownload: permite cargar la barra de progreso sin que se inicialice varias veces por cada foto a descargar
       */

      //Si entra al if significa que la descarga ha finalizado, muestra un dialogo y vacía la lista de id
      //para no concatenar nuevos elementos si se continúa realizando nuevas descargas
      final int index_id = initial_id;
      if(index_id >= list_id.size() && index_id != 0 ){
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
      progressDownload.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar",
              new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    //if(taskSnapshot != null)
                    taskSnapshot.cancel();
                    progressDownload.dismiss();
                   //
                    String title = activity.getResources().getString(R.string.title_mostrar_dialogo_operacion_cancel);
                    String mensaje = activity.getResources().
                            getString(R.string.message_mostrar_dialogo_descargas_cancelada)+
                            " Nº Fotos descargadas: "+(index_id+1);
                    showDialogFireStorage(activity, title, mensaje, DISMISS_DIALOG);
                 }
              });

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

      taskSnapshot = ficheroRef.getFile(localFile)
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
               }).addOnCanceledListener(new OnCanceledListener() {
                     @Override
                     public void onCanceled() {
                        progressDownload.dismiss();
                     }
      });

   }

   public static void saveLabelledPhotosInCSVFile(Activity activity, List<List<String>> listLabels /*, char separator*/){
      /**
       * Dado el id de la colección photos, lee sus etiquetas y coordenadas por medio de un listener
       * @param listLabels: lista anidada que contiene la información de cada etiqueta
       * @param separator: permite delimitar cada celda, en la mayoría de los casos se usa ','
       *        pero depende de cara región al usar decimales con ','. Excel trabaja con ';'
       */
      try {
         File rootPath = new File(Environment.getExternalStoragePublicDirectory(
                 Environment.DIRECTORY_DOWNLOADS), LOCAL_DIRECTORY);
         if(!rootPath.exists()) {
            rootPath.mkdirs();
         }
         SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyMMddHHmmss");
         String dateString = formatoFecha.format(new Date(System.currentTimeMillis()));
         String fileName = "mascarillas_"+dateString;
         final File localFileCSV = new File(rootPath,fileName+".csv");
         //CSVWriter writer = new CSVWriter(new FileWriter(localFileCSV, true ) ,';');
         CSVWriter writer=new CSVWriter(new OutputStreamWriter(new FileOutputStream(localFileCSV,
                 true), "UTF-8"),';');
         List<String[]> data = new ArrayList<String[]>();

         int maxNumElementsArray = 0;
         for (List<String> label :listLabels){
            int size = label.size();
            String[] labelArray = new String[size];
            for(int i= 0; i < size; i++){
               labelArray[i] = label.get(i);
            }
            if(maxNumElementsArray < size)
               maxNumElementsArray = size;
            data.add(labelArray);
         }

         String[] titles = new String[maxNumElementsArray];
         titles[0] = activity.getString(R.string.id);
         titles[1] = activity.getString(R.string.creation_date);
         titles[2] = activity.getString(R.string.url_Photo);

         //Máximo número de etiquetas es igual al máximo número de elementos en el array
         // sin contar (id, creation_date, urlPhoto) y dividido para 3 (labels, x, y)
         int maxNumLabels = (maxNumElementsArray - 3)/3;
         for(int i=1; i <= maxNumLabels; i++){
            int indexLabels = 3*i;
            titles[indexLabels] = "LABEL"+(i);
            titles[indexLabels+1] = "X"+(i);
            titles[indexLabels+2] = "Y"+(i);
         }

         data.add(0,titles);
         writer.writeAll(data);
         writer.close();
         //Muestra el diálogo de operación exitosa
         String title = activity.getResources().getString(R.string.title_mostrar_dialogo);
         String mensaje = activity.getResources().getString(
                 R.string.message_mostrar_dialogo_etiquetas_exportadas_CSV);
         showDialogFireStorage(activity, title, mensaje, DISMISS_DIALOG);

      } catch (IOException e) {
         e.printStackTrace();
      }

   }

}
