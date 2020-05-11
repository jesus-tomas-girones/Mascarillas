package es.upv.master.android.reconocimientofacial.ui.label;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.data.DataBase;
import es.upv.master.android.reconocimientofacial.model.Photo;


public class ListLabelActivity extends AppCompatActivity {
    private TabLayout tabs;
    private RecyclerView recyclerView;
    FirebaseFirestore db;
    public static ListLabelAdapter adaptador;
    public static final int REQUEST_CODE_NEXT_PHOTO = 1;
    public static int position;
    FirestoreRecyclerOptions<Photo> opciones;
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
        Query query = db.collection(DataBase.COLLECTION)
                .orderBy("creation_date", Query.Direction.DESCENDING)
                .whereEqualTo("labelled",isEvaluated);
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
                Photo photoItem = (Photo) adaptador.getItem(position);
                String idPhoto = adaptador.getSnapshots().getSnapshot(position).getId();
                Intent intent = new Intent(ListLabelActivity.this, LabelActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Id_photo", idPhoto);
                intent.putExtra("Labelled_photo", photoItem.isLabelled());
                intent.putExtra("URL_photo", photoItem.getUrlPhoto());
                //getApplicationContext().startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_NEXT_PHOTO);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE_NEXT_PHOTO  && resultCode  == RESULT_OK) {

                Photo photoItem = (Photo) opciones.getSnapshots().get(position);
                String idPhoto = adaptador.getSnapshots().getSnapshot(position).getId();
                //String id = adaptador.getKey(position);
                //Photo photoItem = (Photo) adaptador.getItem(position);
                Context context = getApplicationContext();
                Intent intent = new Intent(context, LabelActivity.class);
                intent.putExtra("Id_photo", idPhoto);
                intent.putExtra("Labelled_photo", photoItem.isLabelled());
                intent.putExtra("URL_photo", photoItem.getUrlPhoto());
                //context.startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_NEXT_PHOTO);
            }

    }


}
