package org.example.proyectofinal;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, Serializable, SensorEventListener {
//   Usuario usuarioLogeago=new Usuario("Christian PC",0.0,0.0);
Usuario usuarioLogeago =null;
    private FusedLocationProviderClient mFusedLocationClient;
    private DatabaseReference mDataBase;
    private static final int CODIGO_PERMISOS_UBICACION = 1;
    private ImageButton btnMapa;//usuarioooo
    public ArrayList<Usuario> usuarios =new ArrayList<>();
    private FirebaseAuth mauth;
    ////////////////// sesores
    SensorManager sensorManager;
    private Configuracion configuracion=null;
    Sensor sensor;
    TextView pasos,distancia,nomUsuarioSesion;
    ImageButton mapaImagen;
    boolean running=false;
    float pasosAlmacenados=0;
    boolean band=true;
    ////botones
    MenuItem iniciarS;
    MenuItem cerrarS;
    MenuItem config;
    MenuItem regis;
    ArrayList<LatLng> marcadorReal=new ArrayList<>();
    //////////////////fin sensores
    public void botonesEventos(){

        this.btnMapa.setOnClickListener(this);
    }
    public void sensores(){///////agreergarrrrrr
        pasos=(TextView)findViewById(R.id.pasos);
        distancia=(TextView)findViewById(R.id.recorrido);
        this.nomUsuarioSesion=(TextView)findViewById(R.id.NomUsuario);
        this.mapaImagen=(ImageButton)findViewById(R.id.btnMapa);
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMapa=(ImageButton)findViewById(R.id.btnMapa);
        sensores();//agregarrrrrrrrrrrrr
        recibirDatos();
        permisosApp();
        botonesEventos();
      baseDATOS= FirebaseDatabase.getInstance().getReference();
        mDataBase= FirebaseDatabase.getInstance().getReference();
        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(this);

        this.mapaImagen.setEnabled(false);
       sesion(usuarioLogeago);
        SharedPreferences settings= getApplicationContext().getSharedPreferences("configuracion",0);
        SharedPreferences.Editor editor=settings.edit();
        int tiempo=settings.getInt("tiempo",0);
        if (tiempo==0){
            editor.putInt("tiempo",10);
            editor.commit();
        }
    }
    ///sesion verificacion
    public void sesion(Usuario usuarioLogeago) {
        if (usuarioLogeago != null) {
            this.nomUsuarioSesion.setText(usuarioLogeago.getUsuario());
            this.mapaImagen.setEnabled(true);
          //  creatBotonesMenu(men);
            //Toast.makeText(this,"ya vale", Toast.LENGTH_LONG).show();
            ActualizarGPS();
         //   Toast.makeText(this,"GPS ACTIVADOaaaaaaaaaaaa",Toast.LENGTH_SHORT);
             this.nomUsuarioSesion.setText(usuarioLogeago.getUsuario());
           // creatBotonesMenu(men);
        }else{
            this.mapaImagen.setEnabled(false);
        }

    }
    int tiempoActualizacion=10;
    public void ActualizarGPS() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Sin permisos",Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      //  Toast.makeText(this,"GPS ACTIVADO1",Toast.LENGTH_SHORT).show();
       /*int tiempo=0;
        if (configuracion!=null){
            tiempo=Integer.parseInt(configuracion.getTiempoGps())*1000;
            Log.e("Tiempo de "+tiempo," segundos");
        }else{
            tiempo=10000;
        }*/
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,tiempoActualizacion,0,locationListener);///TIEMPO DE ACTUALIZACION
        //Toast.makeText(this,"GPS ACTIVADO",Toast.LENGTH_SHORT).show();
    }
boolean registradorPos=false;
    Double recorridoMetros=0.0;
    public void actua(Location location){
       // Toast.makeText(this,"Moviendose",Toast.LENGTH_LONG);
        if (location != null ) {
            Double latActual=location.getLatitude();
            Double longActual=location.getLongitude();
            usuarioLogeago.distanciaRecorrida=recorridoMetros;
            Log.e("Latitud:"+usuarioLogeago.getLatitud()+" lONGITUD:"+usuarioLogeago.getLongitud()," Nombre:"+usuarioLogeago.getUsuario()+" recorrido de "+recorridoMetros);
            if (latActual.equals(usuarioLogeago.getLatitud())==false && longActual.equals(usuarioLogeago.getLongitud())==false){
                if (this.usuarioLogeago.getLongitud()==0.0 && this.usuarioLogeago.getLatitud()==0.0 && usuarioLogeago.distanciaRecorrida==0.0){
                    Map<String,Object> latlang= new HashMap<>();
                    latlang.put("usuario", usuarioLogeago.getUsuario());
                    latlang.put("latitud",location.getLatitude());
                    latlang.put("longitud",location.getLongitude());
                    distancia.setText(String.valueOf(usuarioLogeago.distanciaRecorrida));
                    usuarioLogeago.setLatitud(location.getLatitude());//agregar
                    usuarioLogeago.setLongitud(location.getLongitude());///agregar
                    mDataBase.child("ubicaciones").push().setValue(latlang);
                    registradorPos=true;
                }else{
                    Map<String,Object> latlang= new HashMap<>();
                    latlang.put("usuario", usuarioLogeago.getUsuario());
                    latlang.put("latitud",location.getLatitude());
                    latlang.put("longitud",location.getLongitude());
                    recibirDatos();
                  //  marcadorReal.add(new LatLng(location.getLatitude(),location.getLongitude()));
                    OperacionesRecorrido operacionesRecorrido=new OperacionesRecorrido(usuarioLogeago,latActual,longActual);
                    operacionesRecorrido.start();
                    while (operacionesRecorrido.isAlive()){

                    }
                    DecimalFormat formato1 = new DecimalFormat("#.00");
                    String etiqueta="m";
                    if(usuarioLogeago.distanciaRecorrida!=0){
                        recorridoMetros=usuarioLogeago.distanciaRecorrida;
                        distancia.setText(formato1.format(recorridoMetros)+" "+usuarioLogeago.getUnidades());
                        usuarioLogeago.setLatitud(location.getLatitude());//agregar
                        usuarioLogeago.setLongitud(location.getLongitude());///agregar
                        mDataBase.child("ubicaciones").push().setValue(latlang);
                    }

                }
            }
        }
    }
    ///cambio en la loclizacion del usuaro
    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if(usuarioLogeago!=null){
                actua(location);
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void permisosApp(){

        int estadoDePermiso1 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (estadoDePermiso1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    CODIGO_PERMISOS_UBICACION);
        }
        ///
        int estadoDePermiso2 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (estadoDePermiso2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PERMISOS_UBICACION);
        }
        ///INTERET
        int estadoDePermiso3 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET);
        if (estadoDePermiso3 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.INTERNET},
                    CODIGO_PERMISOS_UBICACION);
        }

    }
    public void recibirDatos(){
        Bundle datos = getIntent().getExtras();
        if (datos!=null){
            usuarioLogeago =(Usuario)datos.getSerializable("usuario");
            this.configuracion=(Configuracion)datos.getSerializable("configuracion");
            if (configuracion!=null){
               // Toast.makeText(this,"Temp"+configuracion.getTiempoGps(),Toast.LENGTH_SHORT).show();
            }
            String id = (String)datos.getSerializable("id");
            baseDATOS= FirebaseDatabase.getInstance().getReference();
            recuperarDatos(id);


           sesion(usuarioLogeago);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnMapa:Intent intent= new Intent(MainActivity.this, MapsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("usuario", usuarioLogeago);
                bundle.putSerializable("ubicaciones",marcadorReal);
                intent.putExtras(bundle);
                startActivity(intent);
                break;


        }
    }




    @Override
    protected void onStart(){
        baseDATOS= FirebaseDatabase.getInstance().getReference();
        mDataBase= FirebaseDatabase.getInstance().getReference();
        super.onStart();
        mauth=FirebaseAuth.getInstance();
    //    sesion(usuarioLogeago);
        SharedPreferences settings= getApplicationContext().getSharedPreferences("configuracion",0);
        tiempoActualizacion=settings.getInt("tiempo",0)*1000;

        if(mauth.getCurrentUser()!=null){

          // startActivity(new Intent(MainActivity.this,MapsActivity.class));
           recuperarDatos(mauth.getUid());
         //  sesion(usuarioLogeago);
        }
//       creatBotonesMenu(men);

    }
    private Logeos data;
    private DatabaseReference baseDATOS;
    public void recuperarDatos(final String idEntrada){
        baseDATOS.child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String usuario="";
                String apellido="";
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    data=snapshot.getValue(Logeos.class);
                    String id=data.getId();
                    try{
                        if(id.equals(idEntrada)){
                            usuario=data.getNombre();
                        }

                    }catch (Exception e){
                        Log.e("Excepcion:",e.getMessage());
                    }
                }
               usuarioLogeago =new Usuario(usuario,0.0,0.0);
                usuarioLogeago.idRegistro= usuario;
                usuarioLogeago.apellido=apellido;
                sesion(usuarioLogeago);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    ////////////////////////sensores

    @Override
    protected void onResume() {

        super.onResume();
        running=true;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor!=null){
            sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI);

        }else{
            Toast.makeText(this,"SENSOR PASOS NO ENCONTRADO",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onPause() {

        super.onPause();
        running=false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (running && band) {
            this.pasosAlmacenados=event.values[0];
            band=false;
        }else if(usuarioLogeago!=null){
            float pasosActuales=event.values[0]-pasosAlmacenados;
            this.usuarioLogeago.pasosDados=pasosActuales;
            pasos.setText(Float.toString(usuarioLogeago.pasosDados));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
@Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable("usuario",this.usuarioLogeago);
        outState.putDouble("recorrido",recorridoMetros);

}
@Override
    protected void onRestoreInstanceState(Bundle saved){
        super.onRestoreInstanceState(saved);
    usuarioLogeago =(Usuario)saved.getSerializable("usuario");
    recorridoMetros=(Double)saved.getDouble("recorrido");
    if(usuarioLogeago!=null){
        if (usuarioLogeago.pasosDados!=null){
            pasos.setText(Float.toString(usuarioLogeago.pasosDados));

        }
        if (usuarioLogeago.distanciaRecorrida!=0) {
            DecimalFormat formato1 = new DecimalFormat("#.00");
            distancia.setText(formato1.format(usuarioLogeago.distanciaRecorrida)+" "+usuarioLogeago.getUnidades());
        }
        if(recorridoMetros!=null){
            distancia.setText(Double.toString(recorridoMetros));
        }
    }


}
public void creatBotonesMenu(Menu menus){
    if(usuarioLogeago !=null){
        menus.findItem(R.id.IniciarSesion).setEnabled(false);
        menus.findItem(R.id.registrarse).setEnabled(false);
        menus.findItem(R.id.cerrarSesion).setEnabled(true);
    }else {
        menus.findItem(R.id.cerrarSesion).setEnabled(false);
    }
}
Menu men;
public boolean onCreateOptionsMenu(Menu menus){
        men=menus;
       getMenuInflater().inflate(R.menu.overflow,menus);
    //   creatBotonesMenu(menus);


        return true;
}
public boolean onOptionsItemSelected(MenuItem item){

        int id=item.getItemId();
    Toast.makeText(this,"configuracion", Toast.LENGTH_SHORT);
        if(id==R.id.configuracion){
            Intent intent=new Intent(this,ConfiguracionActivity.class);
           Bundle bun= new Bundle();
            bun.putSerializable("configuracion",this.configuracion);
            intent.putExtras(bun);
            startActivity(intent);
        }
        else if (id==R.id.cerrarSesion){
            Toast.makeText(this,"Regresa Pronto", Toast.LENGTH_LONG).show();
            mauth.signOut();
            this.btnMapa.setEnabled(false);
            this.nomUsuarioSesion.setText("");
            this.distancia.setText("0");
            this.pasos.setText("0");
            usuarioLogeago=null;
            //startActivity(new Intent(this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        }else if(id==R.id.closeApp){
            Toast.makeText(this,"Cerrar App", Toast.LENGTH_LONG).show();

            finish();

        }else if(id==R.id.IniciarSesion){
            Intent intent = new Intent(this,IngresarActivity.class);

            startActivity(intent);
        }
        else if(id==R.id.registrarse){
            Intent intent = new Intent(this,RegistrarActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("usuario", usuarioLogeago);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
}



}




















class OperacionesRecorrido extends Thread{
    Double distanciaNueva=0.0;
    Usuario usuario;
    Double lat1,lat2;
    Double lng1,lng2;
    public OperacionesRecorrido(Usuario usuario,Double lat,Double log){
        this.usuario=usuario;
        lat2=usuario.getLatitud();
        lng2=usuario.getLongitud();
        this.lat1=lat;
        this.lng1=log;
    }
    @Override
    public void run () {
        //double radioTierra = 3958.75;//en millas
        if(lat2!=0.0 && lng2!=0.0){
            double radioTierra = 6371;//en kilómetros
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double sindLat = Math.sin(dLat / 2);
            double sindLng = Math.sin(dLng / 2);
            double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                    * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
            double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
            double distancia = radioTierra * va2;
            usuario.distanciaRecorrida+=distancia*1000;
        }

        /*
        Double aux=usuario.distanciaRecorrida+distancia*1000;
        if (aux>1000 ){
            usuario.setUnidades("Km");
            Double auxa=usuario.distanciaRecorrida;
            usuario.distanciaRecorrida=usuario.distanciaRecorrida/1000;
            Log.e("Cambio de unidad a KM "+auxa," a "+usuario.distanciaRecorrida);
        }

        if (usuario.getUnidades().equals("m")){
            Log.e("Matiene metros "+usuario.distanciaRecorrida," a ");
            Double disTot=usuario.distanciaRecorrida+distancia*1000;
            usuario.distanciaRecorrida=disTot;
        }else{

            Double disTot=usuario.distanciaRecorrida+distancia;
            Log.e("Matiene km "+usuario.distanciaRecorrida," a ");
            usuario.distanciaRecorrida=disTot;
        }
*/




    }

}
