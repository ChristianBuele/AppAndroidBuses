package org.example.proyectofinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracionActivity extends AppCompatActivity {
private Spinner spinner;
private Configuracion configuracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_activitys);
        obtenerDatos();
        spinner=(Spinner)findViewById(R.id.spinner);
        SharedPreferences settings= getApplicationContext().getSharedPreferences("configuracion",0);
        int tiempo=settings.getInt("tiempo",0);
        String [] datos={"10","20","30"};
        ArrayAdapter <String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,datos);
        spinner.setAdapter(adapter);
        if (tiempo==10){
            spinner.setSelection(0);
        }else if(tiempo==20){
            spinner.setSelection(1);
        }else {
            spinner.setSelection(2);
        }

    }

    public void guardaCambios(View view){
        String tiempo=spinner.getSelectedItem().toString();
      //  this.configuracion=new Configuracion();
        //this.configuracion.setTiempoGps(tiempo);
        SharedPreferences settings= getApplicationContext().getSharedPreferences("configuracion",0);
        SharedPreferences.Editor editor=settings.edit();
        editor.putInt("tiempo",Integer.valueOf(tiempo));
        editor.commit();
        Toast.makeText(this,"Cambios Guardados",Toast.LENGTH_SHORT).show();
    }
    public void volver(View view){
        Intent intent = new Intent(this,MainActivity.class);
       // Bundle bundle = new Bundle();
       // bundle.putSerializable("configuracion",this.configuracion);
     //   intent.putExtras(bundle);



        startActivity(intent);
        finish();
        }

    public void obtenerDatos(){
        Bundle datos = getIntent().getExtras();
        if (datos!=null){
            configuracion =(Configuracion)datos.getSerializable("configuracion");
        }
    }
}
