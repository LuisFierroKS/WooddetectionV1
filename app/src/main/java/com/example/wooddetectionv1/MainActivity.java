package com.example.wooddetectionv1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import java.io.InputStream;
import com.example.wooddetectionv1.woodtypes.WoodTypesActivity;

public class MainActivity extends AppCompatActivity {

    private WoodClassifier woodClassifier;

    // Vistas
    private ImageView woodImageView;
    private LinearLayout placeholderLayout;
    private TextView resultText;
    private TextView confidenceText;
    private ProgressBar confidenceProgress;
    private Button btnCamera;
    private Button btnGallery;
    private Button btnWoodTypes;

    // Lanzador para capturar foto de la cámara (retorna una miniatura en Bitmap)
    private final ActivityResultLauncher<Void> takePicturePreviewLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    analizarYMostrarImagen(bitmap);
                } else {
                    Toast.makeText(MainActivity.this, "Captura cancelada", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Lanzador para seleccionar foto de la galería (retorna la Uri de la imagen)
    private final ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Bitmap bitmap = loadBitmapFromUri(uri);
                    if (bitmap != null) {
                        analizarYMostrarImagen(bitmap);
                    } else {
                        Toast.makeText(MainActivity.this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        woodImageView = findViewById(R.id.woodImageView);
        placeholderLayout = findViewById(R.id.placeholderLayout);
        resultText = findViewById(R.id.resultText);
        confidenceText = findViewById(R.id.confidenceText);
        confidenceProgress = findViewById(R.id.confidenceProgress);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        // Inicializar clasificador de madera TFLite
        try {
            woodClassifier = new WoodClassifier(this);
        } catch (Exception e) {
            resultText.setText("Error de Carga");
            Toast.makeText(this, "Error al cargar modelo TFLite: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        // Listeners de los botones
        btnCamera.setOnClickListener(v -> takePicturePreviewLauncher.launch(null));

        btnGallery.setOnClickListener(v -> selectImageLauncher.launch("image/*"));

        btnWoodTypes = findViewById(R.id.btnWoodTypes);
        btnWoodTypes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WoodTypesActivity.class);
            startActivity(intent);
        });
    }

    private void analizarYMostrarImagen(Bitmap bitmap) {
        // Mostrar la imagen en la vista y ocultar el marcador de posición
        woodImageView.setVisibility(View.VISIBLE);
        woodImageView.setImageBitmap(bitmap);
        placeholderLayout.setVisibility(View.GONE);

        // Ejecutar la inferencia
        try {
            WoodClassifier.ClassificationResult result = woodClassifier.classify(bitmap);

            // Mostrar resultados en la UI
            resultText.setText(result.getLabel());
            confidenceText.setVisibility(View.VISIBLE);
            confidenceText.setText("Confianza: " + (int) (result.getConfidence() * 100) + "%");

            confidenceProgress.setVisibility(View.VISIBLE);
            confidenceProgress.setProgress((int) (result.getConfidence() * 100));
        } catch (Exception e) {
            resultText.setText("Error de análisis");
            Toast.makeText(this, "Error en inferencia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private Bitmap loadBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (woodClassifier != null) {
            woodClassifier.close(); // Libera recursos del modelo
        }
    }
}
