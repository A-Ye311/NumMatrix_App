package com.example.sudoku.game;

import java.util.Random;

/**
 * Erstellt ein Sudoku-Puzzle in einer gewählten Schwierigkeit.
 * Die Lösung ist fest vorgegeben, es werden nur Felder entfernt.
 */
public class SudokuGenerator {
    private static final int SIZE = 9;
    private static final int BOX = 3;
    private static final int CELL_COUNT = SIZE * SIZE;
    private static final int EASY_REMOVE = 30;
    private static final int MEDIUM_REMOVE = 40;
    private static final int HARD_REMOVE = 50;

    private final Random rnd = new Random();

    /**
     * Enthält das sichtbare Puzzle und die passende Lösung.
     */
    public static class Puzzle {
        /** 0 bedeutet: Diese Zelle ist leer. */
        public final int[][] puzzle;
        /** Vollständige Lösung des Rätsels. */
        public final int[][] solution;
        /** Speichert Puzzle und Lösung zusammen. */
        public Puzzle(int[][] puzzle, int[][] solution) {
            this.puzzle = puzzle;
            this.solution = solution;
        }
    }

    /**
     * Baut ein neues Puzzle für die gewünschte Schwierigkeit.
    */
    public Puzzle generate(String difficulty) {
        int[][] sol = randomizedSolution();
        int[][] pz = deepCopy(sol);
        int remove = removedCellsForDifficulty(difficulty);

        removeCellsByBox(pz, remove);
        return new Puzzle(pz, sol);
    }

    private int[][] randomizedSolution() {
        int[][] grid = new int[SIZE][SIZE];
        fillGrid(grid, 0);
        return grid;
    }

    private boolean fillGrid(int[][] grid, int cellIndex) {
        if (cellIndex == CELL_COUNT) {
            return true;
        }

        int row = cellIndex / SIZE;
        int col = cellIndex % SIZE;
        int[] candidates = shuffledDigits();

        for (int value : candidates) {
            if (!isValidPlacement(grid, row, col, value)) {
                continue;
            }

            grid[row][col] = value;
            if (fillGrid(grid, cellIndex + 1)) {
                return true;
            }
            grid[row][col] = 0;
        }

        return false;
    }

    private boolean isValidPlacement(int[][] grid, int row, int col, int value) {
        for (int i = 0; i < SIZE; i++) {
            if (grid[row][i] == value || grid[i][col] == value) {
                return false;
            }
        }

        int startRow = (row / BOX) * BOX;
        int startCol = (col / BOX) * BOX;
        for (int r = startRow; r < startRow + BOX; r++) {
            for (int c = startCol; c < startCol + BOX; c++) {
                if (grid[r][c] == value) {
                    return false;
                }
            }
        }

        return true;
    }

    private int[] shuffledDigits() {
        int[] digits = {1,2,3,4,5,6,7,8,9};
        shuffleArray(digits);
        return digits;
    }

    private void shuffleArray(int[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int temp = values[i];
            values[i] = values[j];
            values[j] = temp;
        }
    }
    /**
     * Kopiert ein Sudoku-Feld, damit das Original gleich bleibt.
     */
    private static int[][] deepCopy(int[][] src) {
        int[][] out = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(src[r], 0, out[r], 0, SIZE);
        }
        return out;
    }
    /**
     * Legt fest, wie viele Felder je nach Schwierigkeit leer werden.
     */
    private static int removedCellsForDifficulty(String difficulty) {
        if (difficulty == null) {
            return EASY_REMOVE;
        }
        switch (difficulty.toUpperCase()) {
            case "MITTEL":
                return MEDIUM_REMOVE;
            case "SCHWER":
                return HARD_REMOVE;
            default:
                return EASY_REMOVE;
        }
    }
    /**
     * Entfernt Felder möglichst gleichmäßig in allen 3x3-Boxen.
     */
    private void removeCellsByBox(int[][] puzzle, int remove) {
        int perBox = remove / 9;
        int rest = remove % 9;

        for (int box = 0; box < 9; box++) {
            int target = perBox + (box < rest ? 1 : 0);
            removeFromBox(puzzle, box, target);
        }
    }
    /**
     * Entfernt in einer bestimmten Box zufällig einige Werte.
     */
    private void removeFromBox(int[][] puzzle, int box, int target) {
        int startR = (box / 3) * BOX;
        int startC = (box % 3) * BOX;

        int removed = 0;
        int guard = 0;
        while (removed < target && guard++ < 200) {
            int r = startR + rnd.nextInt(BOX);
            int c = startC + rnd.nextInt(BOX);
            if (puzzle[r][c] != 0) {
                puzzle[r][c] = 0;
                removed++;
            }
        }
    }
}
