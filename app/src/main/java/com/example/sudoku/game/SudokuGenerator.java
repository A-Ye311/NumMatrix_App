package com.example.sudoku.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuGenerator {

    private static final int SIZE = 9;
    private static final int BOX_SIZE = 3;
    private static final int EASY_REMOVE = 30;
    private static final int MEDIUM_REMOVE = 40;
    private static final int HARD_REMOVE = 50;

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

        removeCellsEvenly(pz, remove);
        return new Puzzle(pz, sol);
    }

    private int[][] deepCopy(int[][] src) {
        int[][] out = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(src[r], 0, out[r], 0, SIZE);
        }
        return out;
    }

    private int removedCellsForDifficulty(String difficulty) {
        if ("MITTEL".equals(difficulty)) {
            return MEDIUM_REMOVE;
        }
        if ("SCHWER".equals(difficulty)) {
            return HARD_REMOVE;
        }
        return EASY_REMOVE;
    }

    private void removeCellsEvenly(int[][] puzzle, int remove) {
        List<List<int[]>> byBox = buildCellsByBox();
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

    private List<List<int[]>> buildCellsByBox() {
        List<List<int[]>> byBox = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            byBox.add(new ArrayList<>());
        }
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int box = (r / BOX_SIZE) * BOX_SIZE + (c / BOX_SIZE);
                byBox.get(box).add(new int[]{r, c});
            }
        }
        return byBox;
    }
}
