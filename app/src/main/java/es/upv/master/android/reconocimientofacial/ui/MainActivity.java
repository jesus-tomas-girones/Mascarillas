package es.upv.master.android.reconocimientofacial.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import es.upv.master.android.reconocimientofacial.ui.preferences.PreferencesActivity;
import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.ui.take_photo.TakePhotoActivity;
import es.upv.master.android.reconocimientofacial.ui.label.ListLabelActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager. getDefaultSharedPreferences(this);
    }

    public void start(View v){
        Intent i = new Intent(this, TakePhotoActivity.class);
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
            Intent i = new Intent(getApplicationContext(), PreferencesActivity.class);
            startActivity(i);
            return true;
        }

        else if (id == R.id.menu_ser_evaluador) {
            boolean switchPass = prefs.getBoolean("passwordSwitch", false);
            if(switchPass){
                Intent i = new Intent(getApplicationContext(), ListLabelActivity.class);
                startActivity(i);
            }else{
                alertDialogLogin();
            }

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
                                //Escribo la contraesña en preferencias
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("password", true);
                                editor.commit();
                                Intent i = new Intent(getApplicationContext(), ListLabelActivity.class);
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
