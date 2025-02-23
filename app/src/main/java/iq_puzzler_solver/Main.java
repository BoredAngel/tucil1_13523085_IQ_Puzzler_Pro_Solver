package iq_puzzler_solver;

import iq_puzzler_solver.primordials.Board;


public class Main {
    public static void main(String[] args) {
        Board board = new Board("input.txt");
        board.solve();
    }

}
