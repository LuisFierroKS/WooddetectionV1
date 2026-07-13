package com.example.wooddetectionv1;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class WoodClassifier {

    private Interpreter interpreter;
    private final List<String> labels = new ArrayList<>();

    public WoodClassifier(Context context) throws IOException {
        this(context, "best_float32.tflite");
    }

    public WoodClassifier(Context context, String modelPath) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        this.interpreter = new Interpreter(loadModelFile(context, modelPath), options);
        loadLabels(context);
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void loadLabels(Context context) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("labels.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    labels.add(line.trim());
                }
            }
        } catch (IOException e) {
            // Respuestas genéricas si no se encuentra el archivo de etiquetas
            for (int i = 0; i <= 34; i++) {
                labels.add("Especie Madera " + i);
            }
        }
    }

    public ClassificationResult classify(Bitmap bitmap) {
        if (interpreter == null) {
            throw new IllegalStateException("El clasificador no está inicializado.");
        }

        // YOLOv8/YOLO11 de clasificación TFLite espera entrada [1, 480, 480, 3] en FLOAT32
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);

        // Preprocesamiento de la imagen móvil: redimensión y normalización de píxeles a [0, 1]
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(480, 480, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0.0f, 255.0f))
                .build();

        TensorImage processedImage = imageProcessor.process(tensorImage);

        // Salida esperada por YOLO de clasificación: Tensor [1, 35] con las puntuaciones de clase
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 35}, DataType.FLOAT32);

        // Ejecutar inferencia
        interpreter.run(processedImage.getBuffer(), outputBuffer.getBuffer().rewind());

        // Encontrar la clase con mayor confianza
        float[] outputArray = outputBuffer.getFloatArray();
        int maxIdx = -1;
        float maxVal = -1.0f;

        for (int i = 0; i < outputArray.length; i++) {
            if (outputArray[i] > maxVal) {
                maxVal = outputArray[i];
                maxIdx = i;
            }
        }

        String className = (maxIdx != -1 && maxIdx < labels.size()) ? labels.get(maxIdx) : "Desconocido (" + maxIdx + ")";
        return new ClassificationResult(className, maxVal, maxIdx);
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public static class ClassificationResult {
        private final String label;
        private final float confidence;
        private final int classIndex;

        public ClassificationResult(String label, float confidence, int classIndex) {
            this.label = label;
            this.confidence = confidence;
            this.classIndex = classIndex;
        }

        public String getLabel() {
            return label;
        }

        public float getConfidence() {
            return confidence;
        }

        public int getClassIndex() {
            return classIndex;
        }
    }
}
