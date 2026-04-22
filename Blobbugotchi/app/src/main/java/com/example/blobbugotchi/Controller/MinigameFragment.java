package com.example.blobbugotchi.Controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.blobbugotchi.Model.Games.Minigame.Coin;
import com.example.blobbugotchi.Model.Games.Minigame.Enemy;
import com.example.blobbugotchi.Model.Games.Minigame.Player;
import com.example.blobbugotchi.Model.Games.Minigame.SpriteSheet;
import com.example.blobbugotchi.Model.Games.Minigame.Star;
import com.example.blobbugotchi.Model.Games.Minigame.TileMap;
import com.example.blobbugotchi.Model.Games.Minigame.Trap;
import com.example.blobbugotchi.R;
import com.example.blobbugotchi.View.MinigameActivity;

import java.util.ArrayList;
import java.util.List;

public class MinigameFragment extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    public static final int STATE_PLAYING = 0;
    public static final int STATE_WIN = 1;
    public static final int STATE_DEAD = 2;
    public static final int STATE_LEVEL_DONE = 3; // nivel completado, transición

    private int gameState = STATE_PLAYING;

    private Thread gameThread;
    private volatile boolean running = false;
    private SurfaceHolder holder;

    private TileMap tileMap;
    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Coin> coins = new ArrayList<>();
    private List<Star> stars = new ArrayList<>();
    private List<Trap> traps = new ArrayList<>();

    private int screenW, screenH;
    private int tileSize;
    private float mapOffsetX, mapOffsetY;

    // Nivel y puntuación acumulada
    private int currentLevel;
    private int accumulatedScore;

    // Joystick
    private float joystickBaseX, joystickBaseY;
    private float joystickKnobX, joystickKnobY;
    private float joystickRadius;
    private boolean joystickActive = false;
    private int joystickPointerId = -1;
    private int activeDirection = 0;

    private Paint wallPaint, waterPaint, exitPaint;
    private Paint joystickBasePaint, joystickKnobPaint;
    private Paint uiTextPaint, uiShadowPaint, uiHeartPaint, uiLevelPaint;
    private Paint overlayPaint;

    public interface GameListener {
        void onGameOver(int score);
        void onWin(int score);
        void onLevelComplete(int nextLevel, int accumulatedScore);
        void onPlaySound(int soundType);
    }

    private GameListener listener;

    public MinigameFragment(Context context, GameListener listener, int level, int accumulatedScore) {
        super(context);
        this.listener = listener;
        this.currentLevel = level;
        this.accumulatedScore = accumulatedScore;
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
        initPaints();
    }

    private void initPaints() {
        wallPaint = new Paint(); wallPaint.setColor(0xFF063467);
        waterPaint = new Paint(); waterPaint.setColor(0xFF58C0DB);
        waterPaint = new Paint(); waterPaint.setColor(0xFF58C0DB);
        exitPaint = new Paint(); exitPaint.setColor(0xFF00FF88);

        uiTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        uiTextPaint.setColor(0xFFFFFFFF);
        uiTextPaint.setTextSize(36f);
        uiTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        uiShadowPaint = new Paint(uiTextPaint);
        uiShadowPaint.setColor(0xFF050C12);
        uiShadowPaint.setMaskFilter(new BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL));

        uiHeartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        uiHeartPaint.setColor(0xFFFF3355);
        uiHeartPaint.setTextSize(52f);
        uiHeartPaint.setTypeface(Typeface.DEFAULT_BOLD);

        uiLevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        uiLevelPaint.setColor(0xFFFFD700);
        uiLevelPaint.setTextSize(32f);
        uiLevelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        uiLevelPaint.setTextAlign(Paint.Align.CENTER);

        joystickBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        joystickBasePaint.setColor(0x55FFFFFF);

        joystickKnobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        joystickKnobPaint.setColor(0xAAFFFFFF);

        overlayPaint = new Paint();
        overlayPaint.setColor(0xCC050C12);
    }

    private void initGame() {
        tileMap = new TileMap(screenW, screenH, currentLevel);
        tileSize = tileMap.getTileSize();

        int mapPixelW = TileMap.COLS * tileSize;
        int mapPixelH = TileMap.ROWS * tileSize;
        mapOffsetX = (screenW - mapPixelW) / 2f;
        mapOffsetY = (screenH - mapPixelH) / 2f;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        Bitmap bmpPlayer = BitmapFactory.decodeResource(getResources(), R.drawable.player, opts);
        Bitmap bmpEnemy = BitmapFactory.decodeResource(getResources(), R.drawable.enemy, opts);
        Bitmap bmpStar = BitmapFactory.decodeResource(getResources(), R.drawable.star, opts);
        Bitmap bmpCoin = BitmapFactory.decodeResource(getResources(), R.drawable.coin, opts);

        BitmapFactory.Options optsHook = new BitmapFactory.Options();
        optsHook.inScaled = false;
        optsHook.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmpHook = BitmapFactory.decodeResource(getResources(), R.drawable.hook, optsHook);

        SpriteSheet ssPlayer = (bmpPlayer != null)
                ? new SpriteSheet(bmpPlayer, 3, 3, 9, 8f, true) : null;
        if (ssPlayer != null) ssPlayer.setDirection(2);

        SpriteSheet ssHook = (bmpHook != null)
                ? new SpriteSheet(bmpHook, 1, 1, 1, 1f, false) : null;

        // La puntuación acumulada se pasa al Player
        player = new Player(1, 1, tileSize, ssPlayer, accumulatedScore);

        // Velocidad de enemigos aumenta con el nivel
        float enemySpeed = 1.5f + (currentLevel - 1) * 0.4f;

        enemies.clear();
        int[][] enemyPos = getEnemyPositions();
        for (int[] pos : enemyPos) {
            SpriteSheet ss = (bmpEnemy != null)
                    ? new SpriteSheet(bmpEnemy, 3, 3, 9, 6f, true) : null;
            if (ss != null) ss.setDirection(2);
            enemies.add(new Enemy(pos[0], pos[1], tileSize, ss, enemySpeed));
        }

        coins.clear();
        for (int[] pos : getCoinPositions()) {
            SpriteSheet ss = (bmpCoin != null)
                    ? new SpriteSheet(bmpCoin, 1, 1, 1, 1f, false) : null;
            coins.add(new Coin(pos[0], pos[1], tileSize, ss));
        }

        stars.clear();
        for (int[] pos : getStarPositions()) {
            SpriteSheet ss = (bmpStar != null)
                    ? new SpriteSheet(bmpStar, 4, 1, 4, 8f, true) : null;
            stars.add(new Star(pos[0], pos[1], tileSize, ss));
        }

        traps.clear();
        for (int[] pos : getTrapPositions()) {
            traps.add(new Trap(pos[0], pos[1], tileSize, ssHook));
        }

        joystickRadius = screenH * 0.12f;
        joystickBaseX = joystickRadius * 2.0f;
        joystickBaseY = screenH - joystickRadius * 1.8f;
        joystickKnobX = joystickBaseX;
        joystickKnobY = joystickBaseY;
    }

    private int[][] getEnemyPositions() {
        switch (currentLevel) {
            case 2: return new int[][]{{3,7},{15,7},{17,11}};
            case 3: return new int[][]{{14,3},{3,7},{15,7},{17,11}};
            default: return new int[][]{{7,1},{13,5},{5,9},{16,9}};
        }
    }

    private int[][] getCoinPositions() {
        switch (currentLevel) {
            case 2: return new int[][]{
                    {3,1},{5,1},{9,1},{11,1},{15,1},{17,1},
                    {3,3},{7,3},{9,3},{13,3},{17,3},
                    {5,5},{9,5},{13,5},{17,5},
                    {7,7},{13,7},{17,7},
                    {3,9},{7,9},{11,9},{15,9},{17,9},
                    {3,11},{7,11},{11,11},{15,11}
            };
            case 3: return new int[][]{
                    {3,1},{5,1},{9,1},{11,1},{15,1},{17,1},
                    {3,3},{7,3},{9,3},{13,3},{17,3},
                    {5,5},{9,5},{13,5},{17,5},
                    {7,7},{13,7},{17,7},
                    {3,9},{7,9},{11,9},{15,9},{17,9},
                    {3,11},{7,11},{11,11},{15,11}
            };
            default: return new int[][]{
                    {2,1},{9,1},{13,1},
                    {5,2},{17,2},
                    {1,3},{9,3},{17,3},
                    {5,4},{13,4},
                    {1,5},{9,5},{17,5},
                    {3,6},{11,6},
                    {13,7},{18,7},
                    {9,10},{18,10},
                    {3,11}
            };
        }
    }

    private int[][] getStarPositions() {
        switch (currentLevel) {
            case 2: return new int[][]{{11,3},{5,9}};
            case 3: return new int[][]{{11,3},{5,9}};
            default: return new int[][]{{3,5},{18,8}};
        }
    }

    private int[][] getTrapPositions() {
        switch (currentLevel) {
            case 2: return new int[][]{{15,5},{5,7},{13,11}};
            case 3: return new int[][]{{5,3},{15,5},{5,7},{11,7},{13,11}};
            default: return new int[][]{{4,3},{11,5},{9,7},{3,9},{9,11}};
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (running) {
            long now   = System.nanoTime();
            float delta = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            if (delta > 0.05f) {
                delta = 0.05f;
            }

            if (player == null || tileMap == null) {
                try {
                    Thread.sleep(16);
                }
                catch (InterruptedException ignored) {}

                lastTime = System.nanoTime();

                continue;
            }

            if (gameState == STATE_PLAYING) {
                update(delta);
            }
            render();

            long sleepMs = 16 - (System.nanoTime() - now) / 1_000_000;
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                }
                catch (InterruptedException ignored) {}
            }
        }
    }

    private void update(float delta) {
        player.setDirection(activeDirection);
        player.update(delta, tileMap);

        for (Enemy e : enemies) {
            e.update(delta, tileMap);
        }

        for (Coin c : coins) {
            c.update(delta);
        }

        for (Star s : stars) {
            s.update(delta);
        }

        for (Coin c : coins) {
            if (!c.isCollected() && RectF.intersects(player.getBounds(), c.getBounds())) {
                c.collect();
                player.addScore(Coin.POINTS);

                if (listener != null) {
                    listener.onPlaySound(0);
                }
            }
        }

        for (Star s : stars) {
            if (!s.isCollected() && RectF.intersects(player.getBounds(), s.getBounds())) {
                s.collect();
                player.addLife();

                if (listener != null) {
                    listener.onPlaySound(2);
                }
            }
        }

        for (Trap t : traps) {
            if (RectF.intersects(player.getBounds(), t.getBounds())) {
                player.takeDamage();

                if (listener != null) {
                    listener.onPlaySound(1);
                }
            }
        }

        for (Enemy e : enemies) {
            if (RectF.intersects(player.getBounds(), e.getBounds())) {
                player.takeDamage();

                if (listener != null) {
                    listener.onPlaySound(1);
                }
            }
        }

        if (player.isDead()) {
            gameState = STATE_DEAD;

            if (listener != null) {
                listener.onGameOver(player.getScore());
            }

            return;
        }

        if (tileMap.isExit(player.getTileCol(tileMap), player.getTileRow(tileMap))) {
            if (currentLevel < 3) {
                gameState = STATE_LEVEL_DONE;

                if (listener != null) {
                    listener.onLevelComplete(currentLevel + 1, player.getScore());
                }
            }
            else {
                gameState = STATE_WIN;

                if (listener != null) {
                    listener.onWin(player.getScore());
                }
            }
        }
    }

    private void render() {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }

        if (player == null || tileMap == null) {
            canvas.drawColor(0xFF0a1a2e);
            holder.unlockCanvasAndPost(canvas);

            return;
        }

        canvas.drawColor(0xFF0a1a2e);

        canvas.save();
        canvas.translate(mapOffsetX, mapOffsetY);
        drawMap(canvas);

        for (Trap t : traps) {
            t.draw(canvas);
        }

        for (Coin c : coins) {
            c.draw(canvas);
        }

        for (Star s : stars) {
            s.draw(canvas);
        }

        for (Enemy e : enemies) {
            e.draw(canvas);
        }

        player.draw(canvas);
        canvas.restore();

        drawHUD(canvas);
        drawJoystick(canvas);
        if (gameState == STATE_LEVEL_DONE) {
            drawLevelDoneOverlay(canvas);
        }
        else if (gameState != STATE_PLAYING) {
            drawOverlay(canvas);
        }

        holder.unlockCanvasAndPost(canvas);
    }

    private void drawMap(Canvas canvas) {
        for (int row = 0; row < TileMap.ROWS; row++) {
            for (int col = 0; col < TileMap.COLS; col++) {
                int tile = tileMap.getTile(col, row);
                Paint p = tile == TileMap.TILE_WALL ? wallPaint :
                        tile == TileMap.TILE_EXIT  ? exitPaint : waterPaint;
                canvas.drawRect(col * tileSize, row * tileSize,
                        (col+1) * tileSize, (row+1) * tileSize, p);
            }
        }
    }

    private void drawHUD(Canvas canvas) {
        // Nivel centrado arriba
        canvas.drawText("Nivel " + currentLevel, screenW / 2f, 40f, uiLevelPaint);

        // Corazones — esquina superior izquierda
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < player.getLives(); i++) hearts.append("♥ ");
        canvas.drawText(hearts.toString(), 30f, 62f, uiHeartPaint);

        // Puntuación debajo
        String scoreText = "Puntos: " + player.getScore();
        canvas.drawText(scoreText, 34f, 108f, uiShadowPaint);
        canvas.drawText(scoreText, 30f, 104f, uiTextPaint);
    }

    private void drawJoystick(Canvas canvas) {
        canvas.drawCircle(joystickBaseX, joystickBaseY, joystickRadius, joystickBasePaint);
        canvas.drawCircle(joystickKnobX, joystickKnobY, joystickRadius * 0.5f, joystickKnobPaint);
    }

    private void drawLevelDoneOverlay(Canvas canvas) {
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);
        Paint big = new Paint(uiTextPaint); big.setTextSize(64f); big.setTextAlign(Paint.Align.CENTER);
        Paint med = new Paint(uiTextPaint); med.setTextSize(36f); med.setTextAlign(Paint.Align.CENTER);

        canvas.drawText("¡Nivel " + currentLevel + " superado!", screenW/2f, screenH/2f - 40, big);
        canvas.drawText("Puntos: " + player.getScore(), screenW/2f, screenH/2f + 20, med);
        canvas.drawText("Cargando nivel " + (currentLevel+1) + "...", screenW/2f, screenH/2f + 70, med);
    }

    private void drawOverlay(Canvas canvas) {
        canvas.drawRect(0, 0, screenW, screenH, overlayPaint);
        String title = gameState == STATE_WIN ? "¡ESCAPASTE!" : "GAME OVER";
        Paint big = new Paint(uiTextPaint); big.setTextSize(64f); big.setTextAlign(Paint.Align.CENTER);
        Paint med = new Paint(uiTextPaint); med.setTextSize(36f); med.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(title, screenW/2f, screenH/2f - 40, big);
        canvas.drawText("Puntuación: " + player.getScore(), screenW/2f, screenH/2f + 20, med);
        canvas.drawText("Toca para volver al menú", screenW/2f, screenH/2f + 80, med);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameState == STATE_DEAD || gameState == STATE_WIN) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ((MinigameActivity) getContext()).finish();
            }

            return true;
        }

        if (gameState == STATE_LEVEL_DONE) {
            return true; // esperar transición automática
        }

        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                float tx = event.getX(actionIndex), ty = event.getY(actionIndex);
                float dist = (float) Math.hypot(tx - joystickBaseX, ty - joystickBaseY);

                if (!joystickActive && dist < joystickRadius * 2f) {
                    joystickActive = true;
                    joystickPointerId = pointerId;
                    updateJoystick(tx, ty);
                }

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if (event.getPointerId(i) == joystickPointerId) {
                        updateJoystick(event.getX(i), event.getY(i));
                    }
                }

                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (pointerId == joystickPointerId) {
                    joystickActive = false;
                    joystickPointerId = -1;
                    joystickKnobX = joystickBaseX;
                    joystickKnobY = joystickBaseY;
                    activeDirection = 0;
                }

                break;
            }
        }
        return true;
    }

    private void updateJoystick(float tx, float ty) {
        float dx = tx - joystickBaseX, dy = ty - joystickBaseY;
        float dist = (float) Math.hypot(dx, dy);

        if (dist > joystickRadius) {
            dx = dx/dist*joystickRadius;
            dy = dy/dist*joystickRadius;
        }

        joystickKnobX = joystickBaseX + dx;
        joystickKnobY = joystickBaseY + dy;

        if (dist < joystickRadius * 0.25f) {
            activeDirection = 0; return;
        }

        if (Math.abs(dx) > Math.abs(dy)) {
            activeDirection = dx > 0 ? 4 : 3;
        }
        else {
            activeDirection = dy > 0 ? 2 : 1;
        }
    }

    @Override public void surfaceCreated(SurfaceHolder h)  {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override public void surfaceChanged(SurfaceHolder h, int format, int w, int height) {
        screenW = w;
        screenH = height;

        initGame();
    }

    @Override public void surfaceDestroyed(SurfaceHolder h) {
        running = false;
        try {
            gameThread.join();
        }
        catch (InterruptedException ignored) {}
    }

    public void pause() {
        running = false;
    }

    public void resume() {
        if (!running) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }
}