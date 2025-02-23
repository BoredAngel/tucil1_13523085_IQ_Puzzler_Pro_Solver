package iq_puzzler_solver.primordials;

import java.awt.Color;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
    public int n, m, p;
    public int[][] board;
    public List<Piece> pieces;
    private List<Color> colors;
    private boolean isSolved = false;
    public int iteration = 0;

    // create board with pieces
    public Board(int n, int m, List<Piece> pieces) {
        this.n = n;
        this.m = m;
        this.p = pieces.size();
        this.board = new int[n][m];
        this.pieces = pieces;
        this.colors = new ArrayList<>();
        
        // generate color
        Random random = new Random();
        float hue = random.nextFloat();
        for (int i = 0; i < 26; i++) {
            hue += 0.618033988749895f;
            hue %= 1; 
            Color color = Color.getHSBColor(hue, 0.9f, 0.9f);

            this.colors.add(color);
        }
    }
    
    // create board with path to txt file
    public Board(String pathToFile) {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        Input inp = new Input(Paths.get(currentDir, "../", pathToFile).toString());
        this.n = inp.n;
        this.m = inp.m;
        this.p = inp.p;
        this.board = new int[n][m];
        this.pieces = inp.pieces;
        this.colors = new ArrayList<>();

        // generate color
        Random random = new Random();
        float hue = random.nextFloat();
        for (int i = 0; i < 26; i++) {
            hue += 0.618033988749895f;
            hue %= 1; 
            Color color = Color.getHSBColor(hue, 0.9f, 0.9f);

            this.colors.add(color);
        }
    }

    // place a piece to the board
    private boolean placePiece(int x, int y, int piece_idx, int rot, int flip) {
        Piece cur_piece = this.pieces.get(piece_idx);
        
        // flip & rotate piece
        for (int i = 0; i < rot; i++) cur_piece = cur_piece.rotate();
        for (int i = 0; i < flip; i++) cur_piece = cur_piece.flip();

        // check if can be place
        for (int i = 0; i < cur_piece.n; i++) {
            for (int j = 0; j < cur_piece.m; j++) {
                if (cur_piece.shape[i][j]) {
                    if (x + i < 0 || x + i >= n || y + j < 0 || y + j >= m) return false;
                    if (this.board[x + i][y + j] != 0) return false;
                }
            }
        }

        // if valid, put the piece
        for (int i = 0; i < cur_piece.n; i++) {
            for (int j = 0; j < cur_piece.m; j++) {
                if (cur_piece.shape[i][j]) {
                    this.board[x + i][y + j] = piece_idx + 1;
                }
            }
        }

        return true;
    }

    // remove piece from the board (assuming you dont need to check cuz if placed means its valid, no?)
    private void removePiece(int x, int y, int piece_idx, int rot, int flip) {
        Piece cur_piece = this.pieces.get(piece_idx);

        // flip & rotate piece
        for (int i = 0; i < rot; i++) cur_piece = cur_piece.rotate();
        for (int i = 0; i < flip; i++) cur_piece = cur_piece.flip();

        for (int i = 0; i < cur_piece.n; i++) {
            for (int j = 0; j < cur_piece.m; j++) {
                if (cur_piece.shape[i][j]) {
                    this.board[x + i][y + j] = 0;
                }
            }
        }
    }

    private boolean Solve(int x, int y, boolean[] used_piece) {
        if (x >= this.n) return true;
        this.iteration++;
        for (int piece_idx = 0; piece_idx < this.p; piece_idx++) {      // every piece,
            for (int rot = 0; rot < 4; rot++) {                         // rotated,
                for (int flip = 0; flip < 2; flip++) {                  // flipped,
                    if (!used_piece[piece_idx]) {                       // check if used,
                        if (placePiece(x, y, piece_idx, rot, flip)) {   // placed if can.
                            used_piece[piece_idx] = true;
                            
                            int nextX = x;
                            int nextY = y;
                            // find next point
                            while (this.board[nextX][nextY] != 0) {
                                nextY++;
                                if (nextY >= this.m) {
                                    nextX++;
                                    nextY = 0;
                                }

                                if (nextX >= this.n) return true;
                            }
                            
                            // System.out.println("currently iterating x: " + nextX + ", y: " + nextY);
                            if (this.Solve(nextX, nextY, used_piece)) return true;
                            removePiece(x, y, piece_idx, rot, flip);
                            used_piece[piece_idx] = false;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean solve() {
        boolean[] used_piece = new boolean[this.p];
        
        Instant start = Instant.now();
        this.isSolved = this.Solve(0, 0, used_piece);
        Instant end = Instant.now();
        
        System.out.println("Execution Time: " + Duration.between(start, end).toMillis() + " ms.");
        
        if (this.isSolved) {
            this.isSolved = true;
            System.out.println("Found solution:");
            this.printBoard();
            return true;
        } else {
            System.out.println("no solution found lmao");
        }

        return false;
    }

    // prints the board with the symbols and distinct colors
    public void printBoard() {
        Color c;
        System.out.println("Iterated " + this.iteration + " Combinations");
        for (int i = 0; i < this.n; i++) {
            for (int j = 0; j < this.m; j++) {
                if (this.board[i][j] != 0) {
                    c = this.colors.get(this.board[i][j]);
                    System.out.print("\033[38;2;" + c.getRed() + ";" + c.getGreen() + ";" + c.getBlue() + "m");
                    System.out.print(this.pieces.get(this.board[i][j] - 1).symbol);
                }
            }
            System.out.println("");
        }
        System.out.println("\033[0m");
    }
}
