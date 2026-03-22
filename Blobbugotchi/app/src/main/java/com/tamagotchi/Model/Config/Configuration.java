package com.tamagotchi.Model.Config;

public class Configuration {
    float bgm_vlm; // Background music
    float bgs_vlm; // Ambient music
    float me_vlm; // Melody music
    float se_vlm; // Sound effects
    float volume; // Global sound of the app

    public Configuration() { // Inicia con el volumen a la mitad
        this.bgm_vlm = 5;
        this.bgs_vlm = 5;
        this.me_vlm = 5;
        this.se_vlm = 5;
        this.volume = 5;
    }

    public float getBgm_vlm() {
        return bgm_vlm;
    }

    public void setBgm_vlm(float bgm_vlm) {
        this.bgm_vlm = bgm_vlm;
    }

    public float getBgs_vlm() {
        return bgs_vlm;
    }

    public void setBgs_vlm(float bgs_vlm) {
        this.bgs_vlm = bgs_vlm;
    }

    public float getMe_vlm() {
        return me_vlm;
    }

    public void setMe_vlm(float me_vlm) {
        this.me_vlm = me_vlm;
    }

    public float getSe_vlm() {
        return se_vlm;
    }

    public void setSe_vlm(float se_vlm) {
        this.se_vlm = se_vlm;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
