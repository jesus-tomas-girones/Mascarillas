package es.upv.master.android.reconocimientofacial.data;

import android.graphics.Bitmap;

import com.google.firebase.firestore.FirebaseFirestore;

import es.upv.master.android.reconocimientofacial.model.Photo;

public class Firebase {
   static public final String COLLECTION = "photosPrueba"; //"Mascarillas"

   public static void subirFoto(Bitmap bitmap, String name) {

   }


   public static void registrarFoto(final long creation_date, final String id, String url){
      Photo photo = new Photo(creation_date,false, url);
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      db.collection(COLLECTION).document(id).set(photo);
   }

}
