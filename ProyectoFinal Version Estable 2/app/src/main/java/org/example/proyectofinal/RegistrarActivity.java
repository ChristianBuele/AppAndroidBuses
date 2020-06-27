package org.example.proyectofinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RegistrarActivity extends AppCompatActivity implements Serializable {
    private Usuario nuevoUsuario;
    private EditText etNombre;
    private EditText etApellido;
    private EditText etCorreo;
    private EditText etContra;
    private EditText etConfirm;
    private EditText etCelular;

    private String nombre;
    private String apellido;
    private String correo;
    private String contra;
    private String confirm;
    private String celular;

    private Button enviar;

    FirebaseAuth auth;
    DatabaseReference bd;
    String idUsuarioRegistrado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);
        etNombre=(EditText) findViewById(R.id.editText9);
        etApellido=(EditText) findViewById(R.id.editText7);
        etCorreo=(EditText) findViewById(R.id.editText8);
        etContra=(EditText) findViewById(R.id.editText10);
        etConfirm=(EditText) findViewById(R.id.editText);
        etCelular=(EditText) findViewById(R.id.editText11);
        enviar=(Button)findViewById(R.id.btnEnviar) ;
        auth=FirebaseAuth.getInstance();
        bd= FirebaseDatabase.getInstance().getReference();
        recibirDatos();
    }
    public void recibirDatos(){
        Bundle datos = getIntent().getExtras();
        if (datos!=null){
            nuevoUsuario=(Usuario)datos.getSerializable("usuario");

        }
    }

    public void enviar(View view){
        nombre=etNombre.getText().toString();
        apellido=etApellido.getText().toString();
        correo=etCorreo.getText().toString();
        contra=etContra.getText().toString();
        confirm=etConfirm.getText().toString();
        celular=etCelular.getText().toString();

        if(!nombre.isEmpty() && !apellido.isEmpty() && !correo.isEmpty() && !contra.isEmpty() && !confirm.isEmpty() && !celular.isEmpty()){
            if(contra.length()>=6){
                if(contra.equals(confirm)){
                    if(celular.length()==10){
                        try {
                            Integer.parseInt(celular);
                        } catch (NumberFormatException nfe){
                            Toast.makeText(this, "El celular debe contener unicamente digitos", Toast.LENGTH_SHORT).show();
                        }
                        finally {
                            iniciarRegistro();
                        }
                    }
                    else{
                        Toast.makeText(this, "El celular debe tener 10 digitos", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(this, "Las contraseñas deben coincidir", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "No ha rellenado todos los campos", Toast.LENGTH_SHORT).show();
        }

    }

    public void iniciarRegistro(){
        auth.createUserWithEmailAndPassword(correo,contra).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String id= auth.getCurrentUser().getUid();
                    idUsuarioRegistrado=id;
                    Map<String,Object> map=new HashMap<>();
                    map.put("nombre",nombre);
                    map.put("apellido",apellido);
                    map.put("correo",correo);
                    map.put("contrasenia",contra);
                    map.put("celular",celular);
                    map.put("id",id);
                    map.put("isConductor","false");
                    bd.child("usuarios").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if(task2.isSuccessful()){
                                Intent intent= new Intent(RegistrarActivity.this, MainActivity.class);
                                Toast.makeText(RegistrarActivity.this,"Usuario Registrado",Toast.LENGTH_SHORT).show();
                               /* nuevoUsuario= new Usuario(nombre,0.0,0.0);
                                nuevoUsuario.idRegistro=idUsuarioRegistrado;
                                Log.e("Usuario es ",":"+nuevoUsuario.getUsuario());
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("usuario",nuevoUsuario);
                                intent.putExtras(bundle);*/
                                startActivity(intent);
                                 finish();
                            }
                            else {
                                Toast.makeText(RegistrarActivity.this, "Error al añadir usuario a la base de datos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(RegistrarActivity.this, "No se pudo registrar el usuario, revisa el correo", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
