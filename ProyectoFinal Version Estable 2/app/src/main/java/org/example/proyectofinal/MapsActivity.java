package org.example.proyectofinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private Usuario usuario;
    private GoogleMap mMap;
    DataSnapshot dataSnapshot;
    private ArrayList<ArrayList<LatLng>> lineasBuses;
    ///referencia  a la base de datos
    private DatabaseReference baseDatos;
    Double distanciaRecorrida;
    DatosBaseDatos bd;

    EditText etPlaceOrigen;
    EditText etPlaceDestino;
    Button btSubmit;
    TextView tvAdreess;
    //
    private ArrayList<Marker> marcadorTemporal=new ArrayList<>();
    private ArrayList<LatLng> marcadorReal= new ArrayList<>();
    private ArrayList<Usuario> usuarios= new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mapa);
        spinnerLineas=(Spinner)findViewById(R.id.spinnerRutas);
        etPlaceOrigen= findViewById(R.id.editTextOrigen);
        etPlaceDestino=findViewById(R.id.editTextDestino);
        btSubmit=findViewById(R.id.buttonGuardarOrigen);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        recibirDatos();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        baseDatos= FirebaseDatabase.getInstance().getReference();

        temporizador();

    }
    punto coordenadas= new punto();
    public synchronized void guardarUbicacionOrigen(View v){

            String dirreccion=etPlaceOrigen.getText().toString();
            GeoLocation geoLocation = new GeoLocation();
            geoLocation.getAddress(dirreccion,getApplicationContext(),new GeoHandler(etPlaceOrigen,this,mMap,coordenadas));






    }

    public void guardarUbicacionDestino(View v){
            String dirreccion=etPlaceDestino.getText().toString();
            GeoLocation geoLocation = new GeoLocation();
            geoLocation.getAddress(dirreccion,getApplicationContext(),new GeoHandler(etPlaceDestino,this, mMap,coordenadas));

    }
    private void temporizador(){
        new CountDownTimer(10000,1000){
            public void onTick(long millisUntilFinised){

            }
            public void onFinish(){
               // Toast.makeText(MapsActivity.this,"APP ACTUALIZADA",Toast.LENGTH_SHORT).show();

                //onMapReady(mMap);
                cargarPosicionTocada();
            }
        }.start();
    }
    private class GeoHandler extends Handler{
        public EditText etPlace;
        public Context context;
        public GoogleMap mMap;
        public punto coordenadas;
        public GeoHandler (EditText etPlace, Context context,GoogleMap mMap,punto coordenadas){
            this.etPlace=etPlace;
            this.context=context;
            this.mMap=mMap;
            this.coordenadas=coordenadas;
        }
        @Override
        public void handleMessage(Message msg){
            String address;
            switch (msg.what){
                case 1:
                    Bundle bundle=msg.getData();
                    address=bundle.getString("address");
                    this.etPlace.setText(address);
                    String datos [] =etPlace.getText().toString().split(",");
                    if(!coordenadas.estado){
                        coordenadas.setLatitudInicial(Double.parseDouble(datos[0]));
                        coordenadas.setLongitudInicial(Double.parseDouble(datos[1]));
                        coordenadas.estado=true;
                    }else{
                        coordenadas.setLatitudFinal(Double.parseDouble(datos[0]));
                        coordenadas.setLongitudFinal(Double.parseDouble(datos[1]));
                    }

                    LatLng sydney = new LatLng(coordenadas.getLatitudInicial(), coordenadas.getLongitudInicial());
                    mMap.addMarker(new MarkerOptions().position(sydney).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    float zoom=16;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,zoom));//pongo el zoom y que se cente en la posicion
                    break;
                default:
                    address=null;
            }




        }
    }
    public void cargarPosicionTocada(){
        Toast.makeText(this,"sasasas",Toast.LENGTH_SHORT).show();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.e("eentraaaaaaa","si valeee");
                    mMap.clear();
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    etPlaceDestino.setText(latLng.latitude + "," + latLng.longitude);
                mMap.addMarker(options);
                coordenadas.setLongitudFinal(latLng.longitude);
                coordenadas.setLatitudFinal(latLng.latitude);

            }
        });
    }
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+ output + "?" + parameters +"&key=" +"AIzaSyCKQt40xSEfXMuOHUS68r_3apOH2ACEiEs";


        return url;
    }
    public void trazarRuta(View v){
        if(coordenadas.getLatitudInicial()!=null && coordenadas.getLongitudInicial()!=null && coordenadas.getLatitudFinal()!=null && coordenadas.getLongitudFinal()!=null){
            LatLng origen = new LatLng(coordenadas.getLatitudInicial(),coordenadas.getLongitudInicial());
            LatLng destino= new LatLng(coordenadas.getLatitudFinal(),coordenadas.getLongitudFinal());
            String url = getDirectionsUrl(origen, destino);
            Log.e("la url es ",":"+url);

            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);

        }else{
            Toast.makeText(this,"Ingrese origen y destino",Toast.LENGTH_SHORT).show();
        }
    }
    private class DownloadTask extends AsyncTask <String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public class DirectionsJSONParser {

        /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
        public List<List<HashMap<String,String>>> parse(JSONObject jObject){

            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for(int i=0;i<jRoutes.length();i++){
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    @SuppressWarnings("rawtypes")
                    List path = new ArrayList<HashMap<String, String>>();

                    /** Traversing all legs */
                    for(int j=0;j<jLegs.length();j++){
                        jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for(int k=0;k<jSteps.length();k++){
                            String polyline = "";
                            polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for(int l=0;l<list.size();l++){
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat", Double.toString(list.get(l).latitude) );
                                hm.put("lng", Double.toString(list.get(l).longitude) );
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e){
            }
            return routes;
        }

        /**
         * Method to decode polyline points
         * decoding-polylines-from-google-maps-direction-api-with-java
         * */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng(((lat / 1E5)),
                        ((lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng position=null;
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){

                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    position = new LatLng(lat, lng);
                    points.add(position);
                    Log.e("resultados "+lat,":"+path.get(j));

                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,16));
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(20);
                lineOptions.color(Color.BLUE);
            }
            // Drawing polyline in the Google Map for the i-th route
            if(points!=null) {
                mMap.addPolyline(lineOptions);
            }
        }

    }


    private FusedLocationProviderClient fusedLocationClient;

    public void myLocation(View v){
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            String coor="";
                            coor+=location.getLatitude()+","+location.getLongitude();
                            etPlaceOrigen.setText(coor);
                            LatLng origen= new LatLng(location.getLatitude(),location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origen,16));
                            coordenadas.setLatitudInicial(location.getLatitude());
                            coordenadas.setLongitudInicial(location.getLongitude());
                            coordenadas.estado=true;
                        }
                    }
                });
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
            mMap.setMyLocationEnabled(true);
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
    private Spinner spinnerLineas;
    HashMap<String,ArrayList<LatLng>> rutas;
    public void recibirDatos(){
        Bundle datos = getIntent().getExtras();
        if (datos!=null){
            usuario=(Usuario)datos.getSerializable("usuario");
            rutas=(HashMap<String,ArrayList<LatLng>> )datos.getSerializable("rutas");
            cargarRutas();
          //  this.marcadorReal=(ArrayList<LatLng>)datos.getSerializable("ubicaciones");
            Log.e("Marcadores llegan con ",":"+marcadorReal.size());

        }
    }
    public void cargarRutas(){
        String clave="";

            Iterator<String> lineas=rutas.keySet().iterator();
            while (lineas.hasNext()){
                clave+=lineas.next()+",";
            }
        String [] datos=clave.split(",");
        ArrayAdapter <String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,datos);
        spinnerLineas.setAdapter(adapter);
        spinnerLineas.setSelection(0);

    }
    public void CargarParadas(View view){
        String linea=spinnerLineas.getSelectedItem().toString();
        for (int i=0;i< rutas.get(linea).size();i++){
            Log.e("datos"," "+ rutas.get(linea).get(i).longitude);
            mMap.addMarker(new MarkerOptions().position(rutas.get(linea).get(i)).title(linea).icon(BitmapDescriptorFactory.fromResource(R.mipmap.station)));
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


