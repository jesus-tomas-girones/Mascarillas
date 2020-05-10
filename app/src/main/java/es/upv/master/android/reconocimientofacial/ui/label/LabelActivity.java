package es.upv.master.android.reconocimientofacial.ui.label;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.data.DataBase;

import static es.upv.master.android.reconocimientofacial.data.DataBase.descargandoDatos;
import static es.upv.master.android.reconocimientofacial.data.DataBase.getCollectionReferencePhotos;
import static es.upv.master.android.reconocimientofacial.data.DataBase.preferencesLabels;


public class LabelActivity extends AppCompatActivity implements View.OnTouchListener {

   String idPhoto;  //id de la colección a modificar
   boolean islabelledPhoto;
   String nLabel = "1";
   ImageView photo;
   TextView[] circle = new TextView[9];
   String[] listLabel;
   Button selectedButton;
   boolean exitWithoutSaving = false;

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
      //Cargo todos las etiquetas desde un recurso array en el array listlabel
      listLabel = getResources().getStringArray(R.array.labels);

      //Cargamos la imagen de URL
      Glide.with(getApplicationContext())
              .load(uriPhoto)
              .placeholder(R.drawable.mask_frontal)
              .into(photo);
   }

   //TODO Esta función ya no es necesario, reemplazar por circle[Integer.valueOf(nLabel)],
   // java no me permite asignarle directamente un elemento del array porque es un valor primitivo, "Error Variable mighr no been initialized"
   private TextView getCircle() {
      int index = Integer.valueOf(nLabel) - 1;
      return circle[index];
   }


   public void onButtonClic(View v) {
      nLabel = (String) v.getTag();
      //TODO Marcar botón como seleccionado, por ejemplo cam
      //Marcar resto de botones como NO seleccionado
      TextView circle = getCircle();
      if (circle.getVisibility() == View.VISIBLE) {
         circle.setVisibility(View.INVISIBLE);
      }
      //Cambio el alfa de los botones pulsados
      ClickedButtonStyle(v);
   }

   public void ClickedButtonStyle(View v){
      if(selectedButton != v.findViewWithTag(v.getTag())){
         if(selectedButton != null)
            selectedButton.setAlpha(1);
         selectedButton = v.findViewWithTag(v.getTag());
         selectedButton.setAlpha(0.7f);
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
      //Pregunto si la foto ha sido etiquetada
      if(islabelledPhoto)
         loadLabels(idPhoto);
   }

   @Override
   protected void onPause() {
      //Guardamos los valores en la base de datos
      if(!exitWithoutSaving)
      saveLabels();
      super.onPause();
   }

   public void saveLabels() {
      for (int i = 0; i < circle.length; i++) {
         //Me aseguro que la foto ha sido etiquetada para eliminar las etiquetas
         if(islabelledPhoto)
         DataBase.releaseLabel(idPhoto, listLabel[i], getX(circle[i]), getY(circle[i]), i+1);
         if (circle[i].getVisibility() == View.VISIBLE) {
            DataBase.updateLabel(idPhoto, listLabel[i], getX(circle[i]), getY(circle[i]), i+1);
         }
      }
   }

   //TODO tratar de pasar esta funcion a Firebase con parámetros adecuados
   //No funciona en Database porque la función loadLabels es asíncrono y al recuperar los datos, no me llegan
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

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_label, menu);
      return true;
      //return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();

      //noinspection SimplifiableIfStatement
      if (id == R.id.menu_label_salir) {
         exitWithoutSaving = true;
         finish();
         return true;
      }

      else if (id == R.id.menu_label_siguiente) {
         //alertDialogLogin();
         Intent i = new Intent(getApplicationContext(), ListLabelActivity.class);
         startActivity(i);
         return true;
      }

      return super.onOptionsItemSelected(item);
   }


}