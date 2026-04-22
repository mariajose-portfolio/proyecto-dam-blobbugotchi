package com.example.blobbugotchi.Model.Games.Minigame;

public class TileMap {

    public static final int TILE_WATER = 0;
    public static final int TILE_WALL  = 1;
    public static final int TILE_EXIT  = 2;

    public static final int COLS = 20;
    public static final int ROWS = 13;

    // Nivel 1
    private static final int[][] MAP1 = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,1},
            {1,0,1,0,1,0,1,0,1,0,1,0,0,0,1,0,1,0,1,1},
            {1,0,1,0,0,0,1,0,0,0,1,1,1,0,1,0,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,0,1,0,0,0,1,1,1,0,1,1},
            {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,0,1,1},
            {1,1,1,0,1,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0,1,1,0,1},
            {1,0,0,0,1,0,0,0,1,0,0,0,1,0,1,0,0,0,0,1},
            {1,1,1,0,1,1,1,0,0,0,1,0,1,0,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1},
    };

    // Nivel 2
    private static final int[][] MAP2 = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,1,0,0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,1},
            {1,0,1,0,1,0,1,0,1,1,1,0,1,0,1,0,1,0,1,1},
            {1,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,0,1,1},
            {1,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,1,0,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
            {1,0,1,1,1,0,1,1,1,1,1,0,1,1,1,0,1,1,1,1},
            {1,0,0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,1},
            {1,1,1,0,1,1,1,0,1,0,1,1,1,0,1,0,1,1,0,1},
            {1,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,1,0,0,1},
            {1,0,1,1,1,0,1,0,1,1,1,1,1,0,1,1,1,0,1,1},
            {1,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1},
    };

    // Nivel 3
    private static final int[][] MAP3 = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,1},
            {1,0,1,1,1,0,1,0,1,0,1,0,1,1,1,0,1,0,1,1},
            {1,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,1},
            {1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1},
            {1,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1},
            {1,1,1,0,1,0,1,1,1,0,1,1,1,0,1,0,1,0,1,1},
            {1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,1},
            {1,0,1,1,1,1,1,0,1,1,1,0,1,1,1,0,1,1,0,1},
            {1,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,1},
            {1,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,1,0,1,1},
            {1,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1},
    };

    private static final int[][][] MAPS = {MAP1, MAP2, MAP3};
    private int[][] currentMap;
    private int tileSize;

    public TileMap(int screenW, int screenH, int level) {
        currentMap = MAPS[Math.max(0, Math.min(level - 1, 2))];
        int base = Math.min(screenW / COLS, screenH / ROWS);
        tileSize = (int)(base * 1.10f);
    }

    public int getTileSize() { return tileSize; }

    public int getTile(int col, int row) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return TILE_WALL;
        return currentMap[row][col];
    }

    public boolean isWall(int col, int row) {
        return getTile(col, row) == TILE_WALL;
    }

    public boolean isExit(int col, int row) {
        return getTile(col, row) == TILE_EXIT;
    }

    public int toCol(float px) {
        return (int)(px / tileSize);
    }

    public int toRow(float py) {
        return (int)(py / tileSize);
    }
}