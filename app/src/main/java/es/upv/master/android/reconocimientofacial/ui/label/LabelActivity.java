package es.upv.master.android.reconocimientofacial.ui.label;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
   // La imagen dentro del ImageView no siempre ocupa el 100% de la superficie.
   // Las siguientes cuatro variables descriven su esquina superior izquierda y su ancho y alto
   int imgOrigWidth, imgOrigHeight, imgWidth, imgHeight;
   String idPhoto;  //id del documento a modificar
   boolean islabelledPhoto;
   String nLabel = "1";
   ImageView photo;
   TextView[] circle;
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
      islabelledPhoto = extras.getBoolean("Labelled_photo", false);
      String uriPhoto = extras.getString("URL_photo");

      photo = findViewById(R.id.photo);
      photo.setOnTouchListener(this);

      //Cargo todos las etiquetas desde un recurso array en el array listlabel
      listLabel = getResources().getStringArray(R.array.labels);
      if (listLabel.length > MAX_LABEL) {
         Toast.makeText(this, "ERROR: Demasiadas Etiquetas", Toast.LENGTH_LONG).show();
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
      ObtainImgDimensions();
      //TODO Poner progreso circular.
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

   //Variable que me permiten calcular la diferencia de desplazamiento
   // en la acción onTouch para pasar a la siguiente foto
   private float mX = 0, mY = 0;
   private boolean tap = false;

   @Override
   public boolean onTouch(View v, MotionEvent event) {
      super.onTouchEvent(event);
      float x = event.getX();
      float y = event.getY();
      switch (event.getAction()) {
         case MotionEvent.ACTION_DOWN:
            tap = true;
            break;
         case MotionEvent.ACTION_MOVE:
            float dx = x - mX;
            float dy = y - mY;
            if (dx < -12 && Math.abs(dy) < 8) {   //
               actionNextPhoto();
            } else if (Math.abs(dx) > 8 || Math.abs(dy) > 8) {
               tap = false;
            }
            break;
         case MotionEvent.ACTION_UP:
            if (tap) {
               TextView circle = getCircle();
               circle.setVisibility(View.VISIBLE);
               circle.setX(event.getX() - circle.getWidth() / 2);
               circle.setY(event.getY() - circle.getHeight() / 2);
               v.performClick();
            }
      }
      mX = x;
      mY = y;
      return true;
   }

   /** Dado un cículo que marca  la posición de una etiqueta obtenemos x en un rango [0, 1]
    *
    * @param circle TextView que marca la posición de una etiqueta
    * @return coordenada x en un rango [0, 1]
    */
   private float getX(TextView circle) {
      if (circle.getVisibility() == View.INVISIBLE)
         return -1;
      else
         return (circle.getX() - imgOrigWidth + circle.getWidth() / 2) / imgWidth;
   }

   /** Dado un cículo que marca  la posición de una etiqueta obtenemos y en un rango [0, 1]
    *
    * @param circle TextView que marca la posición de una etiqueta
    * @return coordenada y en un rango [0, 1]
    */
   private float getY(TextView circle) {
      if (circle.getVisibility() == View.INVISIBLE)
         return -1;
      else
         return (circle.getY() - imgOrigHeight + circle.getHeight() / 2) / imgHeight;
   }

   /** Situamos en pantalla un círculo que marca la posición de una etiqueta
    *
    * @param circle TextView que queremos situar
    * @param x coordenada x en rango [0, 1]
    * @param y coordenada y en rango [0, 1]
    */
   private void setCircle(TextView circle, float x, float y) {
      if (x < 0 || y < 0) {
         circle.setVisibility(View.INVISIBLE);
      } else {
         circle.setVisibility(View.VISIBLE);
         circle.setX(x * imgWidth + imgOrigWidth - circle.getWidth() / 2);
         circle.setY(y * imgHeight + imgOrigHeight - circle.getHeight() / 2);
      }
   }

/*   @Override
   protected void onResume() {
      super.onResume();
      //Pregunto si la foto ha sido etiquetada
//      if (islabelledPhoto)
//         loadLabels(idPhoto);
   }*/

   @Override
   protected void onPause() {
      //Guardamos los valores en la base de datos
      if (!exitWithoutSaving)
         saveLabels();
      super.onPause();
   }

   /**Crea listas con etiquetas y sus coordenadas para llamar a DataBase.updateLabel() y almacenarlas
    */
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

   /** Lee listas con etiquetas de DataBase.loadLabels() y situa los Circle[] según lo leido
    * @param id identifica la imagen a cargar
    */
   void loadLabels(String id) {
      DataBase.loadLabels(id, new DataBase.LoadLabelsListener() {
         @Override
         public void onLoad(List<String> label, List<Double> x, List<Double> y) {
            for (int i = 0; i < label.size(); i++) {
               int nLabel = 0;
               while (nLabel < listLabel.length && !label.get(i).equals(listLabel[nLabel]))
                  nLabel++;
               if (nLabel >= listLabel.length) {
                  String s = "Etiqueta '" + label.get(i) + "' n encontrada en lista de etiquetas actual";
                  Log.e("MASCARILLA", s); //DOTO crear costante TAG
                  Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
               } else {
                  setCircle(circle[nLabel], x.get(i).floatValue(), y.get(i).floatValue());
               }
            }
            //TODO parar progreso
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
      } else if (id == R.id.menu_label_aceptar) {
         finish();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   private void actionNextPhoto() {
      Intent i = new Intent(getApplicationContext(), ListLabelActivity.class);
      this.setResult(Activity.RESULT_OK, i);
      finish();
   }

   private Handler handler = new Handler();       // Handler para el temporizador

   private void ObtainImgDimensions() {
      int[] array = getBitmapPositionInsideImageView(photo);
      imgOrigWidth = array[0];
      imgOrigHeight = array[1];
      imgWidth = array[2];
      imgHeight = array[3];
      // La primera llamada imgWidth==imgHeight (2613x2613)
      // La segunda también (1080x1080)
      // La tercera es la buena
      if (imgWidth == imgHeight) {//imgWidth == 1080 && imgHeight == 1080) {
         //Lo intentamos dentro de 200 ms
         handler.postDelayed(
                 new Runnable() {
                    @Override
                    public void run() {
                       ObtainImgDimensions();
                    }
                 },
                 200);
      } else {
         if (islabelledPhoto)
            loadLabels(idPhoto); //Una vez sabemos las dimensiones pasamos a cargar etiquetas
         else ;
            //TODO parar progreso
      }
   }

   /**
    * Returns the bitmap position inside an imageView.
    *
    * @param imageView source ImageView
    * @return 0: left, 1: top, 2: width, 3: height
    */
   public static int[] getBitmapPositionInsideImageView(ImageView imageView) {
      int[] ret = new int[4];

      if (imageView == null || imageView.getDrawable() == null)
         return ret;

      // Get image dimensions
      // Get image matrix values and place them in an array
      float[] f = new float[9];
      imageView.getImageMatrix().getValues(f);

      // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
      final float scaleX = f[Matrix.MSCALE_X];
      final float scaleY = f[Matrix.MSCALE_Y];

      // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
      final Drawable d = imageView.getDrawable();
      final int origW = d.getIntrinsicWidth();
      final int origH = d.getIntrinsicHeight();

      // Calculate the actual dimensions
      final int actW = Math.round(origW * scaleX);
      final int actH = Math.round(origH * scaleY);
      ret[2] = actW;
      ret[3] = actH;
      // Get image position
      // We assume that the image is centered into ImageView
      int imgViewW = imageView.getWidth();
      int imgViewH = imageView.getHeight();

      int top = (int) (imgViewH - actH) / 2;
      int left = (int) (imgViewW - actW) / 2;
      ret[0] = left;
      ret[1] = top;
      return ret;
   }

}