package com.example.sudoku.game;

public class SudokuGenerator {

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

        int remove = 35;
        if ("MITTEL".equals(difficulty)) remove = 45;
        if ("SCHWER".equals(difficulty)) remove = 55;

        int count = 0;
        outer:
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (count >= remove) break outer;
                pz[r][c] = 0;
                count++;
            }
        }

        return new Puzzle(pz, sol);
    }

    private int[][] deepCopy(int[][] src) {
        int[][] out = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(src[r], 0, out[r], 0, 9);
        }
        return out;
    }
}
