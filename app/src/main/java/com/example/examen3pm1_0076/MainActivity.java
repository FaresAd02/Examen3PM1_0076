package com.example.examen3pm1_0076;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    EditText editTextID, editTextDescripcion, editTextPeriodista;
    Button buttonGuardar, buttonRegistrarAudio,buttonTomarImagen;
    TextView longitud;
    ImageView imagen, reproducir;
    SeekBar barra;
    String imagenBase64, audioBase64, audioFilePath;
    Uri imageUri;
    MediaPlayer mediaPlayer;
    MediaRecorder mediaRecorder;
    boolean grabando=false;
    Handler handler = new Handler();
    Runnable runnable;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int AUDIO_PICK_CODE = 1002;
    private static final int PERMISSION_CODE = 1003;
    private static final int AUDIO_RECORD_CODE = 1004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextID = findViewById(R.id.editTextID);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        editTextPeriodista = findViewById(R.id.editTextPeriodista);
        buttonGuardar = findViewById(R.id.butttonGuardar);
        buttonRegistrarAudio = findViewById(R.id.buttonRegistrarAudio);
        buttonTomarImagen = findViewById(R.id.buttonTomarImagen);
        longitud = findViewById(R.id.longitud);
        imagen = findViewById(R.id.imagen);
        reproducir = findViewById(R.id.reproducir);
        barra = findViewById(R.id.barra);

        buttonGuardar.setOnClickListener(v->{
            guardarEnFirebase();
        });

        buttonTomarImagen.setOnClickListener(v->{
            if(checkPermission()){
                showImagePickDialog();
            }else{
                requestPermission();
            }
        });

        buttonRegistrarAudio.setOnClickListener(v->{
            if(checkPermission()){
                manejarGrabacion();
            }else{
                requestPermission();
            }
        });

        reproducir.setOnClickListener(v->{
            if (mediaPlayer != null ) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    reproducir.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mediaPlayer.start();
                    reproducir.setImageResource(android.R.drawable.ic_media_pause);
                    actualizarBarra();
                }
            }
        });
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    showImagePickDialog();
                }else if (permissions[0].equals(Manifest.permission.RECORD_AUDIO)) {
                    manejarGrabacion();
                }else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSION_CODE);
    }



    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    private void guardarEnFirebase(){
        if (editTextID.getText().toString().isEmpty() || editTextDescripcion.getText().toString().isEmpty() || editTextPeriodista.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        inicializarFirebase();

        int id = Integer.parseInt(editTextID.getText().toString().trim());
        String descripcion = editTextDescripcion.getText().toString().trim();
        String periodista = editTextPeriodista.getText().toString().trim();

        Entrevista entrevista = new Entrevista(id, descripcion, periodista, imagenBase64, audioBase64);

        databaseReference.child("Entrevistas").child(String.valueOf(id)).setValue(entrevista)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    verLista();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show());
    }

    private void showImagePickDialog(){
        String[] options = {"Tomar Foto", "Elegir Foto"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SeleccionarImagen").setItems(options, (dialog,which)->{
            if(which==0){
                abrirCamara();
            }else{
                abrirGaleria();
            }
        }).show();
    }

    private void abrirCamara(){
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nueva Imagen");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Desde Camara");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    private void abrirGaleria(){
        Intent galeriaIntent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galeriaIntent, IMAGE_PICK_CODE);
    }

    private void empezarGrabacion(){
        try {
            limpiarGrabacion();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio_entrevista.mp3";
            mediaRecorder.setOutputFile(audioFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();
            grabando = true;
            buttonRegistrarAudio.setText("Detener Grabacion");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Fallo en Grabacion", Toast.LENGTH_SHORT).show();
        }catch (IllegalStateException ise) {
            Toast.makeText(this, "Grabacion no Iniciada Correctamente", Toast.LENGTH_SHORT).show();
        }
    }

    private void terminarGrabacion(){
        if (grabando){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            grabando = false;
            buttonRegistrarAudio.setText("Iniciar Grabacion");

            File audioFile = new File(audioFilePath);
            Uri audioUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    audioFile
            );

            grantUriPermission(getPackageName(), audioUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            audioBase64 = uriToBase64(audioUri);

            Log.d("AudioBase64", "Base64 Audio String: " + audioBase64);
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioFilePath);
                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(mp->{
                    reproducir.setImageResource(android.R.drawable.ic_media_play);
                });
            }catch (IOException e){
                e.printStackTrace();
            }
            reproducir.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void manejarGrabacion(){
        if(!grabando){
            if(checkPermission()){
                empezarGrabacion();
            }else {
                requestPermission();
            }
        }else {
            terminarGrabacion();
        }
    }

    private void limpiarGrabacion(){
        if(mediaRecorder!=null){
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder=null;
        }
    }

    private void actualizarBarra(){
        if(mediaPlayer != null){
            barra.setProgress(mediaPlayer.getCurrentPosition());
            runnable=this::actualizarBarra;
            handler.postDelayed(runnable, 1000);
        }
    }

    private void verLista(){
        Intent intent = new Intent(this, ListaEntrevistas.class);
        startActivity(intent);
        finish();
    }
    private String uriToBase64(Uri audioUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(audioUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] data = outputStream.toByteArray();
            return Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if (data!= null && data.getData()!=null){
                imageUri=data.getData();
            }

            if(imageUri!=null){
                imagen.setImageURI(imageUri);
                imagenBase64=uriToBase64(imageUri);
            }else {
                if (requestCode==IMAGE_CAPTURE_CODE && imageUri !=null){
                    imagen.setImageURI(imageUri);
                    imagenBase64=uriToBase64(imageUri);
                }
            }
        }else if (requestCode==AUDIO_PICK_CODE && data!=null && data.getData()!=null){
            Uri audioUri = data.getData();
            if (audioUri!=null){
                mediaPlayer= MediaPlayer.create(this, audioUri);
                barra.setMax(mediaPlayer.getDuration());
                audioBase64 = uriToBase64(audioUri);
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer=null;
        }
        handler.removeCallbacks(runnable);
    }
}
