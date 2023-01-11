package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.RenderEngine;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 1.5F;
    @FXML
    public Button delete;

    @FXML
    public Button addCamera;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;


    private Color staticColor = Color.BLUE;

    @FXML
    private ColorPicker colorPicker = new ColorPicker();

    private ObservableList<ModelTable> models = FXCollections.observableArrayList();
    private ObservableList<CameraTable> camerasTable = FXCollections.observableArrayList();

    @FXML
    private TableView<ModelTable> table;

    @FXML
    private TableColumn<ModelTable, File> model;

    @FXML
    private TableView<CameraTable> cameras;

    @FXML
    private TableColumn<CameraTable, Integer> cameraName;

    private ArrayList<Model> activeModels = new ArrayList<>();

    @FXML
    public CheckBox drawMeshCheckBox;

    @FXML
    public CheckBox useLightingCheckBox;

    @FXML
    public CheckBox texturePolygonsCheckBox;

    @FXML
    public VBox drawingMode;

    private boolean drawMesh = false;
    private boolean useLighting = false;
    private boolean texturePolygons = false;

    private Camera activeCamera = new Camera(
            new com.cgvsu.math.Vector3f(0, 0, 100),
            new com.cgvsu.math.Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    private float[][] z_buffer;

    private int count = 1;

    private Color activeColor;
    private Color currentColor;
    private Color targetColor;

    private final Color[] colors1 = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.PURPLE, Color.PINK};
    private final Color[] colors2 = {Color.CYAN, Color.MAGENTA, Color.YELLOW};
    private final Color[] colors3 = {Color.BLACK, Color.RED, Color.WHITE};
    private Color[] colors;

    private int colorIndex = 1;
    private int stepCount = 1;
    private final int numberOfSteps = 50;

    @FXML
    private void initialize() {

        camerasTable.add(new CameraTable(activeCamera, 1));
        cameras.getItems().setAll(camerasTable);
        cameraName.setCellValueFactory(new PropertyValueFactory<>("name"));

        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        whiteTheme();
        drawingMode.setSpacing(4);

        colors = colors1;
        activeColor = colors[0];
        currentColor = activeColor;
        targetColor = colors[1];

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            z_buffer = new float[(int) height + 1][(int) width + 1];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    z_buffer[i][j] = 1000000F;
                }
            }

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

            activeCamera.setAspectRatio((float) (width / height));

            if(texturePolygons) {

                float redStep = getRedStep(activeColor, targetColor) / numberOfSteps;
                float greenStep = getGreenStep(activeColor, targetColor) / numberOfSteps;
                float blueStep = getBlueStep(activeColor, targetColor) / numberOfSteps;

                if (stepCount < numberOfSteps) {
                    currentColor = getColor(redStep, greenStep, blueStep, currentColor);
                    stepCount++;
                } else {
                    stepCount = 1;
                    currentColor = targetColor;
                    activeColor = targetColor;
                    colorIndex++;
                    if (colorIndex == colors.length) {
                        colorIndex = 0;
                    }
                    targetColor = colors[colorIndex];
                }

                for (int i = 0; i < activeModels.size(); i++) {
                    if (activeModels.get(i) != null) {
                        RenderEngine.render(canvas.getGraphicsContext2D(),
                                activeCamera, activeModels.get(i), (int) width, (int) height,
                                canvas, currentColor, drawMesh, useLighting, texturePolygons, z_buffer);
                    }
                }

            } else {

                for (int i = 0; i < activeModels.size(); i++) {
                    if (activeModels.get(i) != null) {
                        RenderEngine.render(canvas.getGraphicsContext2D(),
                                activeCamera, activeModels.get(i), (int) width, (int) height,
                                canvas, staticColor, drawMesh, useLighting, texturePolygons, z_buffer);
                    }
                }

            }

        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    private float getRedStep(Color startColor, Color endColor) {
        return (float) endColor.getRed() - (float) startColor.getRed();
    }

    private float getGreenStep(Color startColor, Color endColor) {
        return (float) endColor.getGreen() - (float) startColor.getGreen();
    }

    private float getBlueStep(Color startColor, Color endColor) {
        return (float) endColor.getBlue() - (float) startColor.getBlue();
    }

    private Color getColor(float redStep, float greenStep, float blueStep, Color currentColor) {
        double r = currentColor.getRed() + redStep;
        double g = currentColor.getGreen() + greenStep;
        double b = currentColor.getBlue() + blueStep;
        return new Color(r, g, b, 1);
    }

    @FXML
    private Color changeColor() {
        staticColor = colorPicker.getValue();
        return staticColor;
    }

    @FXML
    private void whiteTheme() {
        anchorPane.setStyle("-fx-background-color: #ffffff");
        delete.setStyle("-fx-border-color: #1d1d1d");
        addCamera.setStyle("-fx-border-color: #1d1d1d");
        drawingMode.setStyle("-fx-border-style: solid;"
                + "-fx-border-width: 2;"
                + "-fx-border-color: #1d1d1d");
        drawMeshCheckBox.setStyle("-fx-text-fill: black");
        useLightingCheckBox.setStyle("-fx-text-fill: black");
        texturePolygonsCheckBox.setStyle("-fx-text-fill: black");
        table.setStyle("-fx-border-style: solid;"
                + "-fx-border-width: 2;"
                + "-fx-border-color: #1d1d1d");
        cameras.setStyle("-fx-border-style: solid;"
                + "-fx-border-width: 2;"
                + "-fx-border-color: #1d1d1d");

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
    }

    @FXML
    private void blackTheme() {
        anchorPane.setStyle("-fx-background-color: #181818");
        delete.setStyle("-fx-border-color: #A9A9A9");
        addCamera.setStyle("-fx-border-color: #A9A9A9");
        drawingMode.setStyle("-fx-border-style: solid;"
                + "-fx-border-width: 2;"
                + "-fx-border-color: #A9A9A9");
        drawMeshCheckBox.setStyle("-fx-text-fill: white");
        useLightingCheckBox.setStyle("-fx-text-fill: white");
        texturePolygonsCheckBox.setStyle("-fx-text-fill: white");
        table.setStyle("-fx-border-style: solid;"
                + "-fx-border-width: 2;"
                + "-fx-border-color: #A9A9A9");
        cameras.setStyle("-fx-border-style: solid;"
                + "-fx-border-width: 2;"
                + "-fx-border-color: #A9A9A9");

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.WHITE);
    }

    @FXML
    private void drawMesh() {
        drawMesh = !drawMesh;
        if (activeModels.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("В приложении нет загруженных моделей или не выбрана модель из списка!");
            alert.showAndWait();
        }
    }

    @FXML
    private void texturePolygons() {
        texturePolygons = !texturePolygons;
        if (activeModels.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("В приложении нет загруженных моделей или не выбрана модель из списка!");
            alert.showAndWait();
        }
    }

    @FXML
    private void setUseLighting() {
        useLighting = !useLighting;
        if (activeModels.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("В приложении нет загруженных моделей или не выбрана модель из списка!");
            alert.showAndWait();
        }
    }

    @FXML
    private void addCamera() {
        Camera newCamera = new Camera(
                new com.cgvsu.math.Vector3f(0, 0, 100),
                new com.cgvsu.math.Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 100);
        count++;
        camerasTable.add(new CameraTable(newCamera, count));
        cameras.getItems().setAll(camerasTable);
        cameraName.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    @FXML
    private Camera setActiveCamera() {
        CameraTable v = cameras.getSelectionModel().getSelectedItem();
        activeCamera = v.camera;
        return activeCamera;
    }

    @FXML
    private ArrayList<Model> handle() {
        int count = 0;
        try {
            ModelTable v = table.getSelectionModel().getSelectedItem();
            mesh = v.model;
            for (int i = 0; i < activeModels.size(); i++) {
                if (mesh != activeModels.get(i)) {
                    count++;
                }
            }
            if (count == activeModels.size()) {
                activeModels.add(mesh);
            } else {
                activeModels.remove(mesh);
            }
        } catch (NullPointerException exception) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("В этой строке нет загруженных моделей!");
            alert.showAndWait();
        }
        return activeModels;
    }

    @FXML
    private void setDeleteModel() {
        ModelTable deleteModel = table.getSelectionModel().getSelectedItem();
        models.remove(deleteModel);
        table.getItems().remove(deleteModel);
        activeModels.remove(deleteModel.model);
    }


    @FXML
    private void onOpenModelMenuItemClick() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        String fileContent = Files.readString(fileName);
        mesh = ObjReader.read(fileContent);
        if (mesh.vertices.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Пустой файл!");
            alert.showAndWait();
        } else {
            models.add(new ModelTable(mesh, file.getName()));
            table.getItems().setAll(models);
            model.setCellValueFactory(new PropertyValueFactory<>("string"));
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() throws IOException {
        if(table.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Не выбрана модель для сохранения!");
            alert.showAndWait();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(table.getSelectionModel().getSelectedItem().string);
        byte[] str = ObjWriter.write(table.getSelectionModel().getSelectedItem().model).getBytes(StandardCharsets.UTF_8);
        fileOutputStream.write(str);
    }

    @FXML
    private void onSaveAsModelMenuItemClick() throws IOException {
        if(table.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Не выбрана модель для сохранения!");
            alert.showAndWait();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
            fileChooser.setTitle("Save Model");

            File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
            if (file == null) {
                return;
            }

            Path fileName = Path.of(file.getAbsolutePath());
            FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(fileName));
            byte[] str = ObjWriter.write(table.getSelectionModel().getSelectedItem().model).getBytes(StandardCharsets.UTF_8);
            fileOutputStream.write(str);
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        activeCamera.movePosition(new com.cgvsu.math.Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        activeCamera.movePosition(new com.cgvsu.math.Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        activeCamera.movePosition(new com.cgvsu.math.Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        activeCamera.movePosition(new com.cgvsu.math.Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        activeCamera.movePosition(new com.cgvsu.math.Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        activeCamera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    /*@FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new com.cgvsu.math.Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new com.cgvsu.math.Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new com.cgvsu.math.Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new com.cgvsu.math.Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new com.cgvsu.math.Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }*/
}