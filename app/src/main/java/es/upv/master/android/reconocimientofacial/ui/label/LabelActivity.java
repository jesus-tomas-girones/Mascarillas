package es.upv.master.android.reconocimientofacial.ui.label;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.data.DataBase;

import static es.upv.master.android.reconocimientofacial.data.DataBase.getCollectionReferencePhotos;
import static es.upv.master.android.reconocimientofacial.data.DataBase.preferencesLabels;


public class LabelActivity extends AppCompatActivity implements View.OnTouchListener {

   String idPhoto;  //id de la colección a modificar
   boolean islabelledPhoto;
   String nLabel = "1";
   ImageView photo;
   TextView[] circle = new TextView[9];
   ArrayList<String> listLabel;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_label);
      //Cargamos los parámetros
      Bundle extras = getIntent().getExtras();
      idPhoto = extras.getString("Id_photo");
      islabelledPhoto = extras.getBoolean("Labelled_photo",false);
      String uriPhoto = extras.getString("URL_photo");

      photo = findViewById(R.id.photo);
      photo.setOnTouchListener(this);

      //Inicializo todos los círculos
      int circlesID[] = {R.id.txv_label1, R.id.txv_label2, R.id.txv_label3, R.id.txv_label4, R.id.txv_label5,
              R.id.txv_label6, R.id.txv_label7, R.id.txv_label8, R.id.txv_label9};
      for (int i = 0; i < circle.length; i++) {
         circle[i] = findViewById(circlesID[i]);
      }
      //TODO cambiar - Cargo todos las etiquetas
      listLabels();

      //Cargamos la imagen de URL
      Glide.with(getApplicationContext())
              .load(uriPhoto)
              .placeholder(R.drawable.mask_frontal)
              .into(photo);
   }

   //TODO Esta función ya no es necesario, reemplazar por circle[Integer.valueOf(nLabel)]
   private TextView getCircle() {
      int index = Integer.valueOf(nLabel) - 1;
      for (int i = 0; i < circle.length; i++) {
         if (index == i) return circle[i];
      }
      return null;
   }

   public void onButtonClic(View v) {
      nLabel = (String) v.getTag();
      //TODO Marcar botón como seleccionado, por ejemplo cam
      //Marcar resto de botones como NO seleccionado
      TextView circle = getCircle();
      if (circle.getVisibility() == View.VISIBLE) {
         circle.setVisibility(View.INVISIBLE);
      }
   }

   @Override
   public boolean onTouch(View v, MotionEvent event) {
      TextView circle = getCircle();
      circle.setVisibility(View.VISIBLE);
      circle.setX(event.getX() - circle.getWidth() / 2);
      circle.setY(event.getY() - circle.getHeight() / 2);
      v.performClick();
      return false;
   }

   private float getX(TextView circle) {
      if (circle.getVisibility() == View.INVISIBLE)
         return -1;
      else
         return (circle.getX() + circle.getWidth() / 2) / photo.getWidth();
   }

   private float getY(TextView circle) {
      if (circle.getVisibility() == View.INVISIBLE)
         return -1;
      else
         return (circle.getY() + circle.getHeight() / 2) / photo.getHeight();
   }

   private void setCircle(TextView circle, float x, float y) {
      if (x < 0 || y < 0) {
         circle.setVisibility(View.INVISIBLE);
      } else {
         circle.setVisibility(View.VISIBLE);
         circle.setX(x * photo.getWidth() - circle.getWidth() / 2); //NO FUNCIONA photo.getWidth ES 0
         circle.setY(y * photo.getHeight() - circle.getHeight() / 2);
      }
   }

   @Override
   protected void onResume() {
      super.onResume();
      if(islabelledPhoto)
         loadLabels(idPhoto);
   }

   @Override
   protected void onPause() {
      //Guardamos los valores en la base de datos
      saveLabels();
      super.onPause();
   }

   public void saveLabels() {
      for (int i = 0; i < circle.length; i++) {
         //Primero me aseguro que la foto ha sido etiquetada para eliminar las etiquetas
         if(islabelledPhoto)
         DataBase.releaseLabel(idPhoto, listLabel.get(i), getX(circle[i]), getY(circle[i]), i+1);
         if (circle[i].getVisibility() == View.VISIBLE) {
            DataBase.updateLabel(idPhoto, listLabel.get(i), getX(circle[i]), getY(circle[i]), i+1);
         }
      }
   }

   //TODO tratar de pasar esta funcion a Firebase con parámetros adecuados
   //No funciona porque loadLabelsToPreference es asíncrono
   public void addLabelsToPhotos() {
      DataBase.loadLabelsToPreference(getApplicationContext(), idPhoto);
      for(int i = 1; i<=9; i++){
         String label = preferencesLabels.getString("label"+i, null);
         if(label != null){
            double x = preferencesLabels.getFloat("x"+i, 0);
            double y = preferencesLabels.getFloat("y"+i, 0);
            setCircle(circle[i-1], (float)x, (float)y);
         }
      }

   }

   public void loadLabels(String id) {
      DocumentReference PhotoRef = getCollectionReferencePhotos().document(id);
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
                          setCircle(circle[index-1], (float)x, (float)y);
                       }
                    }
                 }
              });
          }
   }

   //TODO OPCIONAL Podrías eliminar el método listLabels() si defines directamente un recurso array.string:
/* https://stackoverflow.com/questions/23321449/how-to-list-android-string-resources-in-an-array
   define your array in array.xml:
   <?xml version="1.0" encoding="utf-8"?>
<resources>
    <string-array name="labels">
        <item>barba</item>
        <item>goma mal ...</item>
    </string-array>
 </resources>
   then try this:
   String[] listLabel = getResources().getStringArray(
           R.array.dlabels);
   */
   public void listLabels() {
      int[] labelResources = {R.string.label_1, R.string.label_2, R.string.label_3, R.string.label_4, R.string.label_5,
              R.string.label_6, R.string.label_7, R.string.label_8, R.string.label_9};
      listLabel = new ArrayList<String>();
      for (int i = 0; i < labelResources.length; i++) {
         String label = getResources().getString(labelResources[i]);
         listLabel.add(i, label);
      }
   }

}