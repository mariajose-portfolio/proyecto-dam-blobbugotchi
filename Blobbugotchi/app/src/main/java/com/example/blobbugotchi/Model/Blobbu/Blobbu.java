package com.example.blobbugotchi.Model.Blobbu;

public class Blobbu {
    private String name;

    // De 0 a 100, 100 = no necesita nada, 0 = necesita atención
    private int happyLvl;
    private int hungryLvl;
    private int sleepinessLvl;
    private int careMistakes;
    private double timeTogether;
    private int maxScore;
    private BlobbuState currentState; // Control del estado
    private EvolutionType evolutionType;
    private EvolutionType previousEvolutionType;

    // Umbrales de estado
    private static final int CRITICAL = 20;
    private static final int LOW = 30;
    private static final int HIGH = 80;

    public Blobbu(String name) {
        this.name = name;
        this.happyLvl = 50;
        this.hungryLvl = 50;
        this.sleepinessLvl = 50;
        this.careMistakes = 0;
        this.timeTogether = 0;
        this.maxScore = 0;
        this.evolutionType = EvolutionType.EGG;

        updateState();
    }

    public String getName() {
        return name;
    }

    public int getHappyLvl() {
        return happyLvl;
    }

    public int getHungryLvl() {
        return hungryLvl;
    }

    public int getSleepinessLvl() {
        return sleepinessLvl;
    }

    public int getCareMistakes() {
        return careMistakes;
    }

    public double getTimeTogether() {
        return timeTogether;
    }

    public EvolutionType getPreviousEvolutionType() {
        return previousEvolutionType;
    }

    public BlobbuState getCurrentState() {
        return currentState;
    }

    public void setState(BlobbuState state) {
        this.currentState = state;
    }

    /**
     * Acción de alimentar al blobbu.
     * Aumenta el nivel de hambre (está más saciado).
     */
    public void eat(int amount) {
        hungryLvl = clamp(hungryLvl + amount);
        updateState();
    }

    /**
     * Acción de dormir.
     * Aumenta el nivel de descanso.
     */
    public void sleep(int amount) {
        sleepinessLvl = clamp(sleepinessLvl + amount);
        updateState();
    }

    /**
     * Acción de jugar.
     * Aumenta el nivel de felicidad.
     */
    public void play(int amount) {
        happyLvl = clamp(happyLvl + amount);
        updateState();
    }

    /**
     * Simula el paso del tiempo.
     * Con el tiempo el blobbu empeora sus estadísticas y necesita atención.
     */
    public void passTime() {
        hungryLvl = clamp(hungryLvl - 1);
        sleepinessLvl = clamp(sleepinessLvl - 1);
        happyLvl = clamp(happyLvl - 1);
        timeTogether += 5.0 / 3600.0; // Cada tick representa 5 segundos = 5/3600 horas
        updateState();
    }

    public void addTimeTogether(double hours) {
        this.timeTogether += hours;
    }

    // Getter de evolutionType
    public EvolutionType getEvolutionType() {
        return evolutionType; // asegúrate de tener este atributo
    }

    // Getter de maxScore
    public int getMaxScore() {
        return maxScore;
    }

    /**
     * Si el jugador no atiende a tiempo una necesidad,
     * se añade un error de cuidado
     */
    public void addCareMistake() {
        careMistakes++;
    }

    /**
     * Comprueba el estado de las estadísticas del blobbu para saber
     * qué animación es la que tiene que mostrar en la pantalla.
     */
    public void updateState() {
        if (sleepinessLvl < CRITICAL) {
            currentState = BlobbuState.SLEEPING;
        }
        else if (hungryLvl < LOW) {
            currentState = BlobbuState.HUNGRY;
        }
        else if (happyLvl < LOW) {
            currentState = BlobbuState.SAD;
        }
        else if (happyLvl > HIGH) {
            currentState = BlobbuState.HAPPY;
        }
        else {
            currentState = BlobbuState.IDLE;
        }
    }

    /**
     * Crea el blobbu en fase de bebé con sus stats iniciales, en caso
     * de querer cambiar el cómo serán las stats al nacer se pueden retocar
     * aquí directamente
     * @return Devuelve el Blobbu creado
     */
    public static Blobbu createBaby() {
        Blobbu blobbu = new Blobbu("Baby pearl");
        blobbu.happyLvl = 0;
        blobbu.hungryLvl = 0;
        blobbu.sleepinessLvl = 100;
        blobbu.careMistakes = 0;
        blobbu.timeTogether = 0;

        blobbu.updateState();

        return blobbu;
    }

    public void evolve(EvolutionType newType) {
        this.previousEvolutionType = this.evolutionType;
        this.evolutionType = newType;
        this.careMistakes = 0;
        updateState();
    }

    // Constructor estático para cargar desde BD
    public static Blobbu fromDatabase(String name, EvolutionType evolutionType,
                                      EvolutionType prevEvolutionType, int happyLvl, int hungryLvl,
                                      int sleepinessLvl, int careMistakes, double timeTogether, int maxScore) {
        Blobbu blobbu = new Blobbu(name);
        blobbu.evolutionType = evolutionType;
        blobbu.previousEvolutionType = prevEvolutionType;
        blobbu.happyLvl = happyLvl;
        blobbu.hungryLvl = hungryLvl;
        blobbu.sleepinessLvl = sleepinessLvl;
        blobbu.careMistakes = careMistakes;
        blobbu.timeTogether = timeTogether;
        blobbu.maxScore = maxScore;
        blobbu.updateState();

        return blobbu;
    }

    /**
     * Garantiza que los niveles de las estadísticas nunca sean superiores a 100
     * o inferiores a 0; en caso de serlo, los ajusta a 100 o 0 respectivamente.
     */
    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}