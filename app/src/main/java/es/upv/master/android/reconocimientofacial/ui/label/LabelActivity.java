package es.upv.master.android.reconocimientofacial.ui.label;

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
import es.upv.master.android.reconocimientofacial.data.Firebase;

import static es.upv.master.android.reconocimientofacial.data.Firebase.COLLECTION;

public class LabelActivity extends AppCompatActivity implements View.OnTouchListener {

   String idPhoto;  //id de la colección a modificar
   String nLabel = "1";
   ImageView photo;
   TextView circle1;
   TextView circle2;
   TextView circle3;

   TextView[] circle = new TextView[9];
   LinearLayout layoutCircles;
   ArrayList<String> listLabel;
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_label);
      photo = findViewById(R.id.photo);
      //Inicializo todos los círculos
      int circlesID[] = {R.id.txv_label1, R.id.txv_label2, R.id.txv_label3, R.id.txv_label4, R.id.txv_label5,
              R.id.txv_label6, R.id.txv_label7, R.id.txv_label8, R.id.txv_label9};

      for(int i=0; i<circle.length; i++){
         circle[i] = findViewById(circlesID[i]);
      }
      //Cargo todos las etiquetas
      listLabels();

      photo.setOnTouchListener(this);
      //Cargamos la imagen de storage
      Bundle extras = getIntent().getExtras();
      idPhoto = extras.getString("Id_photo");
      boolean labelPhoto = extras.getBoolean("Labelled_photo");
      String uriPhoto = extras.getString("URL_photo");
      Glide.with(getApplicationContext())
              .load(uriPhoto)
              .placeholder(R.drawable.mask_frontal)
              .into(photo);

   }

   private TextView getCircle() {
      int index = Integer.valueOf(nLabel) - 1;
      for(int i=0; i<circle.length; i++){
         if(index == i) return circle[i];
      }
      return null;
   }

   public void onButtonClic(View v) {
      nLabel = (String) v.getTag();
      //Marcar botón como seleccionado, por ejemplo cam
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

      //Leer de la colección el id idPhoto
      //Leemos los valores label1, x1, y1, x2, ... de la base de datos
      //Inicializamos circle1, 2 y 3

//     bucle n 1..3 hasta que no exista el campo
//       String label = leer de el registro "label"+n
//       float  x = leer de el registro "x"+n
//       float  y = leer de el registro "y"+n
//       nLabel = "no simetrico" -> "1" "mal ajustada" -> "2"
//       TextView circle = getCircle()
//       circle.setVisivility(VISIBLE)
//       setCircle(circle, x, y)

//      setCircle(circle1, -1, -1);
//      setCircle(circle2, 0.9f, 0.9f); //NO FUNCIONA
//      setCircle(circle3, -1, -1);
       getLabels();
   }

   public void getLabels(){
       FirebaseFirestore db = FirebaseFirestore.getInstance();
       DocumentReference PhotoRef =  db.collection(COLLECTION).document(idPhoto);
       for(int i=0; i < circle.length; i++){
           final int index = i+1;
           Task<DocumentSnapshot> query = PhotoRef.get()
                   .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
               @Override
               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                   if (task.isSuccessful()) {
                        Double x = task.getResult().getDouble("x"+index);
                        Double y = task.getResult().getDouble("y"+index);
                       Log.d("ETIQUETAS","x" + index + ": " + x + ",y" + index + ": " + y);
                       if(x != null && y != null){
                           double valorX =  x;
                           double valorY = y;
                           setCircle(circle[index-1],(float)valorX,(float)valorY);
                       }
                   }
               }
           });


       }
   }

   @Override
   protected void onPause() {
      //Guardamos los valores en la base de datos
      saveLabels();
      super.onPause();
   }

   public void saveLabels(){
      FirebaseFirestore db = FirebaseFirestore.getInstance();
      DocumentReference PhotoRef =  db.collection(COLLECTION).document(idPhoto);
      for(int i=0; i < circle.length; i++){
         if(circle[i].getVisibility() == View.VISIBLE){
            Map<String, Object> dataLabel = new HashMap<>();
            int index = i+1;
            dataLabel.put("label"+index, listLabel.get(i));
            dataLabel.put("x"+index ,getX(circle[i]));
            dataLabel.put("y"+index,getY(circle[i]));
            //dataLabel.put("number_label", index);
            PhotoRef.update(dataLabel);
            //PhotoRef.collection("LABEL").document("label"+index).set(dataLabel);
         }
      }
      PhotoRef.update("labelled", true);
   }

   public void listLabels(){
      int[] labelResources = {R.string.label_1, R.string.label_2,  R.string.label_3, R.string.label_4, R.string.label_5,
              R.string.label_6, R.string.label_7, R.string.label_8, R.string.label_9};
      listLabel = new ArrayList<String>();
      for(int i=0; i < labelResources.length; i++){
         String label = getResources().getString(labelResources[i]);
         listLabel.add(i, label);
      }
   }

}
