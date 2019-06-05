package com.example.ftp.GUI;


import com.example.ftp.model.DirectoryModel;
import com.example.ftp.model.implementations.ConnectionException;
import com.example.ftp.model.implementations.DirectoryModelImpl;
import com.example.ftp.server.Server;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApplication extends Application {
    private static final int SCENE_HEIGHT = 600;
    private static final int SCENE_WIDTH = 800;

    private DirectoryModel model;
    private Server server;

    public static void main(String[] args){
        launch(args);
    }
    private TableView<FileDescription> table = new TableView<>();

    private final ObservableList<FileDescription> files =
            FXCollections.observableArrayList(new FileDescription("Jacob1", 0), new FileDescription("Jacob2", 1), new FileDescription("Jacob3", 2));

    private VBox createServerMenu() {
        VBox menu = new VBox();
        Label label = new Label();
        label.setText("Server PORT:");
//        TextArea port = new TextArea();
//        port.setPrefColumnCount(10);
//        port.setPrefRowCount(3);
        label.setText("Server IP:");
        TextArea IP = new TextArea();
        IP.setPrefColumnCount(10);
        IP.setPrefRowCount(3);
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            System.out.println("creating server, IP = " + IP.getText());
            server = new Server(IP.getText(), 2599); // TODO make server always use this port and make it some constant
            new Thread(() -> {
                try {
                    server.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });
//        menu.getChildren().addAll(label, port, IP, saveButton);
        menu.getChildren().addAll(label, IP, saveButton);
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
        saveButton.setOnAction(event -> {
            try {
                if (model == null) { // TODO unregister old one if present
                    System.out.println("creating client, IP = " + IP.getText());
                    model = new DirectoryModelImpl(IP.getText());
                    updateFiles();
                }
            } catch (ConnectionException e) {
                e.printStackTrace();
                // TODO notify user somehow
            }
        });
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
        sendButton.setOnAction(event -> {
            if (model != null) {
                model.openDirectoryByAbsolutePath(path.getText());
                updateFiles();
            }
        });
        menu.getChildren().addAll(label, path, sendButton);
        return menu;
    }

    public class FileDescription {
        public final SimpleStringProperty fileName;
        private final int number;

        public FileDescription(String fileName, int number) {
            this.fileName = new SimpleStringProperty(fileName);
            this.number = number;
        }

        public String getFileName() {
            return fileName.get();
        }

        public void setFileName(String fName) { // TODO delete or at least rename argument
            fileName.set(fName);
        }

        public int getNumber() {
            return number;
        }
    }

    private VBox createFilesBox() {
        VBox filesBox = new VBox();

        TableColumn<FileDescription, String> firstNameCol = new TableColumn<>("fileName");
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<>("fileName")
        );
        table.setEditable(true);
        firstNameCol.setMinWidth(700);
        table.setEditable(true);
        table.setOnMouseClicked(event1 ->  {
            if (model != null) {
                model.openDirectory(table.getSelectionModel().getSelectedItem().getNumber());
                updateFiles();
            }
        });
        table.setItems(files);
        table.getColumns().add(firstNameCol);
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

        ((Group) scene.getRoot()).getChildren().addAll(files);

        menuPanel.getItems().addAll(serverMenu, clientMenu, pathMenu);
        menuPanel.setDividerPositions(0.3, 0.3);
        mainPanel.getItems().addAll(files, menuPanel);
        mainPanel.setDividerPositions(0.8);

        root.getChildren().add(mainPanel);
    }

    private void updateFiles() {
        if (model == null) {
            return;
        }
        files.clear();
        for (int i = 0; i < model.getDirectories().size(); i++) {
            files.add(new FileDescription(model.getDirectories().get(i).getName(), i));
        }
        for (int i = 0; i < model.getFiles().size(); i++) {
            files.add(new FileDescription(model.getFiles().get(i).getName(), i));
        }
    }
}
