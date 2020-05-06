package es.upv.master.android.reconocimientofacial;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import es.upv.master.android.reconocimientofacial.label.LabelActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void start(View v){
        Intent i = new Intent(this, RecognitionActivity.class);
        //Intent i = new Intent(this, LabelActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if (id == R.id.menu_preferencia) {

            return true;
        }

        else if (id == R.id.menu_ser_evaluador) {

            alertDialogLogin();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void alertDialogLogin(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(getString(R.string.title_login_evaluator));
        alertDialog.setMessage(getString(R.string.msg_login_evaluator));

        final EditText input = new EditText(MainActivity.this);


        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_candado);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(getString(R.string.entrar_boton),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String password = input.getText().toString();
                        if (!password.isEmpty()) {
                            String pass = getString(R.string.password_login_evaluator);
                            if (pass.equals(password)) {
                                Toast.makeText(getApplicationContext(),
                                        "Contraseña correcta", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getApplicationContext(), EvaluadorActivity.class);
                                startActivity(i);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.password_error), Toast.LENGTH_SHORT).show();

                            }
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.password_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        alertDialog.setNegativeButton(getString(R.string.cerrar_boton),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

}
