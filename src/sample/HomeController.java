package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    private static final String IDLE_BUTTON_STYLE = "-fx-font-weight: bold; -fx-background-color: #42a8d7;";
    private static final String HOVERED_BUTTON_STYLE = "-fx-font-weight: bold; -fx-background-color: lightblue;";

    @FXML
    public Button create = new Button();
    public Button instructions = new Button();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Home is now loaded!");

        create.setStyle(IDLE_BUTTON_STYLE);
        create.setOnMouseEntered(e -> create.setStyle(HOVERED_BUTTON_STYLE));
        create.setOnMouseExited(e -> create.setStyle(IDLE_BUTTON_STYLE));

        instructions.setStyle(IDLE_BUTTON_STYLE);
        instructions.setOnMouseEntered(e -> instructions.setStyle(HOVERED_BUTTON_STYLE));
        instructions.setOnMouseExited(e -> instructions.setStyle(IDLE_BUTTON_STYLE));
    }

    public void createScreenButtonPushed(ActionEvent event) throws IOException {
        FXMLLoader createLoader = new FXMLLoader(getClass().getResource("create.fxml"));
        Parent createParent = createLoader.load();
        Scene createScene = new Scene(createParent, 800, 600);

        Stage createWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
        createWindow.getIcons().add(new Image(new File("src/res/speech-bubble.png").toURI().toString()));
        createWindow.setTitle("Create a New Board");

        createWindow.setScene(createScene);
        createWindow.show();
    }

    public void instructionsButtonPushed(ActionEvent event) throws IOException {
        FXMLLoader instructionsLoader = new FXMLLoader(getClass().getResource("instructions.fxml"));
        Parent instructionsParent = instructionsLoader.load();
        Scene instructionsScene = new Scene(instructionsParent, 600, 400);

        Stage instructionsWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
        instructionsWindow.getIcons().add(new Image(new File("src/res/speech-bubble.png").toURI().toString()));
        instructionsWindow.setTitle("Instructions");

        instructionsWindow.setScene(instructionsScene);
        instructionsWindow.show();
    }

    public void importButtonPushed(ActionEvent event) throws IOException {

        FXMLLoader importLoader = new FXMLLoader(getClass().getResource("import.fxml"));
        Parent importParent = importLoader.load();
        Scene importScene = new Scene(importParent, 600, 300);

        Stage importWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
        importWindow.getIcons().add(new Image(new File("src/res/speech-bubble.png").toURI().toString()));

        importWindow.setScene(importScene);
        importWindow.show();
    }
}
