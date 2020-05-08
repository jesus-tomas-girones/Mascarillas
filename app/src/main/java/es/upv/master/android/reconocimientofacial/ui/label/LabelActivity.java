package es.upv.master.android.reconocimientofacial.ui.label;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import es.upv.master.android.reconocimientofacial.R;

public class LabelActivity extends AppCompatActivity implements View.OnTouchListener {

   String idPhoto;  //id de la colección a modificar
   String nLabel = "1";
   ImageView photo;
   TextView circle1;
   TextView circle2;
   TextView circle3;

   TextView[] circle;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_label);
      photo = findViewById(R.id.photo);
      circle1 = findViewById(R.id.textView1);
      circle2 = findViewById(R.id.textView2);
      circle3 = findViewById(R.id.textView3);
      photo.setOnTouchListener(this);
      //Cargamos la imagen de storage
      Bundle extras = getIntent().getExtras();
      idPhoto = extras.getString("Id_photo");
      //boolean labelPhoto = extras.getBoolean("Label_photoLabel");
      String uriPhoto = extras.getString("URL_photo");
      Glide.with(getApplicationContext())
              .load(uriPhoto)
              .placeholder(R.drawable.mask_frontal)
              .into(photo);
   }

   private TextView getCircle() {
      switch (nLabel) {
         case "1": return circle1;
         case "2": return circle2;
         case "3": return circle3;
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

      setCircle(circle1, -1, -1);
      setCircle(circle2, 0.9f, 0.9f); //NO FUNCIONA
      setCircle(circle3, -1, -1);
   }

   @Override
   protected void onPause() {
      float x1 = getX(circle1);
      float y1 = getY(circle1);
      float x2 = getX(circle2);
      float y2 = getY(circle2);
      float x3 = getX(circle3);
      float y3 = getY(circle3);
      //Guardamos los valores en la base de datos
      //saveLabels();
      super.onPause();
   }

   //void saveLabels
   // int n=1
   // if circle1.getVisivity() == VISIBLE {
   //    saveNewLabel(n, NAME_LABEL[1], getX(circle1), getY(circle1))
   //    n++;
   //}
   // if circle2.getVisivity() == VISIBLE {
   //    saveNewLabel(n, NAME_LABEL[2], getX(circle2), getY(circle2))
   //    n++;
   //}
   // if CIRCLE3.getVisivity() == VISIBLE {
   //    saveNewLabel(n, NAME_LABEL[3], getX(circle3), getY(circle3))
   //    n++;
   //}

}
