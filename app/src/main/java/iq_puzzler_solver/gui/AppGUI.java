package iq_puzzler_solver.gui;

import java.io.File;

import iq_puzzler_solver.primordials.Board;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AppGUI extends Application{
    private IntegerProperty width = new SimpleIntegerProperty(5);
    private IntegerProperty height = new SimpleIntegerProperty(5);
    private final int MAX_SIZE = 350;
    private Canvas canvas = new Canvas();
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private Board board = null;
    
    private Label uploadLabel = new Label();
    private Button SaveTxtBtn = new Button("Save to txt file");
    private Button SaveImgBtn = new Button("Save to png file");

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("IQ Puzzler Pro Solver");  
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button loadButton = new Button("Load File");
        loadButton.setOnAction(e -> {
            this.uploadLabel.setText("Loading...");
            openFile(primaryStage);
        });

        SaveTxtBtn.setVisible(false);
        SaveImgBtn.setVisible(false);

        SaveTxtBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            // Set the file chooser extension filters (optional)
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Txt File to save solution", "*.txt"));
            
            // Show the save dialog to select a location and file name
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            if (selectedFile != null) {
                // Create the new file
                this.board.saveToTxt(selectedFile.getAbsolutePath());
            }
        });

        SaveImgBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            // Set the file chooser extension filters (optional)
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Png File to save solution", "*.png"));
            
            // Show the save dialog to select a location and file name
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            if (selectedFile != null) {
                // Create the new file
                this.board.saveToImg(selectedFile.getAbsolutePath());
            }
        });

        HBox buttonBox = new HBox(10, SaveTxtBtn, SaveImgBtn);
        buttonBox.setAlignment(Pos.CENTER);


        drawBoard();

        VBox root = new VBox(10, titleLabel, this.canvas, loadButton, this.uploadLabel, buttonBox);
        root.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        // uhhh 
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> Math.min(root.getWidth(), this.MAX_SIZE),
            root.widthProperty()
        ));
        canvas.heightProperty().bind(Bindings.createDoubleBinding(
            () -> Math.min(canvas.getWidth() * ((double) this.height.get() / this.width.get()), this.MAX_SIZE),
            canvas.widthProperty()
        ));

        // Redraw when size changes
        this.canvas.widthProperty().addListener((obs, oldVal, newVal) -> drawBoard());
        this.canvas.heightProperty().addListener((obs, oldVal, newVal) -> drawBoard());

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("IQ Puzzler Pro solver");
        primaryStage.show();
    }

    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Grid File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Input txt Files", "*.txt"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                System.out.println("Loading file: " + file.getAbsolutePath());
                this.board = new Board(file.getAbsolutePath());
                
                String msg;
                if (this.board.solve()) {
                    msg = "Solution Found! :)\n";
                    SaveTxtBtn.setVisible(true);
                    SaveImgBtn.setVisible(true);
                }
                else {
                    msg = "No Solution Found :(\n";
                    SaveTxtBtn.setVisible(false);
                    SaveImgBtn.setVisible(false);
                }
                msg = msg.concat("Execution Time: " + this.board.exec_time + " ms\n"); 
                msg = msg.concat("Iterations: " + this.board.iteration + " times");

                this.uploadLabel.setText(msg);
                this.height.set(board.n);
                this.width.set(board.m);
                drawBoard();
                
            } catch (IllegalArgumentException e) {
                this.uploadLabel.setText(e.getMessage());
                SaveTxtBtn.setVisible(false);
                SaveImgBtn.setVisible(false);
            }
        }
    }

    private void drawBoard() {
        double tileSize;
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (this.board != null) {    
            tileSize = this.getTileSize();
            for (int i = 0; i < board.n; i++) {
                for (int j = 0; j < board.m; j++) {
                    if (board.board[i][j] > 0) {
                        java.awt.Color c = board.colors.get(board.board[i][j] - 1);
                        Color color = Color.rgb(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 255);
                        gc.setFill(color); 
                        gc.fillRect(j * tileSize, i * tileSize, tileSize, tileSize);

                        gc.setStroke(Color.BLACK);
                        gc.strokeText(String.valueOf(board.pieces.get(board.board[i][j] - 1).symbol),
                                      j * tileSize + tileSize/2 - 5, i * tileSize + tileSize/2 + 5);
                    }   
                }
            }
        } else {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        tileSize = this.getTileSize();
        gc.setStroke(Color.BLACK);
        for (int i = 0; i <= this.width.get(); i++) {
            gc.strokeLine(i * tileSize, 0, i * tileSize, this.height.get() * tileSize);
        }
        for (int i = 0; i <= this.height.get(); i++) {
            gc.strokeLine(0, i * tileSize, this.width.get() * tileSize, i * tileSize);
        }
    }

    private double getTileSize() {
        int w = (int) Math.min(canvas.getWidth() / this.width.get(), this.MAX_SIZE / this.width.get());
        int h = (int) Math.min(canvas.getHeight() / this.height.get(), this.MAX_SIZE / this.height.get());
        return Math.min(w, h);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// imma be honest the GUI i kinda half-assed it with Chat-GPT help (sorry about that)