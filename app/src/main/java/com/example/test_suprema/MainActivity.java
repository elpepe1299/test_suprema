package com.example.test_suprema;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final boolean mbUsbExternalUSBManager = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent= null;
    private static BioMiniFactory mBioMiniFactory = null;
    public static final int REQUEST_WRITE_PERMISSION = 786;
    public IBioMiniDevice mCurrentDevice = null;
    private MainActivity mainContext;

    public final static String TAG = "IAMextrix Beta Log";
    private TextView mLogView;
    private TextView mStatusView;
    private TextView nombre;
    private TextView dni;
    private TextView rol;
    private TextView estado;
    private ScrollView mScrollLog = null;
    private Button leer;
    private Button guardar;
    private Button verificar;
    private Button registrar;
    public IBioMiniDevice.TemplateData personTemplate;
    private Bitmap personImage;
    private IBioMiniDevice.CaptureOption mCaptureOptionDefault = new IBioMiniDevice.CaptureOption();
     int count = 0;
     int count2 = 0;
     int SN = 0;
    long overallElapsedTime = 0;
    // Inicailizacion de configuracion de lectura
    private CaptureResponder mCaptureResponseDefault = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                              final IBioMiniDevice.TemplateData capturedTemplate,
                              final IBioMiniDevice.FingerState fingerState) {
            byte[] pImage_raw =null;
            pImage_raw = mCurrentDevice.getCaptureImageAsRAW_8();
            log(String.format(Locale.ENGLISH, "pImage (%d) , FP Quality(%d)", pImage_raw.length , mCurrentDevice.getFPQuality(pImage_raw, mCurrentDevice.getImageWidth(), mCurrentDevice.getImageHeight(), 2)));
            //log("onCapture : Capture successful!");
            printState(getResources().getText(R.string.capture_single_ok));

            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    enableButton(leer);
                    enableButton(guardar);
                    enableButton(verificar);
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if((capturedImage != null)){
                        ImageView iv =(ImageView) findViewById(R.id.finger);
                        byte fp[] = capturedTemplate.data;
                        if(iv != null){
                            iv.setImageBitmap(capturedImage);
                        }
                    }
                }
            });

            personTemplate = capturedTemplate;
            personImage = capturedImage;
            return true;
        }

        @Override
        public void onCaptureError(Object contest, int errorCode, String error){
            if(errorCode != IBioMiniDevice.ErrorCode.OK.value())
                printState(getResources().getText(R.string.capture_single_fail) + "("+error+")");
            disableButton(leer);
            disableButton(guardar);
            disableButton(verificar);
        }
    };

    // Función para borrar todos los archivos en el almacenamiento interno
    private void clearAllFilesInInternalStorage() {
        File internalDir = getFilesDir();
        File[] files = internalDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("Base64Data") && file.getName().endsWith(".txt")) {
                    file.delete();
                }
            }
        }
    }
    // Funcion de registro
    private CaptureResponder mRegister = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {
            printState(getResources().getText(R.string.capture_single_ok));
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    enableButton(leer);
                    enableButton(guardar);
                    enableButton(verificar);
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if((capturedImage != null)){
                        ImageView iv =(ImageView) findViewById(R.id.finger);
                        if(iv != null){
                            // Actualizar el contador fuera del bucle
                            nombre.setText(String.valueOf(count));
                            iv.setImageBitmap(capturedImage);
                            byte[] dat = capturedTemplate.data;
                            byte[] mock = {'A', 'B', 'C'};
                            SendDataHelper.sendData(MainActivity.this, mock, new SendDataHelper.SendDataCallback() {
                                @Override
                                public void onSuccess(String response) {
                                    log("Send data Success" + response);
                                }

                                @Override
                                public void onError(String error) {
                                    log("Send data Error" + error);
                                }
                            });
                        }
                    }
                }
            });
            return true;
        }

        @Override
        public void onCaptureError(Object contest, int errorCode, String error){
            if(errorCode != IBioMiniDevice.ErrorCode.OK.value())
                printState(getResources().getText(R.string.capture_single_fail) + "("+error+")");
            disableButton(leer);
            disableButton(guardar);
            disableButton(verificar);
        }
    };

    // Funcion de comparacion
    private CaptureResponder mCompare = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {
            printState(getResources().getText(R.string.capture_single_ok));
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    enableButton(leer);
                    enableButton(guardar);
                    enableButton(verificar);
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (capturedImage != null) {
                        ImageView iv = (ImageView) findViewById(R.id.finger);
                        if (iv != null) {
                            iv.setImageBitmap(capturedImage);
                            long overallStartTime = System.currentTimeMillis(); // Tiempo de inicio de la operación completa
                            // Obtenemos la lista de archivos en el directorio de almacenamiento interno
                            File internalDir = getFilesDir();
                            File[] files = internalDir.listFiles();

                            if (files != null) {
                                for (File file : files) {
                                    count2++;
                                    if (file.getName().startsWith("Base64Data") && file.getName().endsWith(".txt")) {
                                        try {
                                            long startTime = System.currentTimeMillis();
                                            FileInputStream fis = new FileInputStream(file);
                                            ObjectInputStream ois = new ObjectInputStream(fis);
                                            byte[] recovered = (byte[]) ois.readObject();
                                            ois.close();
                                            fis.close();
                                            long endTime = System.currentTimeMillis();
                                            long elapsedTime = endTime - startTime;
                                            Boolean rptaHuellero = mCurrentDevice.verify(capturedTemplate.data, recovered);
                                            String elapsedTimeStr = String.valueOf(elapsedTime);
                                            rol.setText("C2 : " +count2);
                                            if (rptaHuellero) {
                                                Log.i("", "TIEMPO DE RESPUESTA COINCIDENTE"+  elapsedTime);

//                                                Toast.makeText(MainActivity.this, "Las huellas coinciden con " + file.getName() + " (Tiempo: " + elapsedTime + " ms)", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.i("", "TIEMPO DE RESPUESTA NOOOO COINCIDENTE"+  elapsedTime);
//                                                Toast.makeText(MainActivity.this, "Las huellas NO coinciden con " + file.getName() + " (Tiempo: " + elapsedTime + " ms)", Toast.LENGTH_SHORT).show();
//                                                dni.setText("N-C"+elapsedTimeStr);
                                            }
                                        } catch (IOException | ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            long overallEndTime = System.currentTimeMillis(); // Tiempo de finalización de la operación completa
                             overallElapsedTime = overallEndTime - overallStartTime; // Tiempo total de la operación
                            dni.setText("T " + overallElapsedTime + " ms");
                        }
                    }
                }
            });

            personTemplate = capturedTemplate;
            personImage = capturedImage;
            return true;
        }

        @Override
        public void onCaptureError(Object contest, int errorCode, String error){
            if(errorCode != IBioMiniDevice.ErrorCode.OK.value())
                printState(getResources().getText(R.string.capture_single_fail) + "("+error+")");
            disableButton(leer);
            disableButton(guardar);
            disableButton(verificar);
        }
    };

    private CaptureResponder mCaptureResponsePrev = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {

            //Log.d("CaptureResponsePrev", String.format(Locale.ENGLISH , "captureTemplate.size (%d) , fingerState(%s)" , capturedTemplate== null? 0 : capturedTemplate.data.length, String.valueOf(fingerState.isFingerExist)));
            printState(getResources().getText(R.string.start_capture_ok));
            byte[] pImage_raw =null;
            if( (mCurrentDevice!= null && (pImage_raw = mCurrentDevice.getCaptureImageAsRAW_8() )!= null)) {
                Log.d("CaptureResponsePrev ", String.format(Locale.ENGLISH, "pImage (%d) , FP Quality(%d)", pImage_raw.length , mCurrentDevice.getFPQuality(pImage_raw, mCurrentDevice.getImageWidth(), mCurrentDevice.getImageHeight(), 2)));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(capturedImage != null) {
                        ImageView iv = (ImageView) findViewById(R.id.finger);
                        if(iv != null) {
                            iv.setImageBitmap(capturedImage);
                        }
                    }
                }
            }   );
            return true;
        }

        @Override
        public void onCaptureError(Object context, int errorCode, String error) {
            if( errorCode != IBioMiniDevice.ErrorCode.OK.value())
                printState(getResources().getText(R.string.start_capture_fail));
        }
    };

    synchronized public void printState(final CharSequence str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.status_view)).setText(str);
            }
        });

    }


    // Log para Debug (Se requiere insertar el widget en el xml)

    synchronized public void log(final String msg)
    {
        Log.d(TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if( mLogView == null){
                    mLogView = (TextView) findViewById(R.id.log_Text);
                }
                if(mLogView != null) {
                    mLogView.append(msg + "\n");
                    if(mScrollLog != null) {
                        mScrollLog.fullScroll(mScrollLog.FOCUS_DOWN);
                    }else{
                        Log.d("Log " , "ScrollView is null");
                    }
                }
                else {
                    Log.d("", msg);
                }
            }
        });
    }

    private final BroadcastReceiver mUsbReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ACTION_USB_PERMISSION.equals(action)){
                synchronized (this){
                    UsbDevice device =(UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        if(device != null){
                            if(mBioMiniFactory == null) return;
                            mBioMiniFactory.addDevice(device);
                        }
                    }
                    else{
                        Log.d(TAG, "permission denied for device"+ device);
                    }
                }
            }
        }
    };

    // Vemos si esta conectado el lector
    public void checkDevice(){
        if(mUsbManager == null) return;
        //log("checkDevice");
        HashMap<String , UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIter = deviceList.values().iterator();
        while(deviceIter.hasNext()){
            UsbDevice _device = deviceIter.next();
            if( _device.getVendorId() ==0x16d1 ){
                //Suprema vendor ID
                mUsbManager.requestPermission(_device , mPermissionIntent);
            }else{
            }
        }
    }

    // Main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContext = this;
        leer = findViewById(R.id.button);
        guardar = findViewById(R.id.button2);
        verificar = findViewById(R.id.button3);
        registrar = findViewById(R.id.register);
        nombre = findViewById(R.id.nombre);
        dni = findViewById(R.id.DNI);
        rol = findViewById(R.id.rol);
        estado = findViewById(R.id.estado);
        mCaptureOptionDefault.frameRate = IBioMiniDevice.FrameRate.SHIGH;


        leer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ImageView) findViewById(R.id.finger)).setImageResource(R.drawable.huella);
                //estado.setText(SN);
                if(mCurrentDevice != null){
                    mCurrentDevice.captureSingle(
                            mCaptureOptionDefault,
                            mCaptureResponseDefault,
                            true);
                }
            }
        });

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterFormActivity.class));
            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Borrar todos los archivos al inicio
                count = 0;
                nombre.setText(String.valueOf(count));




                ((ImageView) findViewById(R.id.finger)).setImageResource(R.drawable.huella);
                if(mCurrentDevice != null){
                    mCurrentDevice.captureSingle(
                            mCaptureOptionDefault,
                            mRegister,
                            true);
                }
            }
        });

        verificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ImageView) findViewById(R.id.finger)).setImageResource(R.drawable.huella);
                if(mCurrentDevice != null){
                    mCurrentDevice.captureSingle(
                            mCaptureOptionDefault,
                            mCompare,
                            true);
                }
            }
        });

        restartBioMini();
    }

    // Reseteamos la conexion si se detecta algun cambio en el lector
    void restartBioMini() {
        if(mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }
        if( mbUsbExternalUSBManager ){
            mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
            mBioMiniFactory = new BioMiniFactory(mainContext, mUsbManager){
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    log("----------------------------------------");
                    log("Fingerprint Scanner Changed : " + event + " using external usb-manager");
                    log("----------------------------------------");
                    handleDevChange(event, dev);
                }
            };
            //
            mPermissionIntent = PendingIntent.getBroadcast(this,0,new Intent(ACTION_USB_PERMISSION),0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(mUsbReceiver, filter);
            checkDevice();
        }else {
            mBioMiniFactory = new BioMiniFactory(mainContext) {
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    log("----------------------------------------");
                    log("Fingerprint Scanner Changed : " + event);
                    log("----------------------------------------");
                    handleDevChange(event, dev);

                }
            };
        }
    }

    // Manejamos los cambios en el lector
    void handleDevChange(IUsbEventHandler.DeviceChangeEvent event, Object dev) {
        if (event == IUsbEventHandler.DeviceChangeEvent.DEVICE_ATTACHED && mCurrentDevice == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int cnt = 0;
                    while (mBioMiniFactory == null && cnt < 20) {
                        SystemClock.sleep(1000);
                        cnt++;
                    }
                    if (mBioMiniFactory != null) {
                        mCurrentDevice = mBioMiniFactory.getDevice(0);
                        printState(getResources().getText(R.string.device_attached));
                        //Log.d(TAG, "Hardware Attached : " + mCurrentDevice);
                        if (mCurrentDevice != null /*&& mCurrentDevice.getDeviceInfo() != null*/) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    enableButton(leer);
                                    //log(" DeviceName : " + mCurrentDevice.getDeviceInfo().deviceName);
                                    String SN = mCurrentDevice.getDeviceInfo().deviceSN;
                                    log("         SN : " + SN);
                                    //log("SDK version : " + mCurrentDevice.getDeviceInfo().versionSDK);
                                }
                            });
                        }
                    }
                }
            }).start();
        } else if (mCurrentDevice != null && event == IUsbEventHandler.DeviceChangeEvent.DEVICE_DETACHED && mCurrentDevice.isEqual(dev)) {
            disableButton(leer);
            disableButton(verificar);
            disableButton(guardar);
            printState(getResources().getText(R.string.device_detached));
            //Log.d(TAG, "Fingerprint Scanner removed : " + mCurrentDevice);
            mCurrentDevice = null;
        }
    }
    @Override
    protected void onDestroy() {
        if (mBioMiniFactory != null) {
            mBioMiniFactory.close();
            mBioMiniFactory = null;
        }
        if( mbUsbExternalUSBManager ){
            unregisterReceiver(mUsbReceiver);
        }
        super.onDestroy();
    }
    private void disableButton(Button view) {
        view.setEnabled(false);
    }

    private void enableButton(Button view) {
        view.setEnabled(true);
    }
}