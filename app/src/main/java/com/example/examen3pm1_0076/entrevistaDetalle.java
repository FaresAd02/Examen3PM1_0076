package com.example.examen3pm1_0076;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class entrevistaDetalle extends AppCompatActivity {
    TextView textViewDescripcion, textViewPeriodista;
    ImageView imageViewEntrevista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ver_entrevista);

        textViewDescripcion = findViewById(R.id.textViewDescripcion);
        textViewPeriodista = findViewById(R.id.textViewPeriodista);
        imageViewEntrevista = findViewById(R.id.imageViewEntrevista);

        Intent intent = getIntent();
        if(intent != null) {
            int id = intent.getIntExtra("ID", -1);
            String descripcion = intent.getStringExtra("Descripcion");
            String periodista = intent.getStringExtra("Periodista");

            textViewDescripcion.setText(descripcion);
            textViewPeriodista.setText(periodista);

        }

    }
}
