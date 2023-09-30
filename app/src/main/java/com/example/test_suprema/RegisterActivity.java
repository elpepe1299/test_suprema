package com.example.test_suprema;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageProxy;
import com.example.test_suprema.databinding.ActivityRegisterBinding;
import com.ingenieriiajhr.jhrCameraX.BitmapResponse;
import com.ingenieriiajhr.jhrCameraX.CameraJhr;
import com.ingenieriiajhr.jhrCameraX.ImageProxyResponse;
import org.opencv.android.OpenCVLoader;

public class RegisterActivity extends AppCompatActivity {

    //Variables
    private ActivityRegisterBinding binding;
    private CameraJhr cameraJhr;
    private boolean factorCalculate = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        cameraJhr = new CameraJhr(this);

        if(OpenCVLoader.initDebug()){
            Log.d("OPENCV","SUCCESS");
        }else{
            Log.d("OPENCV","ERROR");
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(cameraJhr.allpermissionsGranted() && !cameraJhr.getIfStartCamera()){
            startCameraJhr();
        }else{
            cameraJhr.noPermissions();
        }
    }
    private void startCameraJhr() {
        cameraJhr.addlistenerBitmap(new BitmapResponse() {
            @Override
            public void bitmapReturn(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                }
            }
        });

        cameraJhr.addlistenerImageProxy(new ImageProxyResponse() {
            @Override
            public void imageProxyReturn(@NonNull ImageProxy imageProxy) {
            }
        });

        cameraJhr.initBitmap();
        cameraJhr.initImageProxy();

        cameraJhr.start(0, 0, binding.cameraPreview, true, false, true);
    }
}
