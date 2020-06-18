package org.example.proyectofinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class IngresarActivity extends AppCompatActivity implements Serializable {
    private Button ingresarB;
    private EditText correo;
    private EditText contra;

    private FirebaseAuth auth;
    private DatabaseReference baseDATOS;
    private String email="";
    private String contras="";
    private Logeos data;
    Usuario nuevoUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingresar);
        ingresarB=(Button) findViewById(R.id.button4);
        correo=(EditText) findViewById(R.id.editText2);
        contra=(EditText) findViewById(R.id.editText3);
        auth=FirebaseAuth.getInstance();


    }

    public void ingresarMap(View view){
        email=correo.getText().toString();
        contras=contra.getText().toString();
        if(!email.isEmpty() && !contras.isEmpty()){
            loginbd();
        }
        else{
            Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
        }

    }

    public void loginbd(){
        auth.signInWithEmailAndPassword(email,contras).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    baseDATOS= FirebaseDatabase.getInstance().getReference();
                    final String idEntrada=auth.getUid();
                    baseDATOS.child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.e("Dentro BUSCANDO USUARIO:"+idEntrada," Apellido:");
                            String usuario="";
                            String apellido="";
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                                Log.e("Dentro for BUSCANDO USUARIO:"+idEntrada," Apellido:");
                                data=snapshot.getValue(Logeos.class);
                                String id=data.getId();
                                Log.e("Usuario:"+id," Apellido:"+data.getId());
                                try{
                                    if(id.equals(idEntrada)){
                                        Log.e("Usuario:"+id," Apellido:"+data.getNombre());
                                        usuario=data.getNombre();
                                    }

                                }catch (Exception e){
                                    Log.e("Excepcion:",e.getMessage());
                                }
                            }

                            nuevoUsuario =new Usuario(usuario,0.0,0.0);
                            nuevoUsuario.idRegistro= usuario;
                            nuevoUsuario.apellido=apellido;
                            Log.e("Usuario:"+nuevoUsuario.getUsuario()," Apellido:"+nuevoUsuario.apellido);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                 Intent intent= new Intent(IngresarActivity.this, MainActivity.class);
                   //Bundle bunble = new Bundle();
               //    bunble.putSerializable("usuario",nuevoUsuario);
                    //bunble.putSerializable("id",idEntrada);
                  // intent.putExtras(bunble);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(IngresarActivity.this, "No se pudo iniciar sesion, compruebe los datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void CargarUsuario(final String idEntrada){
        Log.e("BUSCANDO USUARIO:"+idEntrada," Apellido:");

        if (nuevoUsuario==null){
           // CargarUsuario(idEntrada);
        }
    }

}
