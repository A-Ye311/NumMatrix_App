package com.example.sudoku.game;

public class SudokuGame {

    private final int[][] puzzle;
    private final int[][] solution;
    private final boolean[][] fixed;
    private int mistakes = 0;

    // ✅ 1 Argument: Puzzle-Objekt
    public SudokuGame(SudokuGenerator.Puzzle p) {
        this.puzzle = p.puzzle;
        this.solution = p.solution;

        fixed = new boolean[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                fixed[r][c] = puzzle[r][c] != 0;
            }
        }
    }

    // ✅ wird in GameActivity benutzt
    public int getCell(int r, int c) {
        return puzzle[r][c];
    }

    public int getMistakes() {
        return mistakes;
    }

    // returns: 0 ok, 1 mistake, 2 game over
    public int trySet(int r, int c, int value) {
        if (fixed[r][c]) return 0;
        if (value < 1 || value > 9) return 0;

        if (solution[r][c] == value) {
            puzzle[r][c] = value;
            return 0;
        } else {
            mistakes++;
            return mistakes >= 3 ? 2 : 1;
        }
    }

    public boolean isComplete() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (puzzle[r][c] == 0) return false;
            }
        }
        return true;
    }
}
