package com.example.sudoku.game;

public class SudokuGame {

    private static final int SIZE = 9;
    private static final int MAX_MISTAKES = 3;
    private final int[][] puzzle;
    private final int[][] solution;
    private final boolean[][] fixed;
    private int mistakes = 0;

    public SudokuGame(SudokuGenerator.Puzzle p) {
        this.puzzle = p.puzzle;
        this.solution = p.solution;
        this.fixed = buildFixed(puzzle);
    }

    public int getCell(int r, int c) {
        return puzzle[r][c];
    }

    public int getMistakes() {
        return mistakes;
    }

    // returns: 0 ok, 1 mistake, 2 game over
    public int trySet(int r, int c, int value) {
        if (fixed[r][c] || !isValidValue(value)) return 0;

        if (solution[r][c] == value) {
            puzzle[r][c] = value;
            return 0;
        } else {
            mistakes++;
            return mistakes >= MAX_MISTAKES ? 2 : 1;
        }
    }

    public boolean isComplete() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (puzzle[r][c] == 0) return false;
            }
        }
        return true;
    }

    private static boolean[][] buildFixed(int[][] puzzle) {
        boolean[][] fixed = new boolean[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                fixed[r][c] = puzzle[r][c] != 0;
            }
        }
        return fixed;
    }

    private static boolean isValidValue(int value) {
        return value >= 1 && value <= SIZE;
    }
}
