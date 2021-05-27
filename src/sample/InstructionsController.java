package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InstructionsController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Instructions is now loaded!");
    }

    public void backButtonPushed(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
        Parent homeParent = loader.load();
        Scene homeScene = new Scene(homeParent, 600, 400);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.getIcons().add(new Image(new File("src/res/speech-bubble.png").toURI().toString()));

        window.setScene(homeScene);
        window.show();
    }
}
