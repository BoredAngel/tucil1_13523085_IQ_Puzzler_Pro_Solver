package iq_puzzler_solver.primordials;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Input {
    public int n, m, p;
    public String mode;
    public List<Piece> pieces = new ArrayList<>();
    private int total_piece_size = 0;

    // parse input from a txt file and save it to this class
    public Input(String path) {
        try {
            File f = new File(path);
            Scanner s = new Scanner(f);
            int i = 0;
            char cur_piece_symbol = '-';
            List<String> cur_piece_shape = new ArrayList<>();

            while (s.hasNextLine()) {
                // get the n, m, and p values from the txt file
                // n and m is the board size (n*m)
                // p is the number of pieces
                if (i == 0) {
                    String line = s.nextLine();
                    String[] vals = line.split(" ");
                    int[] values;
                    if (vals.length != 3) throw new IllegalArgumentException("Error: Invalid N, M, and P Value");
                    try {
                        values = Arrays.stream(vals).mapToInt(Integer::parseInt).toArray();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalArgumentException("Error: Invalid N, M, and P Value");
                    }
                    this.n = values[0];    
                    this.m = values[1];    
                    this.p = values[2];   
                    if (this.p > 26) throw new IllegalArgumentException("Error: Invalid P Value. can't be more than 26");
                    i++;
                }
                // get the mode of the board ("DEFAULT", "CUSTOM", "PYRAMID") i aint doin pyramid tho
                else if (i == 1) {
                    String line = s.nextLine();
                    line = line.strip();
                    
                    if (!"DEFAULT".equals(line) && !"CUSTOM".equals(line)) {
                        throw new IllegalArgumentException("Error: Invalid board mode. valid mode: 'DEFAULT', 'CUSTOM'.");
                    }
                    
                    this.mode = line;
                    i++;
                }
                // get the pieces 
                else {
                    String line = s.nextLine();

                    if (line.isBlank()) throw new IllegalArgumentException("Error: Empty line in Piece");

                    if (line.indexOf(cur_piece_symbol) == -1) {
                        // create new piece
                        if (cur_piece_symbol != '-') {
                            parsePiece(cur_piece_shape, cur_piece_symbol);
                            cur_piece_shape.clear(); // clear the temp piece                            
                        }
                        
                        // change 'cur_piece_symbol' to the new symbol
                        boolean isValidPieceSymbol = false;
                        for (char c : line.toCharArray()) {
                            if (isValidSymbol(c)) {
                                cur_piece_symbol = c;
                                isValidPieceSymbol = true;                            
                                break;
                            }
                        }
                        
                        // invalid symbol (A-Z only)
                        if (!isValidPieceSymbol) throw new IllegalArgumentException("Error: Invalid piece symbol. (A-Z only)");
                    } 

                    char cur_symbol = cur_piece_symbol;
                    if (line.chars().allMatch(c -> c == cur_symbol || c == ' '))  {
                        // add line to current temporary shape
                        cur_piece_shape.add(line.stripTrailing());
                        
                    } else {
                        // Multiple symbols in a single line: AAAB
                        System.out.println(line);
                        throw new IllegalArgumentException("Error: Invalid piece shape. Multiple symbols in a single line");
                    }
                }
            }
            // parse last piece
            parsePiece(cur_piece_shape, cur_piece_symbol);

            // check if total piece size is equal to board size
            if (this.n * this.m != this.total_piece_size) {
                throw new IllegalArgumentException("Error: Total piece size is not equal to board size");
            } 

            // check if number of piece is equal to the given value
            if (this.pieces.size() != this.p) {
                throw new IllegalArgumentException("Error: Number of piece is not equal to the given value P");
            } 

            if (this.pieces.size() > 26) throw new IllegalArgumentException("Error: Pieces can't be more than 26");

            s.close();
        } catch (IllegalArgumentException e) {
            throw e; 
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error: Something went wrong. (bruhhh TC nya apa cik)");
        }
    }

    public void parsePiece(List<String> raw_shape, char symbol) {
        int n, m = 0;
        boolean[][] shape;
        boolean isValid = true;

        // find n and m
        n = raw_shape.size();
        for (String s : raw_shape) {
            m = (m < s.length()) ? s.length() : m;
        }

        // if piece is bigger than board
        if (n > this.n || m > this.m) throw new IllegalArgumentException("Error: Invalid piece shape. Piece is bigger than the board");

        shape = new boolean[n][m];
        int startX = -1, startY = -1;
        
        // assign shape
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (raw_shape.get(i).length() > j && raw_shape.get(i).charAt(j) != ' ') {
                    shape[i][j] = true;
                    this.total_piece_size++;
                    startX = i;
                    startY = j;
                }
                else shape[i][j] = false;
            }
        }
        
        boolean[][] connected = new boolean[n][m];

        // check if shape is connected
        Queue<Point> q = new LinkedList<>();
        q.add(new Point(startX, startY));

        while (!q.isEmpty()) {            
            Point cur_point = q.poll();
            int x = cur_point.x;
            int y = cur_point.y;

            // System.err.println("Symbol: " + symbol + ", x: " + x + ", y: " + y);
            connected[x][y] = true;

            // horizontal vertical
            if (x > 0 && shape[x - 1][y] && !connected[x - 1][y]) q.add(new Point(x - 1, y));
            if (x < n - 1 && shape[x + 1][y] && !connected[x + 1][y]) q.add(new Point(x + 1, y));
            if (y > 0 && shape[x][y - 1] && !connected[x][y - 1]) q.add(new Point(x, y - 1));
            if (y < m - 1 && shape[x][y + 1] && !connected[x][y + 1]) q.add(new Point(x, y + 1));

            //diagonal (bruh got scammed. terkena hoax cik)
            // if (x > 0 && y > 0 && shape[x - 1][y - 1] && !connected[x - 1][y - 1]) q.add(new Point(x - 1, y - 1));
            // if (x > 0 && y < m - 1 && shape[x - 1][y + 1] && !connected[x - 1][y + 1]) q.add(new Point(x - 1, y + 1));
            // if (x < n - 1 && y > 0 && shape[x + 1][y - 1] && !connected[x + 1][y - 1]) q.add(new Point(x + 1, y - 1));
            // if (x < n - 1 && y < m - 1 && shape[x + 1][y + 1] && !connected[x + 1][y + 1]) q.add(new Point(x + 1, y + 1));
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (shape[i][j] != connected[i][j]) {
                    throw new IllegalArgumentException("Error: Invalid piece shape. Disconnected shape");
                }
            }
        }

        this.pieces.add(new Piece(n, m, shape, symbol));
    }

    private boolean isValidSymbol(char symbol) {
        return symbol >= 'A' && symbol <= 'Z';
    }
}
