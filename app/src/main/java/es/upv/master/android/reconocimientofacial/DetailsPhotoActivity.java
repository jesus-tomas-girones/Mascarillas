package es.upv.master.android.reconocimientofacial;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailsPhotoActivity extends AppCompatActivity {
    private ImageView photoDetails;
    private TextView txt_id, txt_label;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_photo);
        photoDetails = findViewById(R.id.imgPhotoDetails);
        txt_id = findViewById(R.id.txtIdPhotoDetails);
        txt_label = findViewById(R.id.txtLebelledDetails);

        Bundle extras = getIntent().getExtras();
        Long idPhoto = extras.getLong("Id_photoDetails");
        String uriPhoto = extras.getString("URL_photoDetails");
        boolean labelPhoto = extras.getBoolean("URL_photoDetails");

        txt_id.setText("Id: "+idPhoto);
        txt_label.setText("Label: "+labelPhoto);
        Glide.with(getApplicationContext())
                .load(uriPhoto)
                .placeholder(R.drawable.mask_frontal)
                .into(photoDetails);
    }
}
