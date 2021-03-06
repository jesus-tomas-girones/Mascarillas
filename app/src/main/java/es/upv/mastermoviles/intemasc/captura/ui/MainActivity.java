package es.upv.mastermoviles.intemasc.captura.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import es.upv.mastermoviles.intemasc.captura.R;
import es.upv.mastermoviles.intemasc.captura.ui.take_photo.TakePhotoActivity;
import es.upv.mastermoviles.intemasc.captura.ui.label.ListLabelActivity;

import android.app.Activity;
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
    private final String PREFERENCE_PASSWORD_LOGIN = "password_login";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager. getDefaultSharedPreferences(this);
    }

    public void start(View v){
        Intent i = new Intent(this, TakePhotoActivity.class);
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

        if (id == R.id.menu_ser_evaluador) {
            boolean passwLogin = prefs.getBoolean(PREFERENCE_PASSWORD_LOGIN, false);
            if(passwLogin){
                Intent i = new Intent(getApplicationContext(), ListLabelActivity.class);
                startActivity(i);
            }else{
                alertDialogLogin(this,PREFERENCE_PASSWORD_LOGIN,
                        ListLabelActivity.class, "mascarilla1234");
            }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void alertDialogLogin(final Activity activity, final String preference_password,
                    final Class nextActivity, final String password){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(activity. getString(R.string.title_login_evaluator));
        alertDialog.setMessage(activity. getString(R.string.msg_login_evaluator));

        final EditText input = new EditText(activity);


        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_candado);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(activity. getString(R.string.entrar_boton),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String passwordInput = input.getText().toString();
                        if (!passwordInput.isEmpty()) {
                            if (password.equals(passwordInput)) {
                                Toast.makeText(activity,
                                        "Contraseña correcta", Toast.LENGTH_SHORT).show();
                                //Usuario autentificado guardo el estado a true para que no vuelva a pedir contraseña
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean(preference_password, true);
                                editor.commit();

                                Intent i = new Intent(activity, nextActivity);
                                activity.startActivity(i);
                            } else {
                                Toast.makeText(activity,
                                        activity.getString(R.string.password_error), Toast.LENGTH_SHORT).show();

                            }
                        }else{
                            Toast.makeText(activity,
                                    activity.getString(R.string.password_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        alertDialog.setNegativeButton(activity. getString(R.string.cerrar_boton),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

}
