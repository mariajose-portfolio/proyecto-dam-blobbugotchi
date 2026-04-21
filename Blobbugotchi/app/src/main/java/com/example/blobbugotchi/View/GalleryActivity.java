package com.example.blobbugotchi.View;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.blobbugotchi.DataLayer.DatabaseHelper;
import com.example.blobbugotchi.DataLayer.GalleryEntry;
import com.example.blobbugotchi.Model.Blobbu.Blobbu;
import com.example.blobbugotchi.Model.Blobbu.EvolutionType;
import com.example.blobbugotchi.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        DatabaseHelper db = DatabaseHelper.getInstance(this);

        // Corregir filas duplicadas y sincronizar con el Blobbu actual
        db.ensureGalleryRows();
        syncGalleryWithCurrentBlobbu(db);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        loadGallery();
    }

    private void syncGalleryWithCurrentBlobbu(DatabaseHelper db) {
        Blobbu blobbu = db.getBlobbu();

        if (blobbu == null) return;

        EvolutionType current = blobbu.getEvolutionType();

        if (current != EvolutionType.EGG) {
            db.unlockCreature(current.ordinal());
        }

        if (current != EvolutionType.EGG && current != EvolutionType.BABY) {
            db.unlockCreature(EvolutionType.BABY.ordinal());
        }

        EvolutionType prev = blobbu.getPreviousEvolutionType();
        if (prev != null) {
            db.unlockCreature(prev.ordinal());
        }
    }

    private void loadGallery() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        List<GalleryEntry> entries = db.getGallery();
        EvolutionType[] types = EvolutionType.values();

        Map<EvolutionType, Integer> imageViewIds = new HashMap<>();
        imageViewIds.put(EvolutionType.BABY, R.id.img_baby);
        imageViewIds.put(EvolutionType.TEEN_MEW, R.id.img_teen_mew);
        imageViewIds.put(EvolutionType.TEEN_ART, R.id.img_teen_art);
        imageViewIds.put(EvolutionType.TEEN_TIDES, R.id.img_teen_tides);
        imageViewIds.put(EvolutionType.ADULT_MEW, R.id.img_adult_mew);
        imageViewIds.put(EvolutionType.ADULT_ART, R.id.img_adult_art);
        imageViewIds.put(EvolutionType.ADULT_MER, R.id.img_adult_mer);
        imageViewIds.put(EvolutionType.ADULT_SECRET, R.id.img_adult_secret);

        Map<EvolutionType, Integer> sprites = new HashMap<>();
        sprites.put(EvolutionType.BABY, R.drawable.baby_happy_1);
        sprites.put(EvolutionType.TEEN_MEW, R.drawable.mew_happy_1);
        sprites.put(EvolutionType.TEEN_ART, R.drawable.artsy_happy_1);
        sprites.put(EvolutionType.TEEN_TIDES, R.drawable.tides_happy_1);
        sprites.put(EvolutionType.ADULT_MEW, R.drawable.mew_adult_happy_1);
        sprites.put(EvolutionType.ADULT_ART, R.drawable.artsy_adult_happy_1);
        sprites.put(EvolutionType.ADULT_MER, R.drawable.mer_adult_happy_1);

        for (GalleryEntry entry : entries) {
            if (entry.creatureId <= 0 || entry.creatureId >= types.length) continue;

            EvolutionType type = types[entry.creatureId];
            Integer viewId = imageViewIds.get(type);
            if (viewId == null) continue;

            ImageView img = findViewById(viewId);
            if (img == null) continue;

            if (entry.isUnlocked) {
                Integer sprite = sprites.get(type);
                if (sprite != null) img.setImageResource(sprite);
            }
        }
    }
}