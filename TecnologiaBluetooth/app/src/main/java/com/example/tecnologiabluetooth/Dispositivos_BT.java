package com.example.tecnologiabluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

//Para uso de Bluetooth
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

//Listas
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

//Componentes visuales
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;

//Uso de mensajes y log de informes
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;
import java.util.Set;


/**
 * Programacion para mostrar la lista de dispositivos y bt avilitados
 */
public class Dispositivos_BT extends AppCompatActivity {

    //Depuracion de LOGCAT
    private static final String TAG = "Dispositivos_BT";

    //Lista de dispositivos
    ListView dispositivosBT;

    //Informacion para enviar a la activity principal.
    public static String EXTRA_DEVICE_ADDRESS = "device_address"; //Address de despositivo seleccionado

    /**
     * BluetoothAdapter, es el punto de partida para realizar actividades realizadas con bluetooth
     */
    private BluetoothAdapter mBtAdapter;

    //Lista para los dispositivos que se mostraran en la ListView
    private ArrayAdapter mDispositivosDisponiblesArrayAdapter; //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos__b_t);
    }

    public void onResume(){
        super.onResume();
        //Proceso de validacion de dispositivos
        verificarEstadoBT();

        //Adaptador para la listView, necesita un contexto donde ocurris y una plantilla "layout" para mostrar los datos
        mDispositivosDisponiblesArrayAdapter = new ArrayAdapter(this, R.layout.nombre_dispositivo);

        dispositivosBT = findViewById(R.id.lstListaBT); //ListView que se mostrara en pantalla
        dispositivosBT.setAdapter(mDispositivosDisponiblesArrayAdapter); //Asigna el adapter a la listView
        dispositivosBT.setOnItemClickListener(miEscucha); //Evento cuando se le da click a un item de la lista

        //Se optiene el adaptador local predeterminado.
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        /**
         * Se crea una lista de "dispositivosDisponibles" utilizando getBoundeDevices del BluetoothAdapter
         * getBoundedDivices retorna todos los dispositvos disponibles en el rango cercano al dispositivo local
         */
        Set<BluetoothDevice> dispositivosDisponibles = mBtAdapter.getBondedDevices();

        //Si se optuvo algun dispositivo
        if(dispositivosDisponibles.size() > 0){
            /**
             * Se recorre el adaptador de la lista para agregar los datos de los dispositovos disponibles,
             * esto utilizando una estructura for each.
             * Se le agrega una concatenacion del nombre (getName) del dispositvo y la direccion MAC
             * (getAddress) del dispositivo
             */
            for(BluetoothDevice x : dispositivosDisponibles){
                mDispositivosDisponiblesArrayAdapter.add(x.getName() + "\n" + x.getAddress());
            }
        }

    }

    /**
     * Evento que se lanzara al dar click en un item de la listView
     * El cual enviara los datos del dispositivo a conectar.
     */
    private AdapterView.OnItemClickListener miEscucha = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView v, View view, int position, long id) {
            //Se optiene la informacion del TextView de la lista.
            String  info = ((TextView) view).getText().toString();

            //Se optien la direccion MAC con un substring de "info".
            String address = info.substring(info.length() - 17);

            //Se crea el intent para el envio de datos, los cuales enviara a la MainActivity.
            Intent i = new Intent(Dispositivos_BT.this, MainActivity.class);
            //La estructura del putExtra es "device_address" = key, address = value
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);

            //Se lanza el inten a la activity deseada
            startActivity(i);
        }
    };

    private void verificarEstadoBT(){
        mBtAdapter = BluetoothAdapter.getDefaultAdapter(); //Obtiene el adaptador local predeterminado
        if(mBtAdapter == null){ //Si es nulo
            //El dispositovo no soprta la conexion con Bluetooth
            Toast.makeText(getBaseContext(), "El dispositivo no soporta la tecnologia Bluetooth", Toast.LENGTH_SHORT).show();
        }else{ //Si obtiene el adaptador
            if(mBtAdapter.isEnabled()){ //Comprueba si nuestro bluetooth esta abilitado
                Log.d(TAG, "..Bluetooh Activado.."); //Imprime en el log
            }else{
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, 1);
            }
        }
    }

}