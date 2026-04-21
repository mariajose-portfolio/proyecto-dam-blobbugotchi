package com.example.blobbugotchi.Model.Blobbu;

public class Egg {
    private boolean hatched;

    public Egg() {
        this.hatched = false;
    }

    public boolean isHatched() {
        return hatched;
    }

    public void hatch() {
        this.hatched = true;
    }
}