package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class UploadController implements Initializable {

    private File selectedFile;
    private File selectedDirectory;
    private File defaultDirectory;
    private DirectoryChooser directoryChooser;

    @FXML
    public Button selectImage = new Button();
    public Label completeLabel = new Label();
    public Button selectDirectory = new Button();
    public TextField selectedName = new TextField();
    public TextField selectedPath = new TextField();
    public TextField boxName = new TextField();
    public ComboBox<Category> imageCategory = new ComboBox<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Upload is now loaded!");

        //defaultDirectory = new File(System.getProperty("user.home") + "/Desktop", "CommunicationBoardImages");
        defaultDirectory = new File("MyImages");
        //selectedPath.setText(defaultDirectory.getAbsolutePath());

        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Folder");

        selectImage.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Upload Image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg"));
            selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null)
                selectedName.setText(selectedFile.getName());
        });

        selectDirectory.setOnAction(e -> {
            selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null)
                selectedPath.setText(selectedDirectory.getAbsolutePath());
        });
    }

    public void getCategories(ArrayList<Category> categories) {
        imageCategory.setConverter(new StringConverter<Category>() {

            @Override
            public String toString(Category object) {
                return object.getCategoryName();
            }

            @Override
            public Category fromString(String string) {
                return imageCategory.getItems().stream().filter(category ->
                        category.getCategoryName().equals(string)).findFirst().orElse(null);
            }
        });

        for (Category category : categories) imageCategory.getItems().add(category);
    }

    public void okButtonPushed(ActionEvent event) {

        if (boxName.getText().isEmpty() || imageCategory.getSelectionModel().isEmpty()) {
            completeLabel.setVisible(true);
            completeLabel.setText("Please fill out the settings to continue!");
            completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
            completeLabel.setTextFill(Color.web("red", 1));
        } else {
            try {
                File imgPath = new File("UploadedImages");
                imgPath.mkdir();

                File destinationFile = new File(imgPath, boxName.getText() + ".jpg");

                BufferedImage bImage = SwingFXUtils.fromFXImage(new Image(selectedFile.toURI().toString()), null);
                try {
                    ImageIO.write(bImage, "jpg", destinationFile);
                } catch (IOException e) {
                    completeLabel.setVisible(true);
                    completeLabel.setText("Cannot read and/or write to the selected directory. Please select another folder to continue!");
                    completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
                    completeLabel.setTextFill(Color.web("red", 1));
                    throw new RuntimeException(e);
                }

                FXMLLoader createLoader = new FXMLLoader(getClass().getResource("create.fxml"));
                Parent createParent = createLoader.load();

                CreateController createController = createLoader.getController();
                createController.addNewBox(boxName.getText(), imageCategory.getValue(), imgPath.getAbsolutePath());

                Scene createScene = new Scene(createParent, 800, 600);

                Stage createWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
                createWindow.getIcons().add(new Image(new File("src/res/speech-bubble.png").toURI().toString()));

                createWindow.setScene(createScene);
                createWindow.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void backButtonPushed(ActionEvent event) throws IOException {
        FXMLLoader createLoader = new FXMLLoader(getClass().getResource("create.fxml"));
        Parent createParent = createLoader.load();
        Scene createScene = new Scene(createParent, 800, 600);

        Stage createWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
        createWindow.getIcons().add(new Image(new File("src/res/speech-bubble.png").toURI().toString()));
        createWindow.setTitle("Create a New Board");

        createWindow.setScene(createScene);
        createWindow.show();
    }
}
