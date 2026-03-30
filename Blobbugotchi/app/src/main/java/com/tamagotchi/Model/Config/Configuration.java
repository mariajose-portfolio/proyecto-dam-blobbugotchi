package com.tamagotchi.Model.Config;

public class Configuration {
    private float bgmVolume;
    private float bgsVolume;
    private float meVolume;
    private float seVolume;
    private float masterVolume;

    public Configuration(float bgmVolume, float bgsVolume, float meVolume,
                         float seVolume, float masterVolume) {
        this.bgmVolume = bgmVolume;
        this.bgsVolume = bgsVolume;
        this.meVolume = meVolume;
        this.seVolume = seVolume;
        this.masterVolume = masterVolume;
    }

    public float getBgmVolume() {
        return bgmVolume;
    }
    public float getBgsVolume() {
        return bgsVolume;
    }
    public float getMeVolume() {
        return meVolume;
    }
    public float getSeVolume() {
        return seVolume;
    }
    public float getMasterVolume() {
        return masterVolume;
    }
}