package com.example.tecnologiabluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.Intent;

import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    //Buttons
    Button btnOnLed;
    Button btnOffLed;
    Button btnAbrir;
    Button btnCerrar;

    //TextVire
    TextView txtMensaje;

    //------CONTROLADOR DE EVENTOS DE BT ----------//
    Handler bluetoothIn;
    final int HANDLER_STATUS = 0;
    BluetoothAdapter btAdapter = null;
    BluetoothSocket btSocket = null;
    StringBuilder dataStringIN = new StringBuilder();

    //------CODIGO UNICO DE SERVICIO SSP UUID-------//
    private static final UUID BTMODULOUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Codigo unico de servicio BT
    private String address = null;

    //-----------AQUI DECLARAMOS EL HILO------------//
    ConnectedThread myConexionBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOnLed = findViewById(R.id.btnLEDOn);
        btnOffLed = findViewById(R.id.btnLEDOff);
        btnAbrir = findViewById(R.id.btnAbrirP);
        btnCerrar = findViewById(R.id.btnCerrarP);

        //Manejadore de enventos
        bluetoothIn = new Handler(){
            public void handlerMensaje(android.os.Message msg){
                if(msg.what == HANDLER_STATUS){
                    String readMensaje = (String) msg.obj;
                    dataStringIN.append(readMensaje);
                    int endOfLineIndex = dataStringIN.indexOf("#");
                    if(endOfLineIndex > 0){
                        String dataInPrint = dataStringIN.substring(0, endOfLineIndex);
                        //txtMensaje.setText("Dato :" + dataInPrint);
                        dataStringIN.delete(0, dataInPrint.length());
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); //Se optiene el adaptador local predeterminado
        verificarEstadoBT(); //Verifica el estado de BT para realizar la conexccion

        /*
        * Eventos de los botones para el envio de datos.
        *   A = LED ON
        *   B = LED OFF
        *   C = OPEN DOOR / RGB = GREEN
        *   D = CLOSE DOOR / RGB = RED
        */
        btnOnLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myConexionBT.enviarArduino("A");
            }
        });

        btnOffLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myConexionBT.enviarArduino("B");
            }
        });

        btnAbrir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myConexionBT.enviarArduino("C");
            }
        });

        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myConexionBT.enviarArduino("D");
            }
        });
    }

    /**
     * Hilo para realizar la conexion con el dispositivo bluetooth deseado
     */
    //---------AQUI ESTA EL HILO------------------//
    class ConnectedThread extends Thread{
        InputStream mmInputStream; //Canal de entrada
        OutputStream mmOutputStream; //Canal de salida

        public ConnectedThread(BluetoothSocket socket){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                //Se opctiene los canales del socket de Bluetooth
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            //Se inicializan los canales de entrada y de salida
            this.mmInputStream = tmpIn;
            this.mmOutputStream = tmpOut;
        }

        public void run(){
            byte [] buffer = new byte[256];
            int bytes;
            //----MANTENER EL MODO ESCUCHA----//
            while(true){
                try{
                    bytes = mmInputStream.read(buffer);
                    String readMensaje = new String(buffer, 0 , bytes);

                    bluetoothIn.obtainMessage(HANDLER_STATUS,bytes, -1, readMensaje).sendToTarget();
                }catch (IOException e1){
                    e1.printStackTrace();
                    break;
                }
            }
        }

        /**
         * Enviar datos al arduino, con el valor a enviar como parametro
         * @param input
         */
        public void enviarArduino(String input){
            try{
                //Por el canal de salida se envia la informacion en bytes
                mmOutputStream.write(input.getBytes());
            }catch (IOException e2){
                Toast.makeText(getBaseContext(), "La conexion fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    //------------VINCULACION DISPOSITIVOS-----------------//

    /**
     * Crear comunicacion del socket del Bluetooth, con el dispostivo a conectar como parametro
     * @param device
     * @return
     * @throws IOException
     */
    private BluetoothSocket creaBluetoothSocket(BluetoothDevice device) throws IOException {
        //Retorna la conexcion con algun dispositvi de forma segura utilizando UUID
        return device.createRfcommSocketToServiceRecord(BTMODULOUUID);
    };

    public void onResume(){
        super.onResume();

        Intent i = getIntent();
        //Captura de informacion del intent
        address = i.getStringExtra(Dispositivos_BT.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address); //Obtiene el dispositvo utilizando la direccion address

        try{
            //Se crea el socket de comunicacion entre el celular y el modulo Bluetooth, mandando como parametro el dispositivo deseado
            btSocket = creaBluetoothSocket(device);
        }catch (IOException e3){
            Toast.makeText(getBaseContext(), "No se pudo crear el socket", Toast.LENGTH_LONG).show();
        }

        try {
            //Se realiza la conexion
            btSocket.connect();
        }catch (IOException e4){

        }

        myConexionBT = new ConnectedThread(btSocket); //Inicializacion del hilo de conexcion
        myConexionBT.start(); //Lanzamiento del hilo
    }

    private void verificarEstadoBT(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null){
            Toast.makeText(getBaseContext(), "El dispositivo no soporta la tecnologia Bluetooth", Toast.LENGTH_SHORT).show();
        }else{
            if(btAdapter.isEnabled()){
                //Log.d(TAG, "..Bluetooh Activado..");
            }else{
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, 1);
            }
        }
    }
}