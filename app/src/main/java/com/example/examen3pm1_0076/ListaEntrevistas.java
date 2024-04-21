package com.example.examen3pm1_0076;

import android.Manifest;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListaEntrevistas extends AppCompatActivity {
    private ListView entrevistasLista;
    private ImageView eliminarBoton;
    private ArrayAdapter<Entrevista> entrevistaAdapter;
    private EntrevistaAdapter entrevistaAdapter0;
    private DatabaseReference databaseReference;
    private List<Entrevista> entrevistaListArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_entrevistas);
        entrevistasLista = findViewById(R.id.entrevistasLista);
        FloatingActionButton fab = findViewById(R.id.fab);

        databaseReference = FirebaseDatabase.getInstance().getReference("Entrevistas");

        entrevistaAdapter = new EntrevistaAdapter(this, entrevistaListArray, new EntrevistaAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Entrevista entrevista) {
                        Intent intent = new Intent(ListaEntrevistas.this, entrevistaDetalle.class);
                        intent.putExtra("ID", entrevista.getID());
                        intent.putExtra("Descripcion", entrevista.getDescricpion());
                        intent.putExtra("Periodista", entrevista.getPeriodista());
                        intent.putExtra("ImagenBase64", entrevista.getImagenBase64());
                        intent.putExtra("audioFilePath", entrevista.getAudioBase64());
                        startActivity(intent);;
                    }
                });
        entrevistasLista.setAdapter(entrevistaAdapter);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            });

        entrevistasLista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entrevista entrevista = entrevistaAdapter.getItem(position);
                Intent intent = new Intent(ListaEntrevistas.this, VerEntrevista.class);
                intent.putExtra("ID", entrevista.getID());
                intent.putExtra("Descripcion", entrevista.getDescricpion());
                intent.putExtra("Periodista", entrevista.getPeriodista());
                intent.putExtra("ImagenBase64", entrevista.getImagenBase64());
                intent.putExtra("audioFilePath", entrevista.getAudioBase64());
                startActivity(intent);
            }
        });

        obtenerEntrevistas();
    }
    private void obtenerEntrevistas() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entrevistaListArray.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Entrevista entrevista = postSnapshot.getValue(Entrevista.class);
                    entrevistaListArray.add(entrevista);
                }
                entrevistaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaEntrevistas.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
