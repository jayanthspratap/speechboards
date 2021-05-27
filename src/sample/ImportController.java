package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ImportController implements Initializable {

    ArrayList<String> names = new ArrayList<>();

    @FXML
    public Button selectFile = new Button("Select File");
    public TextField selectedName = new TextField();
    public File selectedFile;
    public Label completeLabel = new Label();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Import is now loaded!");

        selectFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Communication Board");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null)
                selectedName.setText(selectedFile.getName());
        });
    }

    public void okButtonPushed(ActionEvent event) {

        if (selectedFile == null) {
            completeLabel.setVisible(true);
            completeLabel.setText("Please select a file to continue!");
            completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
            completeLabel.setTextFill(Color.web("red", 1));
        } else {
            try {
                ArrayList<String> lines = new ArrayList<>(Files.readAllLines(Paths.get(selectedFile.getPath())));

                Lang lang1 = new Lang(lines.get(0), lines.get(1));
                Lang lang2 = new Lang(lines.get(2), lines.get(3));

                for (int i = 4; i < lines.size(); i++) names.add(lines.get(i));

                FXMLLoader createLoader = new FXMLLoader(getClass().getResource("create.fxml"));
                Parent createParent = createLoader.load();

                CreateController createController = createLoader.getController();
                createController.generateBoard(names, lang1, lang2);

                Scene createScene = new Scene(createParent, 800, 600);

                Stage createWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
                createWindow.getIcons().add(new Image("/res/speech-bubble.png"));

                createWindow.setScene(createScene);
                createWindow.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void backButtonPushed(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
        Parent homeParent = loader.load();
        Scene homeScene = new Scene(homeParent, 600, 400);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.getIcons().add(new Image("/res/speech-bubble.png"));

        window.setScene(homeScene);
        window.show();
    }
}
