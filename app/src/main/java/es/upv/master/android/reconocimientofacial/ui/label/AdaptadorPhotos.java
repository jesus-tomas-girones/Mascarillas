package es.upv.master.android.reconocimientofacial.ui.label;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.model.Photo;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class AdaptadorPhotos extends
        FirestoreRecyclerAdapter<Photo, AdaptadorPhotos.ViewHolder> {
    private Context context;
    protected View.OnClickListener onClickListener;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public AdaptadorPhotos(Context context,
                           @NonNull FirestoreRecyclerOptions<Photo> options) {
        super(options);
        this. context = context.getApplicationContext();
    }

    @Override
    protected void onBindViewHolder(@NonNull AdaptadorPhotos.ViewHolder holder,
                                    int position, @NonNull Photo photo) {
        holder.id.setText("Id: " +photo.getCreation_date());
        holder.etiqueta.setText("Etiquetado: "+ photo.isLabelled());
        Glide.with(context)
            .load(photo.getUrlPhoto())
            .placeholder(R.drawable.mask_frontal)
            .into(holder.imgPhoto);
        holder.itemView.setOnClickListener(onClickListener);
    }

    @NonNull
    @Override
    public AdaptadorPhotos.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_list, parent, false);
        view.setOnClickListener(onClickListener);
        return new AdaptadorPhotos.ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public final ImageView imgPhoto;
        public final TextView id;
        public final TextView etiqueta;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imgPhoto = (ImageView) itemView.findViewById(R.id.imagePhoto);
            this.id = (TextView) itemView.findViewById(R.id.textId);
            this.etiqueta = (TextView) itemView.findViewById(R.id.textEtiqueta);
        }
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

}
