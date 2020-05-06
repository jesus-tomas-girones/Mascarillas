package es.upv.master.android.reconocimientofacial;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import es.upv.master.android.reconocimientofacial.label.LabelActivity;

/**
 * Fragmento con un diálogo personalizado
 */
public class LoginDialog extends DialogFragment {
    private static final String TAG = LoginDialog.class.getSimpleName();
    private EditText passEditText, userEditText;
    public LoginDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    /**
     * Crea un diálogo con personalizado para comportarse
     * como formulario de login
     *
     * @return Diálogo
     */
    public AlertDialog createLoginDialogo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity() );
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_signin, null);

        builder.setView(v);
        Button signup = (Button) v.findViewById(R.id.crear_boton);
        Button signin = (Button) v.findViewById(R.id.entrar_boton);
        CheckBox showPass = v.findViewById(R.id.show_password_check);
        userEditText = v.findViewById(R.id.nombre_input);
        passEditText = v.findViewById(R.id.contrasena_input);
        showPass.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    passEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                else
                    passEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        signup.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Crear Cuenta...
                        dismiss();
                    }
                }
        );

        signin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Loguear...
                        //if(isValitedInput()){
                            Intent i = new Intent(getContext(), EvaluadorActivity.class);
                            startActivity(i);
                        //}
                        dismiss();
                    }
                }

        );

        return builder.create();
    }


    private boolean isValitedInput(){
        String user = userEditText.getText().toString();
        String password = passEditText.getText().toString();
        if(user.isEmpty() || password.isEmpty()){
            return false;
        }
       return true;
    }

}
