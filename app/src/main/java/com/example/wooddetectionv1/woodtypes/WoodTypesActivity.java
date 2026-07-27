package com.example.wooddetectionv1.woodtypes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooddetectionv1.R;
import com.example.wooddetectionv1.woodtypes.adapter.WoodTypesAdapter;
import com.example.wooddetectionv1.woodtypes.model.WoodType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WoodTypesActivity extends AppCompatActivity {

    private RecyclerView rvWoodTypes;
    private WoodTypesAdapter adapter;
    private EditText etSearch;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wood_types);

        // Enlazar vistas
        rvWoodTypes = findViewById(R.id.rvWoodTypes);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);

        // Volver atrás
        btnBack.setOnClickListener(v -> finish());

        // Cargar lista desde assets
        List<WoodType> woodsList = loadWoodsFromAssets();

        // Configurar RecyclerView con Grid de 2 columnas
        adapter = new WoodTypesAdapter(this, woodsList);
        rvWoodTypes.setLayoutManager(new GridLayoutManager(this, 2));
        rvWoodTypes.setAdapter(adapter);

        // Filtrado en tiempo real al escribir
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private List<WoodType> loadWoodsFromAssets() {
        List<WoodType> list = new ArrayList<>();
        try {
            String[] files = getAssets().list("Maderas");
            if (files != null) {
                for (String file : files) {
                    if (file.toLowerCase().endsWith(".jpg") || file.toLowerCase().endsWith(".jpeg") || file.toLowerCase().endsWith(".png")) {
                        // Extraer el nombre antes de la extensión .jpg
                        String name = file;
                        int dotIndex = file.lastIndexOf('.');
                        if (dotIndex > 0) {
                            name = file.substring(0, dotIndex);
                        }
                        
                        // Mantener el nombre tal cual está en la imagen sin la extensión
                        list.add(new WoodType(name, "Maderas/" + file));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar catálogo de maderas desde assets", Toast.LENGTH_SHORT).show();
        }
        
        // Ordenar alfabéticamente
        Collections.sort(list, (w1, w2) -> w1.getName().compareToIgnoreCase(w2.getName()));
        
        return list;
    }
}
