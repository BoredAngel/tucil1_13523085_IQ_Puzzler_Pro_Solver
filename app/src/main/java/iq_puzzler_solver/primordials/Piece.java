package iq_puzzler_solver.primordials;

public class Piece {
    int n, m;
    public char symbol;
    boolean[][] shape;
    static int numOfPieces = 0;

    public Piece(int n, int m, boolean shape[][], char symbol) {
        this.n = n;
        this.m = m;
        this.shape = new boolean[n][m];
        this.symbol = symbol;

        for (int i = 0; i < n; i++) {
            System.arraycopy(shape[i], 0, this.shape[i], 0, m);
        }

        Piece.numOfPieces++;
    }

    public Piece flip() {
        boolean newShape[][] = new boolean[this.n][this.m];

        for (int i = 0; i < this.n; i++) {
            for (int j = 0; j < this.m; j++) {
                newShape[i][j] = this.shape[i][this.m - j - 1];
            }
        }

        return new Piece(this.n, this.m, newShape, this.symbol);
    }

    public Piece rotate() {
        boolean newShape[][] = new boolean[this.m][this.n];

        for (int i = 0; i < this.m; i++) {
            for (int j = 0; j < this.n; j++) {
                newShape[i][j] = this.shape[this.n - j - 1][i];
            }
        }

        return new Piece(this.m, this.n, newShape, this.symbol);
    }

    public void printPiece() {
        for (int i = 0; i < this.n; i++) {
            for (int j = 0; j < this.m; j++) {
                if (this.shape[i][j]) System.out.print(this.symbol);
                else System.out.print("-");
            }
            System.out.println("|");
        }
        System.out.println("");
    }
}
