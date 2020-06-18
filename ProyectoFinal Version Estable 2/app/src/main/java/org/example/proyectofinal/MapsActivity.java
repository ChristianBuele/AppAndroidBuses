package org.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private Usuario usuario;
    private GoogleMap mMap;
    DataSnapshot dataSnapshot;
    ///referencia  a la base de datos
    private DatabaseReference baseDatos;
    Double distanciaRecorrida;
    DatosBaseDatos bd;
    private ArrayList<Marker> marcadorTemporal=new ArrayList<>();
    private ArrayList<LatLng> marcadorReal= new ArrayList<>();
    private ArrayList<Usuario> usuarios= new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        recibirDatos();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        baseDatos= FirebaseDatabase.getInstance().getReference();

        temporizador();

    }
    private void temporizador(){
        new CountDownTimer(10000,1000){
            public void onTick(long millisUntilFinised){

            }
            public void onFinish(){
               // Toast.makeText(MapsActivity.this,"APP ACTUALIZADA",Toast.LENGTH_SHORT).show();

                onMapReady(mMap);

            }
        }.start();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    int cont=1;
    int tipoMapa=1;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(usuario!=null){
            mMap = googleMap;

            baseDatos.child("ubicaciones").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshotL) {
                    dataSnapshot=dataSnapshotL;

                    OperacionesMaps mate = new OperacionesMaps(usuarios,mMap,marcadorReal,dataSnapshot,bd,usuario);
                    mate.start();
                    while (mate.isAlive()){

                    }
                    mMap.clear();
                    UiSettings s=mMap.getUiSettings();
                    if (tipoMapa==1){
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    }else{
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }
                    s.setZoomControlsEnabled(true);
                    for (int i=0;i<usuarios.size();i++){
                        if (usuarios.get(i).getUsuario().equals(usuario.getUsuario()) && usuarios.get(i).getBand()){
                        //    recibirDatos();
                          //  for (int j=0;j<marcadorReal.size();j++){
                         //       mMap.addMarker(new MarkerOptions().position(marcadorReal.get(j)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                         //   }
                            LatLng sydney = new LatLng(usuario.getLatitud(), usuario.getLongitud());
                            mMap.addMarker(new MarkerOptions().position(sydney)
                                    .title("Yo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                            float zoom=16;

                            if(cont==1){

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,zoom));//pongo el zoom y que se cente en la posicion
                                usuarios.get(i).setBand(false);
                                cont++;
                            }


                        }else{
                            LatLng sydney = new LatLng(usuarios.get(i).getLatitud(), usuarios.get(i).getLongitud());
                            mMap.addMarker(new MarkerOptions().position(sydney)
                                    .title(usuarios.get(i).getUsuario()));
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            temporizador();
        }

    }
    public void centrar(View view){
        float zoom=16;
        LatLng sydney = new LatLng(usuario.getLatitud(), usuario.getLongitud());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,zoom));//pongo el zoom y que se cente en la posicion

    }

    public void cambiarMapa(View view){
        if (mMap.getMapType()==1){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            tipoMapa=1;
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            tipoMapa=2;
        }




    }
    public void recibirDatos(){
        Bundle datos = getIntent().getExtras();
        if (datos!=null){
            usuario=(Usuario)datos.getSerializable("usuario");
          //  this.marcadorReal=(ArrayList<LatLng>)datos.getSerializable("ubicaciones");
            Log.e("Marcadores llegan con ",":"+marcadorReal.size());

        }
    }



}

class OperacionesMaps extends Thread{
    ArrayList<Usuario> usuarios;
    GoogleMap mMap;
    ArrayList<LatLng> marcadorReal;
    DataSnapshot dataSnapshot;
    DatosBaseDatos bd;
    private Usuario usuario;
    public OperacionesMaps(ArrayList<Usuario> usuarios, GoogleMap mMap, ArrayList<LatLng> marcadorReal, DataSnapshot snapshot, DatosBaseDatos bd, Usuario usuario){
        this.usuarios=usuarios;
        this.mMap=mMap;
        this.marcadorReal=marcadorReal;
        this.dataSnapshot=snapshot;
        this.bd=bd;
        this.usuario=usuario;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
    @Override
    public void run(){
        usuarios.clear();
        for (DataSnapshot snapshot: dataSnapshot.getChildren()){
            bd=snapshot.getValue(DatosBaseDatos.class);
            try{
                Double latitud=bd.getLatitud();
                Double longitud=bd.getLongitud();
                String usuario=bd.getUsuario();

                validarUsuarios(usuario,latitud,longitud);
            }catch (Exception e){
                Log.e("Excepcion:",e.getMessage());
            }
        }


    }
    public void validarUsuarios(String usuarion,Double la,Double log){
        boolean band=false;

        for (int i=0 ; i<usuarios.size();i++){
            if(usuarios.get(i).getUsuario().equals(usuarion) ){

                if(usuarion.equals(usuario.getUsuario())){
                    LatLng nuevaPos=new LatLng(la,log);
                    Log.e("Marcador:"+marcadorReal.size()," nuevo");
                   // marcadorReal.add(nuevaPos);//agregue esto
                    usuario.setLatitud(la);
                    usuario.setLongitud(log);
                    band=true;
                }else{

                    usuarios.get(i).setLatitud(la);
                    usuarios.get(i).setLongitud(log);
                    band=true;
                }
            }

        }
        if (band==false){
            usuarios.add(new Usuario(usuarion,la,log));
        }
    }
}

