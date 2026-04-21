package com.example.blobbugotchi.DataLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.blobbugotchi.Model.Blobbu.Blobbu;
import com.example.blobbugotchi.Model.Blobbu.EvolutionType;
import com.example.blobbugotchi.Model.Config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "blobbugotchi.db";
    private static final int DB_VERSION = 4;

    // Nombres de tablas y columnas
    private static final String TABLE_BLOBBU  = "blobbu";
    private static final String TABLE_GALLERY = "gallery_entry";
    private static final String TABLE_CONFIG  = "config";

    // Columnas Blobbu
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_EVOLUTION = "evolutionType";
    private static final String COL_PREV_EVOLUTION = "previousEvolutionType";
    private static final String COL_HAPPY = "happyLvl";
    private static final String COL_HUNGRY = "hungryLvl";
    private static final String COL_SLEEPINESS  = "sleepinessLvl";
    private static final String COL_CARE_MISTAKES = "careMistakes";
    private static final String COL_TIME_TOGETHER = "timeTogether";
    private static final String COL_MAX_SCORE = "maxScore";

    // Columnas GalleryEntry
    private static final String COL_CREATURE_ID = "creatureId";
    private static final String COL_IS_UNLOCKED = "isUnlocked";

    // Columnas Config
    private static final String COL_BGM_VOLUME = "bgmVolume";
    private static final String COL_BGS_VOLUME = "bgsVolume";
    private static final String COL_ME_VOLUME = "meVolume";
    private static final String COL_SE_VOLUME = "seVolume";
    private static final String COL_MASTER_VOLUME = "masterVolume";
    private static final String COL_LAST_EVOLUTION_TS = "lastEvolutionTs";


    private static DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }

        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // --- CREACIÓN DE LAS TABLAS ---
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla Blobbu
        // previousEvolutionType puede ser NULL (el bebé aún no ha evolucionado antes)
        db.execSQL("CREATE TABLE " + TABLE_BLOBBU + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_EVOLUTION + " TEXT, " +
                COL_PREV_EVOLUTION + " TEXT, " +
                COL_HAPPY + " INTEGER, " +
                COL_HUNGRY + " INTEGER, " +
                COL_SLEEPINESS + " INTEGER, " +
                COL_CARE_MISTAKES + " INTEGER, " +
                COL_TIME_TOGETHER + " REAL, " +
                COL_MAX_SCORE + " INTEGER)");

        // Tabla Galería
        db.execSQL("CREATE TABLE " + TABLE_GALLERY + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CREATURE_ID + " INTEGER UNIQUE, " +
                COL_IS_UNLOCKED + " INTEGER)"); // 0 = bloqueado, 1 = desbloqueado

        // Tabla Configuración
        db.execSQL("CREATE TABLE " + TABLE_CONFIG + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BGM_VOLUME + " REAL, " +
                COL_BGS_VOLUME + " REAL, " +
                COL_ME_VOLUME + " REAL, " +
                COL_SE_VOLUME + " REAL, " +
                COL_MASTER_VOLUME + " REAL, " +
                COL_LAST_EVOLUTION_TS + " INTEGER)");  // Tiempo entre evoluciones

        db.execSQL("INSERT INTO " + TABLE_CONFIG + " VALUES (1, 1.0, 1.0, 1.0, 1.0, 1.0, " +
                System.currentTimeMillis() + ")");

        // Insertar todas las criaturas de la galería como bloqueadas por defecto
        // El ID corresponde al ordinal del EvolutionType (sin EGG=0)
        for (int i = 1; i < EvolutionType.values().length; i++) {
            db.execSQL("INSERT INTO " + TABLE_GALLERY + " (" +
                    COL_CREATURE_ID + ", " + COL_IS_UNLOCKED + ") VALUES (" + i + ", 0)");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_BLOBBU +
                    " ADD COLUMN " + COL_PREV_EVOLUTION + " TEXT");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_CONFIG +
                    " ADD COLUMN " + COL_LAST_EVOLUTION_TS + " INTEGER DEFAULT "
                    + System.currentTimeMillis());
        }
    }

    // --- BLOBBU ---

    /**
     * Inserta un nuevo Blobbu en la BD y devuelve su ID
     */
    public long insertBlobbu(Blobbu blobbu) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = blobbuToValues(blobbu);
        return db.insert(TABLE_BLOBBU, null, values);
    }

    /**
     * Actualiza el Blobbu existente (siempre hay uno solo, id=1)
     */
    public int updateBlobbu(Blobbu blobbu) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = blobbuToValues(blobbu);
        return db.update(TABLE_BLOBBU, values, COL_ID + "=1", null);
    }

    /**
     * Carga el Blobbu guardado. Devuelve null si no existe.
     */
    public Blobbu getBlobbu() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_BLOBBU, null,
                COL_ID + "=1", null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Blobbu blobbu = cursorToBlobbu(cursor);
            cursor.close();
            return blobbu;
        }
        return null;
    }

    /**
     * Guarda el tiempo total del pomodoro acumulado en el Blobbu
     */
    public void savePomodoroTime(double hours) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_BLOBBU +
                " SET " + COL_TIME_TOGETHER + " = " + COL_TIME_TOGETHER + " + " + hours +
                " WHERE " + COL_ID + " = 1");
    }

    /**
     * Guarda la puntuación máxima del minijuego
     */
    public void saveMaxScore(int score) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MAX_SCORE, score);
        db.update(TABLE_BLOBBU, values, COL_ID + "=1", null);
    }

    // --- GALERÍA ---

    /**
     * Desbloquea una criatura en la galería por su ID de evolución
     */
    public void unlockCreature(int creatureId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_UNLOCKED, 1);
        db.update(TABLE_GALLERY, values,
                COL_CREATURE_ID + "=?", new String[]{String.valueOf(creatureId)});
    }

    /**
     * Devuelve la lista de entradas de la galería
     */
    public List<GalleryEntry> getGallery() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GALLERY, null,
                null, null, null, null, COL_CREATURE_ID + " ASC");

        List<GalleryEntry> entries = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                GalleryEntry entry = new GalleryEntry();
                entry.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                entry.creatureId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CREATURE_ID));
                entry.isUnlocked = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_UNLOCKED)) == 1;
                entries.add(entry);
            }
            cursor.close();
        }
        return entries;
    }

    /**
     * Guarda el timestamp (ms epoch) del momento de la última evolución.
     */
    public void saveLastEvolutionTimestamp(long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LAST_EVOLUTION_TS, timestamp);
        db.update(TABLE_CONFIG, values, COL_ID + "=1", null);
    }

    /**
     * Recupera el timestamp de la última evolución.
     * Devuelve el momento actual si no hay ninguno guardado (primera vez).
     */
    public long getLastEvolutionTimestamp() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONFIG,
                new String[]{COL_LAST_EVOLUTION_TS},
                COL_ID + "=1", null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            long ts = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LAST_EVOLUTION_TS));
            cursor.close();
            return ts;
        }
        return System.currentTimeMillis();
    }

    // --- CONFIGURACIÓN ---

    /**
     * Guarda la configuración de volúmenes
     */
    public void saveConfiguration(Configuration config) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BGM_VOLUME, config.getBgmVolume());
        values.put(COL_BGS_VOLUME, config.getBgsVolume());
        values.put(COL_ME_VOLUME, config.getMeVolume());
        values.put(COL_SE_VOLUME, config.getSeVolume());
        values.put(COL_MASTER_VOLUME, config.getMasterVolume());
        db.update(TABLE_CONFIG, values, COL_ID + "=1", null);
    }

    /**
     * Carga la configuración guardada. Devuelve valores por defecto si no existe.
     */
    public Configuration loadConfiguration() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONFIG, null,
                COL_ID + "=1", null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Configuration config = new Configuration(
                    cursor.getFloat(cursor.getColumnIndexOrThrow(COL_BGM_VOLUME)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(COL_BGS_VOLUME)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(COL_ME_VOLUME)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(COL_SE_VOLUME)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(COL_MASTER_VOLUME))
            );
            cursor.close();
            return config;
        }

        return new Configuration(1f, 1f, 1f, 1f, 1f); // valores por defecto
    }

    // --- HELPERS PRIVADOS ---

    private ContentValues blobbuToValues(Blobbu blobbu) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, blobbu.getName());
        values.put(COL_EVOLUTION, blobbu.getEvolutionType().name());
        // previousEvolutionType puede ser null si el Blobbu nunca ha evolucionado antes
        values.put(COL_PREV_EVOLUTION,
                blobbu.getPreviousEvolutionType() != null
                        ? blobbu.getPreviousEvolutionType().name()
                        : null);
        values.put(COL_HAPPY, blobbu.getHappyLvl());
        values.put(COL_HUNGRY, blobbu.getHungryLvl());
        values.put(COL_SLEEPINESS, blobbu.getSleepinessLvl());
        values.put(COL_CARE_MISTAKES, blobbu.getCareMistakes());
        values.put(COL_TIME_TOGETHER, blobbu.getTimeTogether());
        values.put(COL_MAX_SCORE, blobbu.getMaxScore());
        return values;
    }

    private Blobbu cursorToBlobbu(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
        String evolutionStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVOLUTION));
        int happyLvl = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HAPPY));
        int hungryLvl = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HUNGRY));
        int sleepinessLvl = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SLEEPINESS));
        int careMistakes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CARE_MISTAKES));
        double timeTogether = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TIME_TOGETHER));
        int maxScore = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MAX_SCORE));

        // previousEvolutionType es nullable: puede no existir si el Blobbu aún es bebé
        int prevColIndex = cursor.getColumnIndex(COL_PREV_EVOLUTION);
        EvolutionType prevEvolution = null;

        if (prevColIndex != -1 && !cursor.isNull(prevColIndex)) {
            prevEvolution = EvolutionType.valueOf(cursor.getString(prevColIndex));
        }

        return Blobbu.fromDatabase(name, EvolutionType.valueOf(evolutionStr),
                prevEvolution, happyLvl, hungryLvl, sleepinessLvl,
                careMistakes, timeTogether, maxScore);
    }

    public void ensureGalleryRows() {
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 1; i < EvolutionType.values().length; i++) {
            // INSERT OR IGNORE no falla si ya existe (UNIQUE en creatureId)
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_GALLERY + " (" +
                    COL_CREATURE_ID + ", " + COL_IS_UNLOCKED + ") VALUES (" + i + ", 0)");
        }
    }

    // Para poder reiniciar la partida
    public void resetAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_BLOBBU);
        db.execSQL("DELETE FROM " + TABLE_GALLERY);
        for (int i = 1; i < EvolutionType.values().length; i++) {
            db.execSQL("INSERT INTO " + TABLE_GALLERY +
                    " (creatureId, isUnlocked) VALUES (" + i + ", 0)");
        }
        // Sin tocar config por compatibilidad
    }
}