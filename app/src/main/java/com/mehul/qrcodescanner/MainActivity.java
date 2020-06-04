package com.mehul.qrcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    CameraView cameraView;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        btn = findViewById(R.id.btn);
        cameraView = findViewById(R.id.CameraView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.start();
                cameraView.captureImage();
            }
        });
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();
                rundetector(bitmap);


                Log.i("TAG---",bitmap.toString());
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

    }
    private void rundetector(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetectorOptions options;
        options = new FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_QR_CODE,
                FirebaseVisionBarcode.FORMAT_PDF417
        ).build();
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        processResult(firebaseVisionBarcodes);
                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void processResult(List<FirebaseVisionBarcode> firebaseVisionBarcodes){
        for (FirebaseVisionBarcode item : firebaseVisionBarcodes) {
            int value_type = item.getValueType();
            switch (value_type) {
                case FirebaseVisionBarcode.TYPE_TEXT: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(item.getRawValue());
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;

                case FirebaseVisionBarcode.TYPE_URL: {
                    try {
                        Intent i = new Intent("android.intent.action.MAIN");
                        i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
                        i.addCategory("android.intent.category.LAUNCHER");
                        i.setData(Uri.parse(item.getRawValue ()));
                        startActivity(i);

                    }catch (Exception e){
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getRawValue ()));
                        startActivity(i);
                    }
                }
                break;

                case FirebaseVisionBarcode.TYPE_CONTACT_INFO:
                    {
                    String info = new StringBuilder("Name")
                            .append(item.getContactInfo().getName().getFormattedName())
                            .append("\n")
                            .append("Address : ")
                            .append(item.getContactInfo().getAddresses().get(0).
                                    getAddressLines())
                            .append("\n")
                            .append("Email : ")
                            .append(item.getContactInfo().getEmails().get(0).getAddress())
                            .toString();

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(info);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
                default:
                    Toast.makeText(MainActivity.this,"Not registered Way",Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        cameraView.start();
    }

    @Override
    protected void onStop(  ) {
        super.onStop();
        cameraView.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }
}
