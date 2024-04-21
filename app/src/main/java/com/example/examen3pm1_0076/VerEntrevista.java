package com.example.examen3pm1_0076;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

public class VerEntrevista extends AppCompatActivity {
    TextView textViewDescripcion, textViewPeriodista,textViewID;
    ImageView imageViewEntrevista, reproducir;
    Button eliminarBoton, editarBoton, buttonTomarImagen;
    String imagenBase64;
    MediaPlayer mediaPlayer;
    SeekBar barra;
    Uri imageUri;
    Handler handler = new Handler();
    Runnable runnable;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int PERMISSION_CODE = 1003;
    boolean reproduciendo = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ver_entrevista);

        textViewID = findViewById(R.id.textViewID);
        textViewDescripcion = findViewById(R.id.textViewDescripcion);
        textViewPeriodista = findViewById(R.id.textViewPeriodista);
        imageViewEntrevista = findViewById(R.id.imageViewEntrevista);
        reproducir = findViewById(R.id.reproducir);
        eliminarBoton = findViewById(R.id.eliminarBoton);
        editarBoton = findViewById(R.id.editarBoton);
        buttonTomarImagen = findViewById(R.id.buttonTomarImagen);

        Intent intent = getIntent();
        if(intent != null) {
            int id = intent.getIntExtra("ID", -1);
            String descripcion = intent.getStringExtra("Descripcion");
            String periodista = intent.getStringExtra("Periodista");
            String imagenBase64 = intent.getStringExtra("ImagenBase64");
            String audioBase64 = intent.getStringExtra("AudioBase64");

            textViewID.setText(String.valueOf(id));
            textViewDescripcion.setText(descripcion);
            textViewPeriodista.setText(periodista);
            if (imagenBase64 != null && !imagenBase64.isEmpty()) {
                byte[] decodedString = Base64.decode(imagenBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageViewEntrevista.setImageBitmap(decodedByte);
            }

            if (audioBase64 != null && !audioBase64.isEmpty()) {
                prepararReproductor(audioBase64);
            }

            editarBoton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textViewDescripcion.setEnabled(true);
                    textViewPeriodista.setEnabled(true);
                    buttonTomarImagen.setVisibility(View.VISIBLE);

                    editarBoton.setText("Guardar");
                    editarBoton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            guardarEnFirebase();
                        }
                    });
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
                }else {
                    Toast.makeText(VerEntrevista.this, "El reproductor de audio no está listo.", Toast.LENGTH_SHORT).show();
                }
            });

            eliminarBoton.setOnClickListener(v -> eliminarEntrevista(id));

            buttonTomarImagen.setOnClickListener(v->{
                if(checkPermission()){
                    showImagePickDialog();
                }else{
                    requestPermission();
                }
            });
        }

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
                }else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSION_CODE);
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



    private void prepararReproductor(String audioBase64) {
        try {
            byte[] decodedBytes = Base64.decode(audioBase64, Base64.DEFAULT);
            File audioFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "interview_audio.mp3");
            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                fos.write(decodedBytes);
                configurarReproductor(Uri.fromFile(audioFile));
            }
        } catch (Exception e) {
            Log.e("Preparar Reproductor", "Error preparando reproductor" + e.getMessage());
            Toast.makeText(this, "Fallo en preparar reproductor", Toast.LENGTH_SHORT);
        }
    }

private void configurarReproductor(Uri audioUri){
    mediaPlayer = new MediaPlayer();
    try {
        mediaPlayer.setDataSource(getApplicationContext(), audioUri);
        mediaPlayer.setOnPreparedListener(mp -> {
            reproducir.setEnabled(true);
            mediaPlayer.start();
        });
        mediaPlayer.prepareAsync();
    } catch (IOException e){
        Toast.makeText(this, "Error configurando reproductor", Toast.LENGTH_SHORT);
    }
}


    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    private void verLista(){
        Intent intent = new Intent(this, ListaEntrevistas.class);
        startActivity(intent);
        finish();
    }

    private void guardarEnFirebase(){
        if (textViewDescripcion.getText().toString().isEmpty() || textViewPeriodista.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        inicializarFirebase();
        int id = Integer.parseInt(textViewID.getText().toString().trim());
        String descripcion = textViewDescripcion.getText().toString().trim();
        String periodista = textViewPeriodista.getText().toString().trim();

        Entrevista entrevista = new Entrevista(id, descripcion, periodista, imagenBase64);

        databaseReference.child("Entrevistas").child(String.valueOf(id)).setValue(entrevista)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(VerEntrevista.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    verLista();
                })
                .addOnFailureListener(e -> Toast.makeText(VerEntrevista.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show());
    }

    private void actualizarBarra(){
        if(mediaPlayer != null){
            barra.setProgress(mediaPlayer.getCurrentPosition());
            runnable=this::actualizarBarra;
            handler.postDelayed(runnable, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();if (mediaPlayer != null) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            if (mediaPlayer!=null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    private void eliminarEntrevista(int ID){
        if (ID==-1){
            Toast.makeText(this, "ID inválido, no se puede eliminar", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Entrevistas");
        ref.child(String.valueOf(ID)).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(VerEntrevista.this, "Entrevista eliminada", Toast.LENGTH_SHORT).show();
            finish();}).addOnFailureListener(e -> {
            Toast.makeText(VerEntrevista.this, "Error al eliminar la entrevista", Toast.LENGTH_SHORT).show();
        });
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
            }

            if (imageUri != null) {
                imageViewEntrevista.setImageURI(imageUri);
                imagenBase64 = uriToBase64(imageUri);
            } else {
                if (requestCode == IMAGE_CAPTURE_CODE && imageUri != null) {
                    imageViewEntrevista.setImageURI(imageUri);
                    imagenBase64 = uriToBase64(imageUri);
                }
            }
        }
    }
}
