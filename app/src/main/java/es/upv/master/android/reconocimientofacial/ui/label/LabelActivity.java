package es.upv.master.android.reconocimientofacial.ui.label;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.data.DataBase;


public class LabelActivity extends AppCompatActivity implements View.OnTouchListener {

   final static int MAX_LABEL = 9; //Número máximo de etiquetas que se admiten. Limitada por nú,ero de botones
   final static int BUTTON_ID[] = {R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
           R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};
   final static int CIRCLES_ID[] = {R.id.txv_label1, R.id.txv_label2, R.id.txv_label3, R.id.txv_label4,
           R.id.txv_label5, R.id.txv_label6, R.id.txv_label7, R.id.txv_label8, R.id.txv_label9};


   String idPhoto;  //id del documento a modificar
   boolean islabelledPhoto;
   String nLabel = "1";
   ImageView photo;
   TextView[] circle;
   String[] listLabel;
   Button selectedButton;
   boolean exitWithoutSaving = false;
   //Variable que me permiten calcular la diferencia de desplazamiento
   // en la acción onTouch para pasar a la siguiente foto
   private float mX=0, mY=0;
   private boolean tap=false;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_label);
      //Cargamos los parámetros
      Bundle extras = getIntent().getExtras();
      idPhoto = extras.getString("Id_photo");
      islabelledPhoto = extras.getBoolean("Labelled_photo", false);
      String uriPhoto = extras.getString("URL_photo");

      photo = findViewById(R.id.photo);
      photo.setOnTouchListener(this);

      //Cargo todos las etiquetas desde un recurso array en el array listlabel
      listLabel = getResources().getStringArray(R.array.labels);
      if (listLabel.length>MAX_LABEL) {
         Toast.makeText(this,"ERROR: Demasiadas Etiquetas",Toast.LENGTH_LONG).show();
      }

      //Inicializo todos los círculos y botones
      circle = new TextView[listLabel.length];
      for (int i = 0; i < circle.length; i++) {
         circle[i] = findViewById(CIRCLES_ID[i]);
         TextView v = findViewById(BUTTON_ID[i]);
         v.setText(listLabel[i]);
      }
      for (int i = circle.length; i < MAX_LABEL; i++) {
         Button v = findViewById(BUTTON_ID[i]);
         v.setVisibility(View.GONE);
      }

      //Cargamos la imagen de URL
      Glide.with(getApplicationContext())
              .load(uriPhoto)
              .placeholder(R.drawable.mask_frontal)
              .into(photo);
   }

   // Esta función ya no es necesario, reemplazar por circle[Integer.valueOf(nLabel)],
   // java no me permite asignarle directamente un elemento del array porque es un valor primitivo, "Error Variable mighr no been initialized"
   private TextView getCircle() {
      int index = Integer.valueOf(nLabel) - 1;
      return circle[index];
   }

   public void onButtonClic(View v) {
      nLabel = (String) v.getTag();
      TextView circle = getCircle();
      if (circle.getVisibility() == View.VISIBLE) {
         circle.setVisibility(View.INVISIBLE);
      }
      //Cambio el alfa de los botones pulsados
      setClickedButtonStyle(v);
   }

   public void setClickedButtonStyle(View v) {
      if (selectedButton != v.findViewWithTag(v.getTag())) {
         if (selectedButton != null)
            selectedButton.setAlpha(1);
         selectedButton = v.findViewWithTag(v.getTag());
         selectedButton.setAlpha(0.7f);
      }
   }

   @Override
   public boolean onTouch(View v, MotionEvent event) {
      super.onTouchEvent(event);
      float x = event.getX();
      float y = event.getY();
      switch (event.getAction()) {
         case MotionEvent.ACTION_DOWN:
            tap=true;
            break;
         case MotionEvent.ACTION_MOVE:
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dy>6 && dx>6){
               actionNextPhoto();
            }else if(dy>1 && dx>1){
               tap=false;
            }
            break;
         case MotionEvent.ACTION_UP:
            if (tap){
               addLabel(v, event );
            }
      }
      mX=x; mY=y;
      return true;
   }

   private void addLabel(View v, MotionEvent event ){
      TextView circle = getCircle();
      circle.setVisibility(View.VISIBLE);
      circle.setX(event.getX() - circle.getWidth() / 2);
      circle.setY(event.getY() - circle.getHeight() / 2);
      v.performClick();
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
      if (islabelledPhoto)
         loadLabels(idPhoto);
   }

   @Override
   protected void onPause() {
      //Guardamos los valores en la base de datos
      if (!exitWithoutSaving)
         saveLabels();
      super.onPause();
   }

   //Crea listas con etiquetas y sus coordenadas para llamar a DataBase.updateLabel() y almacenarlas
   void saveLabels() {
      List<String> label = new ArrayList<>();
      List<Float> x = new ArrayList<>();
      List<Float> y = new ArrayList<>();
      for (int i = 0; i < listLabel.length; i++) {
         if (circle[i].getVisibility() == View.VISIBLE) {
            label.add(listLabel[i]);
            x.add(getX(circle[i]));
            y.add(getY(circle[i]));
         }
      }
      DataBase.updateLabels(idPhoto, label, x, y);
   }

   //Lee listas con etiquetas de DataBase.loadLabels() y situa los Circle[] según lo leido
   void loadLabels(String id) {
      DataBase.loadLabels(id, new DataBase.LoadLabelsListener() {
                 @Override
                 public void onLoad(List<String> label, List<Double> x, List<Double> y) {
                    for (int i = 0; i < label.size(); i++) {
                       int nLabel = 0;
                       while (nLabel < listLabel.length && !label.get(i).equals(listLabel[nLabel])) nLabel++;
                       if (nLabel >= listLabel.length) {
                          String s= "Etiqueta '" + label.get(i) + "' n encontrada en lista de etiquetas actual";
                          Log.e("MASCARILLA", s); //DOTO crear costante TAG
                          Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                       } else {
                          setCircle(circle[nLabel], x.get(i).floatValue(), y.get(i).floatValue());
                       }
                    }
                 }
          });
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
            } else if (id == R.id.menu_label_siguiente) {
               actionNextPhoto();
               return true;
            }else if (id == R.id.menu_label_aceptar) {
               finish();
               return true;
            }
            return super.onOptionsItemSelected(item);
         }

         private void actionNextPhoto(){
            Intent i = new Intent(getApplicationContext(), ListLabelActivity.class);
            this.setResult(Activity.RESULT_OK, i);
            finish();
         }

      }