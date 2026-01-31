package com.example.sudoku.game;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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

        int remove = 30;
        if ("MITTEL".equals(difficulty)) remove = 40;
        if ("SCHWER".equals(difficulty)) remove = 50;

        removeCellsEvenly(pz, remove);
        return new Puzzle(pz, sol);
    }

    private int[][] deepCopy(int[][] src) {
        int[][] out = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(src[r], 0, out[r], 0, 9);
        }
        return out;
    }
    private void removeCellsEvenly(int[][] puzzle, int remove) {
        List<int[]>[] byBox = new ArrayList[9];
        for (int i = 0; i < 9; i++) {
            byBox[i] = new ArrayList<>();
        }
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int box = (r / 3) * 3 + (c / 3);
                byBox[box].add(new int[]{r, c});
            }
        }

        Random random = new Random();
        for (List<int[]> boxCells : byBox) {
            Collections.shuffle(boxCells, random);
        }

        int removed = 0;
        while (removed < remove) {
            boolean removedThisRound = false;
            for (List<int[]> boxCells : byBox) {
                if (removed >= remove) {
                    break;
                }
                if (!boxCells.isEmpty()) {
                    int[] cell = boxCells.remove(boxCells.size() - 1);
                    puzzle[cell[0]][cell[1]] = 0;
                    removed++;
                    removedThisRound = true;
                }
            }
            if (!removedThisRound) {
                break;
            }
        }
    }
}
