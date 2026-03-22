package com.tamagotchi.DataLayer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tamagotchi.Model.Blobbu.Blobbu;
import com.tamagotchi.Model.Config.Configuration;

import java.util.List;


public class DatabaseHelper extends AppCompatActivity {
    private String DB_NAME;
    private int DC_VERSION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onUpgrade(String db, int version, int newVersion){

    }

    public long insertBlobbu(Blobbu blobbu){
        return 20;
    }

    public int updateBlobbu(Blobbu blobbu){
        return 20;
    }

    public Blobbu getBlobbu(int id){
        return new Blobbu("ejemplo");
    }

    public void savePomodorTimer(double time){

    }

    public void saveMaxScore(int score){

    }

    public void unlockCreature(int id){

    }

    public List getGallery(){
        return null;
    }

    public void saveConfiguration(Configuration config){

    }

    public Configuration loadConfiguration(){
        return null;
    }
}