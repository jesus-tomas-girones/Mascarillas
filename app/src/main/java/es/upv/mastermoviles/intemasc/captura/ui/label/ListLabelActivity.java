package es.upv.mastermoviles.intemasc.captura.ui.label;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import es.upv.mastermoviles.intemasc.captura.R;
import es.upv.mastermoviles.intemasc.captura.data.DataBase;
import es.upv.mastermoviles.intemasc.captura.model.Photo;
import es.upv.mastermoviles.intemasc.captura.ui.preferences.PreferencesActivity;

import static es.upv.mastermoviles.intemasc.captura.ui.MainActivity.alertDialogLogin;
import static es.upv.mastermoviles.intemasc.captura.ui.MainActivity.prefs;

public class ListLabelActivity extends AppCompatActivity {
   private TabLayout tabs;
   private RecyclerView recyclerView;
   FirebaseFirestore db;
   public static ListLabelAdapter adaptador;
   public static final int REQUEST_CODE_NEXT_PHOTO = 1;
   public static final String PREFERENCE_PASSWORD_DOWNLOAD = "download";
   public static int position;
   FirestoreRecyclerOptions<Photo> opciones;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_evaluator);
      //
      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

      recyclerView = findViewById(R.id.recyclerview_photos);
      db = FirebaseFirestore.getInstance();
      listarFotos(false);

      //
      tabs = (TabLayout) findViewById(R.id.tabs);
      String tab1 = getResources().getString(R.string.tab_lista_evaluados);
      String tab0 = getResources().getString(R.string.tab_lista_sin_evaluar);
      tabs.addTab(tabs.newTab().setText(tab0));
      tabs.addTab(tabs.newTab().setText(tab1));
      tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
      tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
         @Override
         public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()) {
               case 0: //Sin Evaluar
                  listarFotos(false);
                  adaptador.startListening();
                  break;
               case 1: //Evaluados
                  listarFotos(true);
                  adaptador.startListening();
                  break;
            }
            //adaptador.notifyDataSetChanged();
         }

         @Override
         public void onTabUnselected(TabLayout.Tab tab) {
         }

         @Override
         public void onTabReselected(TabLayout.Tab tab) {
         }
      });
      //El adaptador escucha desde su creación y deja de hacerlo en su destrucción.
      //De esta forma al entrar en LabelActivity no es destruido
      adaptador.startListening();
   }

/*    @Override public void onStart() { //
        super.onStart(); adaptador.startListening();
    }*/

   @Override
   public void onDestroy() { //Stop
      //super.onStop();
      super.onDestroy();
      adaptador.stopListening();
   }

   public void listarFotos(boolean isEvaluated) {
      Query query = db.collection(DataBase.COLLECTION)
              .orderBy("creation_date", Query.Direction.DESCENDING)
              .whereEqualTo("labelled", isEvaluated);
      opciones = new FirestoreRecyclerOptions
              .Builder<Photo>()
              .setQuery(query, Photo.class)
              .build();
      adaptador = new ListLabelAdapter(this, opciones);
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      recyclerView.setAdapter(adaptador);
      adaptador.setOnItemClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            position = recyclerView.getChildAdapterPosition(view);
            labelPhotoAtPossition(position);
         }
      });
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == REQUEST_CODE_NEXT_PHOTO && resultCode == RESULT_OK) {
         //Si la actividad retorna indicando que queremos pasar a la siguiente foto
         //incrementamos la posición y abrimos la nueva foto
         position++;
         if (position < adaptador.getItemCount()) {
            labelPhotoAtPossition(position);
         }
      }
   }

   void labelPhotoAtPossition(int possition) {
       /**
        * Abre LabelActivity con una foto para etiquetarla
        * @param possition posición en el adaptador de la foto a etiquetar
         */
      Photo photoItem = (Photo) adaptador.getItem(position);
      String idPhoto = adaptador.getSnapshots().getSnapshot(position).getId();
      Intent intent = new Intent(ListLabelActivity.this, LabelActivity.class);
      intent.putExtra("Id_photo", idPhoto);
      intent.putExtra("Labelled_photo", photoItem.isLabelled());
      intent.putExtra("URL_photo", photoItem.getUrlPhoto());
      startActivityForResult(intent, REQUEST_CODE_NEXT_PHOTO);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_evaluator, menu);
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
      if (id == R.id.menu_download) {
         boolean passwDownload = prefs.getBoolean(PREFERENCE_PASSWORD_DOWNLOAD, false);
         if(passwDownload){
            Intent i = new Intent(getApplicationContext(), PreferencesActivity.class);
            startActivity(i);
         }else{
            String password = getString(R.string.password_download);
            alertDialogLogin(this, PREFERENCE_PASSWORD_DOWNLOAD, PreferencesActivity.class,  password);
         }

         return true;
      }
      return super.onOptionsItemSelected(item);
   }

}
