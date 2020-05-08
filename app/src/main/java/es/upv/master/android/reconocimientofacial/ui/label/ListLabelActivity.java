package es.upv.master.android.reconocimientofacial.ui.label;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.data.Firebase;
import es.upv.master.android.reconocimientofacial.model.Photo;

//import static es.upv.master.android.reconocimientofacial.ui.take_photo.RecognitionActivity.nombreDirectorioFotos;

public class ListLabelActivity extends AppCompatActivity {
    private TabLayout tabs;
    private RecyclerView recyclerView;
    FirebaseFirestore db;
    public static AdaptadorPhotos adaptador;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluator);
        //

        recyclerView = findViewById(R.id.recyclerview_photos);
        db = FirebaseFirestore.getInstance();
        listarFotos( false);

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
                        listarFotos( false);
                        adaptador.startListening();
                        break;
                    case 1: //Evaluados
                        listarFotos( true);
                        adaptador.startListening();
                        break;
                }
                //adaptador.notifyDataSetChanged();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


    }

    @Override public void onStart() {
        super.onStart(); adaptador.startListening();
    }

    @Override public void onStop() {
        super.onStop(); adaptador.stopListening();
    }

    public void listarFotos(boolean isEvaluated){
        Query query = db.collection(Firebase.COLLECTION)
                .whereEqualTo("labelled",isEvaluated);

        FirestoreRecyclerOptions<Photo> opciones = new FirestoreRecyclerOptions
                .Builder<Photo>()
                .setQuery(query, Photo.class)
                .build();
        adaptador = new AdaptadorPhotos(this, opciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptador);

        adaptador.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildAdapterPosition(view);
                Photo photoItem = (Photo) adaptador.getItem(position);
                String idPhoto = adaptador.getSnapshots().getSnapshot(position).getId();
                String type = String.valueOf(idPhoto.charAt(idPhoto.length()-1));
                Log.d("Nombre Photo", "id: "+idPhoto+" Tipo: "+type);
                Context context = getApplicationContext();
                Intent intent = new Intent(context, LabelActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Id_photo", idPhoto);
                //intent.putExtra("Id_photoLabel", photoItem.getCreation_date());
                //intent.putExtra("Label_photoLabel", photoItem.isLabelled());
                intent.putExtra("URL_photo", photoItem.getUrlPhoto());
                context.startActivity(intent);
            }
        });

    }
}
