package com.example.sudoku.game;

import java.util.Random;

public class SudokuGenerator {
    private static final int SIZE = 9;
    private static final int BOX = 3;

    private static final int EASY_REMOVE = 30;
    private static final int MEDIUM_REMOVE = 40;
    private static final int HARD_REMOVE = 50;

    private final Random rnd = new Random();

    public static class Puzzle {
        public final int[][] puzzle;   // 0 = leer
        public final int[][] solution;

        public Puzzle(int[][] puzzle, int[][] solution) {
            this.puzzle = puzzle;
            this.solution = solution;
        }
    }

    public Puzzle generate(String difficulty) {
        int[][] sol = {
                {5,3,4,6,7,8,9,1,2},
                {6,7,2,1,9,5,3,4,8},
                {1,9,8,3,4,2,5,6,7},
                {8,5,9,7,6,1,4,2,3},
                {4,2,6,8,5,3,7,9,1},
                {7,1,3,9,2,4,8,5,6},
                {9,6,1,5,3,7,2,8,4},
                {2,8,7,4,1,9,6,3,5},
                {3,4,5,2,8,6,1,7,9}
        };

        int[][] pz = deepCopy(sol);
        int remove = removedCellsForDifficulty(difficulty);

        removeCellsByBox(pz, remove);
        return new Puzzle(pz, sol);
    }

    private static int[][] deepCopy(int[][] src) {
        int[][] out = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) System.arraycopy(src[r], 0, out[r], 0, SIZE);
        return out;
    }

    private static int removedCellsForDifficulty(String difficulty) {
        if (difficulty == null) return EASY_REMOVE;
        switch (difficulty.toUpperCase()) {
            case "MITTEL": return MEDIUM_REMOVE;
            case "SCHWER": return HARD_REMOVE;
            default: return EASY_REMOVE;
        }
    }

    private void removeCellsByBox(int[][] puzzle, int remove) {
        int perBox = remove / 9;
        int rest = remove % 9;

        // erst pro Box gleichmäßig löschen
        for (int box = 0; box < 9; box++) {
            int target = perBox + (box < rest ? 1 : 0);
            removeFromBox(puzzle, box, target);
        }
    }

    private void removeFromBox(int[][] puzzle, int box, int target) {
        int startR = (box / 3) * BOX;
        int startC = (box % 3) * BOX;

        int removed = 0;
        int guard = 0; // verhindert Endlosschleife, falls Box schon leer wäre
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
