package com.tamagotchi.Controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.tamagotchi.DataLayer.DatabaseHelper;
import com.tamagotchi.DataLayer.GalleryEntry;
import com.tamagotchi.Model.Blobbu.EvolutionType;
import com.tamagotchi.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {

    // Mapa: EvolutionType → ID del ImageView correspondiente en el layout
    private static final Map<EvolutionType, Integer> IMAGE_VIEW_IDS = new HashMap<>();
    static {
        IMAGE_VIEW_IDS.put(EvolutionType.BABY, R.id.img_baby);
        IMAGE_VIEW_IDS.put(EvolutionType.TEEN_MEW, R.id.img_teen_mew);
        IMAGE_VIEW_IDS.put(EvolutionType.TEEN_ART, R.id.img_teen_art);
        IMAGE_VIEW_IDS.put(EvolutionType.TEEN_TIDES, R.id.img_teen_tides);
        IMAGE_VIEW_IDS.put(EvolutionType.ADULT_MEW, R.id.img_adult_mew);
        IMAGE_VIEW_IDS.put(EvolutionType.ADULT_ART, R.id.img_adult_art);
        IMAGE_VIEW_IDS.put(EvolutionType.ADULT_MER, R.id.img_adult_mer);
        IMAGE_VIEW_IDS.put(EvolutionType.ADULT_SECRET, R.id.img_adult_secret);
    }

    // Mapa: EvolutionType → drawable desbloqueado
    private static final Map<EvolutionType, Integer> SPRITES = new HashMap<>();
    static {
        SPRITES.put(EvolutionType.BABY, R.drawable.baby_happy_1);
        SPRITES.put(EvolutionType.TEEN_MEW, R.drawable.mew_happy_2);
        SPRITES.put(EvolutionType.TEEN_ART, R.drawable.artsy_happy_1);
        SPRITES.put(EvolutionType.TEEN_TIDES, R.drawable.tides_happy_1);
        SPRITES.put(EvolutionType.ADULT_MEW, R.drawable.mew_adult_happy_1);
        SPRITES.put(EvolutionType.ADULT_ART, R.drawable.artsy_adult_happy_1);
        SPRITES.put(EvolutionType.ADULT_MER, R.drawable.mer_adult_happy_1);
        //SPRITES.put(EvolutionType.ADULT_SECRET, R.drawable.spr_adult_secret);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_gallery, container, false);

        view.findViewById(R.id.btn_close).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        loadGallery(view);
        return view;
    }

    private void loadGallery(View view) {
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        List<GalleryEntry> entries = db.getGallery();
        EvolutionType[] types = EvolutionType.values();

        for (GalleryEntry entry : entries) {
            // entry.creatureId == ordinal del EvolutionType
            if (entry.creatureId <= 0 || entry.creatureId >= types.length) continue;

            EvolutionType type = types[entry.creatureId];
            Integer imageViewId = IMAGE_VIEW_IDS.get(type);
            if (imageViewId == null) continue;

            ImageView img = view.findViewById(imageViewId);
            if (img == null) continue;

            if (entry.isUnlocked) {
                Integer sprite = SPRITES.get(type);
                if (sprite != null) img.setImageResource(sprite);
            }
            // Si está bloqueado, el layout ya pone spr_locked por defecto,
            // así que no hace falta hacer nada más
        }
    }
}