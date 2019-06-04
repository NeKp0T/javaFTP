package com.example.ftp.GUI;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    private static final int SCENE_HEIGHT = 600;
    private static final int SCENE_WIDTH = 800;

    public static void main(String[] args){
        launch(args);
    }
    private TableView<FileDescription> table = new TableView<>();

    private final ObservableList<FileDescription> files =
            FXCollections.observableArrayList(new FileDescription("Jacob1"), new FileDescription("Jacob2"), new FileDescription("Jacob3"));

    private VBox createServerMenu() {
        VBox menu = new VBox();
        Label label = new Label();
        label.setText("Server PORT:");
        TextArea port = new TextArea();
        port.setPrefColumnCount(10);
        port.setPrefRowCount(3);
        label.setText("Server IP:");
        TextArea IP = new TextArea();
        IP.setPrefColumnCount(10);
        IP.setPrefRowCount(3);
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> { });
        menu.getChildren().addAll(label, port, IP, saveButton);
        return menu;
    }

    private VBox createClientMenu() {
        VBox menu = new VBox();
        Label label = new Label();
        label.setText("Client IP:");
        TextArea IP = new TextArea();
        IP.setPrefColumnCount(10);
        IP.setPrefRowCount(3);
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> { });
        menu.getChildren().addAll(label, IP, saveButton);
        return menu;
    }

    private VBox createPathMenu() {
        VBox menu = new VBox();
        Label label = new Label();
        label.setText("Path:");
        TextArea path = new TextArea();
        path.setPrefColumnCount(10);
        path.setPrefRowCount(3);
        Button sendButton = new Button("Send");
        sendButton.setOnAction(event -> { });
        menu.getChildren().addAll(label, path, sendButton);
        return menu;
    }

    private class FileDescription {
        public String fileName;

        public FileDescription(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    private VBox createFilesBox() {
        VBox filesBox = new VBox();

        TableColumn<FileDescription, String> firstNameCol = new TableColumn<>("Files");
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<>("fileName")
        );
        firstNameCol.setMinWidth(700);
        table.setOnMouseClicked(event1 -> System.out.println(table.getSelectionModel().getSelectedItem().getFileName()));
        table.setItems(files);
        table.getColumns().addAll(firstNameCol);
        filesBox.getChildren().add(table);
        return filesBox;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("FTP");

        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setMinWidth(SCENE_WIDTH + 20);
        stage.setMinHeight(SCENE_HEIGHT + 20);
        stage.setMaxWidth(1.2 * SCENE_WIDTH + 20);
        stage.setMaxHeight(1.2 * SCENE_HEIGHT + 20);
        stage.setScene(scene);
        stage.show();

        SplitPane mainPanel = new SplitPane();
        mainPanel.setMinSize(SCENE_WIDTH, SCENE_HEIGHT);
        mainPanel.setOrientation(Orientation.HORIZONTAL);

        SplitPane menuPanel = new SplitPane();
        menuPanel.setOrientation(Orientation.VERTICAL);

        VBox serverMenu = createServerMenu();
        VBox clientMenu = createClientMenu();
        VBox pathMenu = createPathMenu();
        VBox files = createFilesBox();

        menuPanel.getItems().addAll(serverMenu, clientMenu, pathMenu);
        menuPanel.setDividerPositions(0.3, 0.3);
        mainPanel.getItems().addAll(files, menuPanel);
        mainPanel.setDividerPositions(0.8);

        root.getChildren().add(mainPanel);
    }
}
