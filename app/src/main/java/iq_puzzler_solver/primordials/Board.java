package iq_puzzler_solver.primordials;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

public class Board {
    public int n, m, p;
    public int[][] board;
    public List<Piece> pieces;
    public List<Color> colors;
    public boolean isSolved = false;
    public int iteration = 0;
    public long exec_time;

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
        // String currentDir = Paths.get("").toAbsolutePath().toString();
        System.out.println(pathToFile);
        Input inp = new Input(pathToFile);
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
        this.exec_time = Duration.between(start, end).toMillis();
        System.out.println("Execution Time: " + this.exec_time + " ms.");
        
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
        System.out.println("Iteration: " + this.iteration + " Combinations");
        for (int i = 0; i < this.n; i++) {
            for (int j = 0; j < this.m; j++) {
                if (this.board[i][j] != 0) {
                    c = this.colors.get(this.board[i][j] - 1);
                    System.out.print("\033[38;2;" + c.getRed() + ";" + c.getGreen() + ";" + c.getBlue() + "m");
                    System.out.print(this.pieces.get(this.board[i][j] - 1).symbol);
                }
            }
            System.out.println("");
        }
        System.out.println("\033[0m");
    }

    // save the solution to txt
    public void saveToTxt(String path) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("Solution:\n");
            for (int i = 0; i < this.n; i++) {
                for (int j = 0; j < this.m; j++) {
                    if (this.board[i][j] == 0) writer.write("  ");
                    else writer.write(this.pieces.get(this.board[i][j] - 1).symbol);
                }
                writer.write("\n");
            }
            
            writer.write("\nExecution Time: " + this.exec_time + " ms\n");
            writer.write("Iterations: " + this.iteration + " times\n");
            
            System.out.println("Solution saved at: " + path);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while saving to txt file: " + e.getMessage());
        }

    }

    // save solution to png image
    public void saveToImg(String path) {
        final int TILE_SIZE = 30;   
        final int MARGIN = 25;  

        int w = this.m * TILE_SIZE + 2 * MARGIN;
        int h = this.n * TILE_SIZE + 5 * MARGIN;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Set the background color
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);

        // fill the board
        for (int i = 0; i < this.n; i++) {
            for (int j = 0; j < this.m; j++) {
                int x = j * TILE_SIZE + MARGIN;
                int y = i * TILE_SIZE + 2 * MARGIN;
                if (this.board[i][j] != 0) {
                    g2d.setColor(this.colors.get(this.board[i][j] - 1));
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(String.valueOf(this.pieces.get(this.board[i][j] - 1).symbol),
                    x + TILE_SIZE/2 - 5, y + TILE_SIZE/2 + 5);
                }
            }
        }

        // draw the board lines
        g2d.setColor(Color.BLACK);
        for (int i = 0; i <= this.m; i++) {
            g2d.drawLine(i * TILE_SIZE + MARGIN, 2 * MARGIN, i * TILE_SIZE + MARGIN, this.n * TILE_SIZE + 2 * MARGIN);
        }
        for (int i = 0; i <= this.n; i++) {
            g2d.drawLine(MARGIN, i * TILE_SIZE + 2 * MARGIN, this.m * TILE_SIZE + MARGIN, i * TILE_SIZE + 2 * MARGIN);
        }

        // draw the texts
        String title = "Puzzle Solution";
        int textW1 = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (w - textW1) / 2, (int) 1.5 * MARGIN);

        String exec_time_string = "Execution Time: " + this.exec_time;
        int textW2 = g2d.getFontMetrics().stringWidth(exec_time_string);
        g2d.drawString(exec_time_string, (w - textW2) / 2, this.n * TILE_SIZE + 3 * MARGIN);

        String iterations_string = "Iterations: " + this.iteration;
        int textW3 = g2d.getFontMetrics().stringWidth(iterations_string);
        g2d.drawString(iterations_string, (w - textW3) / 2, this.n * TILE_SIZE + 4 * MARGIN);

        // Save the image as a PNG file
        File outputFile = new File(path);
        try {
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("Solution saved at: " + path);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while saving the image: " + e.getMessage());
        } finally {
            g2d.dispose();
        }
    }
}
