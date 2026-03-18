package com.example.sudoku.game;

/**
 * Verwaltet den aktuellen Stand eines Sudoku-Spiels.
 * Die Klasse merkt sich das Spielfeld, die Lösung und die Fehlerzahl.
 */
public class SudokuGame {

    private static final int SIZE = 9;
    private static final int MAX_MISTAKES = 3;

    /** Das sichtbare Spielfeld. */
    private final int[][] puzzle;
    /** Die fertige Lösung des Sudoku. */
    private final int[][] solution;
    /** Merkt sich, welche Felder schon vorgegeben waren. */
    private final boolean[][] fixed;
    private int mistakes = 0;

    /** Erstellt ein neues Spiel mit Puzzle und Lösung. */
    public SudokuGame(SudokuGenerator.Puzzle p) {
        this.puzzle = p.puzzle;
        this.solution = p.solution;

        fixed = new boolean[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                fixed[r][c] = puzzle[r][c] != 0;
            }
        }
    }
    /** Liefert den aktuellen Wert einer Zelle zurück. */
    public int getCell(int r, int c) {
        return puzzle[r][c];
    }
    /** Liefert die aktuelle Anzahl an Fehlern zurück. */
    public int getMistakes() {
        return mistakes;
    }

    /**
     * Prüft eine Eingabe des Spielers.
     * Rückgabe:
     * 0 = Eingabe war ok,
     * 1 = Eingabe war falsch,
     * 2 = zu viele Fehler, Spiel vorbei.
     */
    public int trySet(int r, int c, int value) {
        if (fixed[r][c] || value < 1 || value > 9) {
            return 0;
        }

        if (solution[r][c] == value) {
            puzzle[r][c] = value;
            return 0;
        }

        return (++mistakes >= MAX_MISTAKES) ? 2 : 1;
    }

    /** Prüft, ob kein Feld mehr leer ist. */
    public boolean isComplete() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (puzzle[r][c] == 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
