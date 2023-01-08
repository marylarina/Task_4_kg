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

    final private float TRANSLATION = 0.5F;
    @FXML
    public Button delete;



    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;
    private Model activeModel = null;

    private Color color = Color.WHITE;

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
    private TableColumn<CameraTable, String> cameraName;

    private ArrayList<Model> activeModels = new ArrayList<>();

    @FXML
    public CheckBox drawMeshCheckBox;

    @FXML
    public CheckBox useLightingCheckBox;

    @FXML
    public CheckBox texturePolygonsCheckBox;

    private boolean drawMesh = false;
    private boolean useLighting = false;
    private boolean texturePolygons = false;

    private Camera camera = new Camera(
            new com.cgvsu.math.Vector3f(0, 00, 100),
            new com.cgvsu.math.Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML
    private void initialize() {

        camerasTable.add(new CameraTable(camera, "camera1"));
        cameras.getItems().setAll(camerasTable);
        cameraName.setCellValueFactory(new PropertyValueFactory<>("name"));

        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

            camera.setAspectRatio((float) (width / height));

            for(int i=0; i<activeModels.size(); i++) {
                if (activeModels.get(i) != null) {
                    RenderEngine.render(canvas.getGraphicsContext2D(),
                            camera, activeModels.get(i), (int) width, (int) height,
                            canvas, color, drawMesh, useLighting, texturePolygons);
                }
            }

        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private Color changeColor() {
        Color newColor = colorPicker.getValue();
        color = newColor;
        return color;
    }

    @FXML
    private void setUseLighting() {
        useLighting = !useLighting;
    }


    @FXML
    private void whiteTheme(){
        anchorPane.setStyle("-fx-background-color: #ffffff");
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
    }

    @FXML
    private void blackTheme(){
        anchorPane.setStyle("-fx-background-color: #000000");
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.WHITE);
    }

    @FXML
    private void drawMesh(){
        drawMesh = !(drawMesh);
        //RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int)canvas.getWidth(), (int)canvas.getHeight(), canvas, drawMesh1, fillPolygons1, texturePolygons1);
    }

    /*@FXML
    private void fillPolygons(){
        //fillPolygons1 = !(fillPolygons1);
        if (mesh!=null) {
            fillPolygons1 = !(fillPolygons1);
            //RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) canvas.getWidth(), (int) canvas.getHeight(), canvas, drawMesh1, fillPolygons1, texturePolygons1);
        }else{

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("В приложении нет загруженных моделей!");
            alert.showAndWait();
        }
    }

     */

    @FXML
    private void texturePolygons(){
        texturePolygons = !(texturePolygons);
        //RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int)canvas.getWidth(), (int)canvas.getHeight(), canvas, drawMesh1, fillPolygons1, texturePolygons1);
    }

    /*@FXML
    private Model handle(){
        try {
            ModelTable v = table.getSelectionModel().getSelectedItem();
            mesh = v.model;
        }
        catch (NullPointerException exception){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("В этой строке нет загруженных моделей!");
            alert.showAndWait();
        }
        return mesh;
    }
     */

    @FXML
    private ArrayList<Model> handle(){
        int count=0;
        try {
            ModelTable v = table.getSelectionModel().getSelectedItem();
            mesh = v.model;
            for(int i=0; i<activeModels.size(); i++){
                if(mesh != activeModels.get(i)){
                    count++;
                }
            }
            if(count==activeModels.size()) {
                activeModels.add(mesh);
            }else{
                activeModels.remove(mesh);
            }
        }
        catch (NullPointerException exception){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("В этой строке нет загруженных моделей!");
            alert.showAndWait();
        }
        return activeModels;
    }

    @FXML
    private void setDeleteModel(){
        ModelTable deleteModel = table.getSelectionModel().getSelectedItem();
        models.remove(deleteModel);
        table.getItems().remove(deleteModel);
        activeModels.remove(deleteModel.model);
    }

   /* @FXML
    private Model handle() throws IOException {
        try {
            ModelTable v = table.getSelectionModel().getSelectedItem();
            mesh = v.model;
        }catch (NullPointerException exception){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Нажмите на строку таблицы!");
            alert.showAndWait();
        }
        return mesh;
    }
    */



    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);

            // todo: обработка ошибок
        } catch (IOException exception) {

        }
        models.add(new ModelTable(mesh, file.getName()));
        table.getItems().setAll(models);
        model.setCellValueFactory(new PropertyValueFactory<>("string"));
    }

    @FXML
    private void onSaveModelMenuItemClick() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(table.getSelectionModel().getSelectedItem().string);
        byte [] str = ObjWriter.write(table.getSelectionModel().getSelectedItem().model).getBytes(StandardCharsets.UTF_8);
        fileOutputStream.write(str);
    }

    @FXML
    private void onSaveAsModelMenuItemClick() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());
        FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(fileName));
        byte [] str = ObjWriter.write(mesh).getBytes(StandardCharsets.UTF_8);
        fileOutputStream.write(str);
    }

    @FXML
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
    }
}