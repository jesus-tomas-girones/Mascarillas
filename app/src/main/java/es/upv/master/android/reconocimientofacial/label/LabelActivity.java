package es.upv.master.android.reconocimientofacial.label;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import es.upv.master.android.reconocimientofacial.R;

public class LabelActivity extends AppCompatActivity implements View.OnTouchListener {

   String id;  //id de la colección a modificar
   String nLabel = "1";
   ImageView photo;
   TextView circle1;
   TextView circle2;
   TextView circle3;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      // se nos pasa id como parámetro
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_label);
      photo = findViewById(R.id.photo);
      circle1 = findViewById(R.id.textView1);
      circle2 = findViewById(R.id.textView2);
      circle3 = findViewById(R.id.textView3);
      photo.setOnTouchListener(this);
      //Cargamos la imagen de storage
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
      //Leemos los valores x1, y1, x2, ... de la base de datos
      //Inicializamos circle1, 2 y 3
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
      super.onPause();
   }
}
