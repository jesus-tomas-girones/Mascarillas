package es.upv.master.android.reconocimientofacial.ui.label;


import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.model.Photo;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


public class ListLabelAdapter extends
        FirestoreRecyclerAdapter<Photo, ListLabelAdapter.ViewHolder> {
    private Context context;
    String labels = "";
    protected View.OnClickListener onClickListener;
    protected View.OnClickListener clickListener;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ListLabelAdapter(Context context,
                            @NonNull FirestoreRecyclerOptions<Photo> options) {
        super(options);
        this. context = context.getApplicationContext();
    }

    @Override
    protected void onBindViewHolder(@NonNull final ListLabelAdapter.ViewHolder holder,
                                    int position, @NonNull Photo photo) {
        CharSequence prettyTime = DateUtils.getRelativeDateTimeString( context, photo.getCreation_date(),
                DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
        holder.creation_date.setText(prettyTime);
        //Si la foto está etiquetada muestra todas las etiquetas caso contrario "false"
        boolean islabelled = photo.isLabelled();
        if(islabelled){
            String labels = loadLabelsName( position);
            holder.etiqueta.setText(labels);
        }else{
        holder.etiqueta.setText("");
        }
        Glide.with(context)
            .load(photo.getUrlPhoto())
            .placeholder(R.drawable.mask_frontal)
            .into(holder.imgPhoto);
        holder.itemView.setOnClickListener(onClickListener);
    }

    public String loadLabelsName(int position){
        String labels = "";
        for(int i =1; i<=9; i++){
            String label = (String) getSnapshots().getSnapshot(position).get("label"+i);
            if(label != null){
                labels = labels +label + "\n";
            }
        }
        if (labels.isEmpty()) labels = "CORECTA";
        return labels;
    }

    @NonNull
    @Override
    public ListLabelAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_list, parent, false);
        view.setOnClickListener(onClickListener);
        return new ListLabelAdapter.ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public final ImageView imgPhoto;
        public final TextView creation_date;
        public final TextView etiqueta;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imgPhoto = (ImageView) itemView.findViewById(R.id.imagePhoto);
            this.creation_date = (TextView) itemView.findViewById(R.id.textCreation_date);
            this.etiqueta = (TextView) itemView.findViewById(R.id.textEtiqueta);
        }
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnClickListener(View.OnClickListener clickListener){
        this.clickListener = clickListener;
    }

    public String getKey(int pos) {
        final String id = super.getSnapshots().getSnapshot(pos).getId();
        return id;
    }


}
