package com.example.wooddetectionv1.woodtypes.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssetImageLoader {
    private static AssetImageLoader instance;
    private final LruCache<String, Bitmap> memoryCache;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private AssetImageLoader() {
        // Usa 1/8 de la memoria disponible para caché
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        executorService = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized AssetImageLoader getInstance() {
        if (instance == null) {
            instance = new AssetImageLoader();
        }
        return instance;
    }

    public void loadImage(Context context, String assetPath, ImageView imageView) {
        imageView.setTag(assetPath);

        // Buscar en caché en memoria
        Bitmap cachedBitmap = memoryCache.get(assetPath);
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        // Limpiar la imagen mientras carga para evitar visualización incorrecta en reciclado
        imageView.setImageDrawable(null);

        // Cargar asíncronamente
        executorService.submit(() -> {
            Bitmap bitmap = loadScaledBitmapFromAsset(context, assetPath, 250, 250);
            if (bitmap != null) {
                memoryCache.put(assetPath, bitmap);
                
                // Mostrar en hilo principal verificando que el tag siga siendo el mismo
                mainHandler.post(() -> {
                    if (assetPath.equals(imageView.getTag())) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    private Bitmap loadScaledBitmapFromAsset(Context context, String assetPath, int reqWidth, int reqHeight) {
        try {
            // Decodificar con inJustDecodeBounds=true para obtener dimensiones
            InputStream stream = context.getAssets().open(assetPath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);
            stream.close();

            // Calcular el factor de escala (inSampleSize)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decodificar con el tamaño final
            options.inJustDecodeBounds = false;
            stream = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
            stream.close();

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
