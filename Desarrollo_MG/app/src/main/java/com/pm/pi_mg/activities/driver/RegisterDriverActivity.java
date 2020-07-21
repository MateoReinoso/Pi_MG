package com.pm.pi_mg.activities.driver;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.pm.pi_mg.R;
import com.pm.pi_mg.includes.MyToolbar;
import com.pm.pi_mg.models.Driver;
import com.pm.pi_mg.providers.AuthProvider;
import com.pm.pi_mg.providers.DriverProvider;

import dmax.dialog.SpotsDialog;

public class RegisterDriverActivity extends AppCompatActivity {

    AuthProvider mAuthProvider;
    DriverProvider mDriverProvider;

    AlertDialog mDialog;

    //instanciar vistas
    Button mButtonRegister;

    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputVehicleBrand;
    TextInputEditText mTextInputVehiclePlate;
    TextInputEditText mTextInputPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        MyToolbar.show(this, "Registro de Repartidor", true);

        mAuthProvider = new AuthProvider();
        mDriverProvider = new DriverProvider();

        mDialog = new SpotsDialog.Builder().setContext(RegisterDriverActivity.this).setMessage("Cargando..").build();

        mButtonRegister = findViewById(R.id.btnRegister);

        mTextInputPassword = findViewById(R.id.textInputPassword);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputName = findViewById(R.id.textInputName);
        mTextInputVehicleBrand = findViewById(R.id.textInputVehicleBrand);
        mTextInputVehiclePlate = findViewById(R.id.textInputVehiclePlate);

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegister();
            }
        });
    }

    void clickRegister() {
        final String name = mTextInputName.getText().toString();
        final String email = mTextInputEmail.getText().toString();
        final String vehicleBrand = mTextInputVehicleBrand.getText().toString();
        final String vehiclePlate = mTextInputVehiclePlate.getText().toString();
        final String password = mTextInputPassword.getText().toString();

        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !vehicleBrand.isEmpty() && !vehiclePlate.isEmpty()){
            if(password.length() >= 6){
                mDialog.show();
                register(name, email, vehicleBrand, vehiclePlate, password);
            }
            else{
                Toast.makeText(this, "La contraseña debe ser mayor a 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    void register(final String name, final String email, final String vehicleBrand, final String vechiclePlate, String password){
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Driver driver = new Driver(id, name, email, vehicleBrand, vechiclePlate);
                    create(driver);
                }
                else{
                    Toast.makeText(RegisterDriverActivity.this, "No se pudo registrar su usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void create(Driver driver){
        mDriverProvider.create(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(RegisterDriverActivity.this, MapDriverActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterDriverActivity.this, "No se pudo crear su registro.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}