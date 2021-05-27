package sample;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public class CreateController implements Initializable {

    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<ImageView> boxes = new ArrayList<>();
    private ArrayList<String> imgPaths;
    private final boolean[] cancelled = {false};
    private boolean inAltPath = false;

    @FXML
    public GridPane image_grid = new GridPane();
    public ComboBox<Lang> lang1, lang2 = new ComboBox<>();
    public Label completeLabel = new Label();
    public Button createBoard = new Button();
    public Button cancelCreate = new Button();
    public Label progressLabel = new Label();
    public Accordion dropdown = new Accordion();
    public ProgressBar progressBar = new ProgressBar();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Create is now loaded!");

        image_grid.getRowConstraints().clear();
        image_grid.setMaxWidth(650);
        progressBar.managedProperty().bind(progressBar.visibleProperty());
        completeLabel.managedProperty().bind(completeLabel.visibleProperty());
        progressLabel.managedProperty().bind(progressLabel.visibleProperty());
        progressLabel.setVisible(false);
        cancelled[0] = false;

        try (BufferedReader csvReader = new BufferedReader(new FileReader("src/res/words.csv"))) {
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                String[] words = Arrays.copyOfRange(data, 1, data.length);
                categories.add(new Category(data[0], new ArrayList<>(Arrays.asList(words))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Category category : categories)
            Platform.runLater(() -> dropdown.getPanes().add(new TitledPane(category.getCategoryName(), category.getBox())));

        getLanguages();
    }

    private void instantiateCheckBox(Category category, ArrayList<String> selectedNames, Lang lang1, Lang lang2) {

        for (int i = 0; i < selectedNames.size(); i++) boxes.add(null);

        for (int i = 0; i < category.getNames().size(); i++) {

            if (!cancelled[0]) {

                final String status = "Translating " + (i + 1) + " of " + category.getNames().size() + " words in " + category.getCategoryName();
                Platform.runLater(() -> progressLabel.setText(status));

                Image image = null;
                String currentImgPath = "";

                File tempImgFile = new File("UploadedImages/" + category.getNames().get(i) + ".jpg");
                if (tempImgFile.exists()) {
                    inAltPath = true;
                    currentImgPath = tempImgFile.toURI().toString();
                }

                boolean inResPath = new File("src/res/" + category.getCategoryName() + "/" + category.getNames().get(i) + ".jpg").exists();

                if (!inAltPath && !inResPath) {

                    category.getNames().remove(i);

                    try (PrintWriter csvWriter = new PrintWriter(new FileWriter("src/res/words.csv"))) {
                        for (Category cat : categories) {
                            csvWriter.print(cat.getCategoryName() + ",");
                            for (String name : cat.getNames()) csvWriter.print(name + ",");
                            csvWriter.println();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    if (inAltPath) {
                        inAltPath = false;
                        image = new Image(currentImgPath, 115, 115, true, false);
                    } else if (inResPath)
                        image = new Image(new File("src/res/" + category.getCategoryName() + "/" + category.getNames().get(i) + ".jpg").toURI().toString(), 115, 115, true, false);

                    final ImageView icon = new ImageView();
                    final CheckBox cb = new CheckBox(category.getNames().get(i));

                    if (category.getIcons().size() < category.getNames().size() && category.getImages().size() < category.getNames().size() && category.getCbs().size() < category.getNames().size()) {
                        category.getIcons().add(i, icon);
                        category.getImages().add(i, image);
                        category.getCbs().add(i, cb);
                    }

                    String primaryCaption = "";
                    String secondaryCaption = "";

                    if (lang1.getID().equals("en")) {
                        primaryCaption = category.getNames().get(i);
                    } else {
                        try {
                            primaryCaption = translate("en", lang1.getID(), category.getNames().get(i));
                            System.out.println(primaryCaption);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (lang2.getID().equals("en")) {
                        secondaryCaption = category.getNames().get(i);
                    } else if (!(lang2.getID().equals("none"))) {
                        try {
                            secondaryCaption = translate("en", lang2.getID(), category.getNames().get(i));
                            System.out.println(secondaryCaption);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    icon.setId(primaryCaption + "." + secondaryCaption + "," + category.getNames().get(i));

                    if (selectedNames.contains(cb.getText())) {
                        cb.setSelected(true);
                        icon.setImage(image);
                        boxes.set(selectedNames.indexOf(cb.getText()), icon);
                    }

                    Image finalImage = image;
                    cb.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {

                        if (new_val) {
                            image_grid.setGridLinesVisible(true);
                            if (boxes.size() < 15) {
                                icon.setImage(finalImage);
                                boxes.add(icon);
                            } else {
                                cb.setSelected(false);
                                completeLabel.setVisible(true);
                                completeLabel.setText("The maximum number of boxes (15) has been reached. Please export this board and start a new board.");
                                completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
                                completeLabel.setTextFill(Color.web("red", 1));
                            }
                        } else {
                            icon.setImage(null);
                            if (boxes.contains(icon)) {
                                boxes.remove(icon);
                            }
                        }

                        image_grid.setGridLinesVisible(false);
                        image_grid.getChildren().clear();
                        if (boxes.size() != 0) image_grid.setGridLinesVisible(true);

                        for (int j = 0; j < boxes.size(); j++) {

                            int name_separator;
                            int lang_separator;

                            if (boxes.get(j) != null) {
                                name_separator = boxes.get(j).getId().indexOf(',');
                                lang_separator = boxes.get(j).getId().indexOf('.');
                            } else {
                                name_separator = lang_separator = 0;
                            }

                            if (boxes.get(j) != null) {
                                Label primaryLabel = new Label(boxes.get(j).getId().substring(0, lang_separator));
                                Label secondaryLabel = new Label(boxes.get(j).getId().substring(lang_separator + 1, name_separator));

                                VBox vbox = new VBox(boxes.get(j), primaryLabel, secondaryLabel);

                                vbox.setStyle("-fx-alignment: center ;");
                                primaryLabel.setStyle("-fx-font-weight: bold ; " +
                                        "-fx-text-alignment: center ;");
                                secondaryLabel.setStyle("-fx-font-weight: bold ; -fx-text-alignment: center ;");
                                secondaryLabel.setTextFill(Color.web("gray", 1));

                                image_grid.add(vbox, j % 3, (int) Math.floor(j / 3));
                            }
                        }
                    });
                }
            }
            else break;
        }

        for (int i = 0; i < boxes.size(); i++) {

            int name_separator;
            int lang_separator;

            if (boxes.get(i) != null) {
                name_separator = boxes.get(i).getId().indexOf(',');
                lang_separator = boxes.get(i).getId().indexOf('.');
            } else {
                name_separator = lang_separator = 0;
            }

            int finalI = i;
            Platform.runLater(
                () -> {
                    if (boxes.get(finalI) != null) {

                        image_grid.setGridLinesVisible(true);

                        Label primaryLabel = new Label(boxes.get(finalI).getId().substring(0, lang_separator));
                        Label secondaryLabel = new Label(boxes.get(finalI).getId().substring(lang_separator + 1, name_separator));

                        VBox vbox = new VBox(boxes.get(finalI), primaryLabel, secondaryLabel);

                        vbox.setStyle("-fx-alignment: center ;");
                        primaryLabel.setStyle("-fx-font-weight: bold ; " +
                                "-fx-text-alignment: center ;");
                        secondaryLabel.setStyle("-fx-font-weight: bold ; -fx-text-alignment: center ;");
                        secondaryLabel.setTextFill(Color.web("gray", 1));

                        image_grid.add(vbox, finalI % 3, (int) Math.floor(finalI / 3));
                    }
                }
            );
        }

        Platform.runLater(() -> category.getBox().getChildren().addAll(category.getCbs()));
    }

    public void generateBoard(ArrayList<String> selectedNames, Lang lang1, Lang lang2) {

        for (Category category : categories) {
            category.getBox().getChildren().clear();
            category.getCbs().clear();
            category.getIcons().clear();
            category.getImages().clear();
        }

        boxes.clear();
        image_grid.getChildren().clear();
        image_grid.setVisible(true);

        this.lang1.setValue(lang1);
        this.lang2.setValue(lang2);

        // Create a Runnable
        Runnable task;

        cancelCreate.setOnAction(event -> {

            cancelled[0] = true;

            for (Category category : categories) {
                category.getBox().getChildren().clear();
                category.getCbs().clear();
                category.getIcons().clear();
                category.getImages().clear();
            }
            boxes.clear();
            completeLabel.setVisible(false);
            image_grid.setGridLinesVisible(false);
            image_grid.getChildren().clear();
            image_grid.setVisible(false);

            progressLabel.setVisible(false);
            progressLabel.setText("");
            cancelCreate.setVisible(false);
            createBoard.setDisable(false);
            progressBar.setVisible(false);

            this.lang1.getSelectionModel().select(new Lang("empty", "Language 1"));
            this.lang2.getSelectionModel().select(new Lang("empty", "Language 2"));
        });

        task = () -> {
            Platform.runLater(() -> {
                cancelCreate.setVisible(true);
                completeLabel.setVisible(false);
                createBoard.setDisable(true);
                image_grid.setVisible(true);
                progressLabel.setVisible(true);
                progressBar.setVisible(true);
                progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            });

            for (int i = 0; i < categories.size(); i++) {
                    int tempI = i;
                    Platform.runLater(() -> {
                        if (tempI != 0) progressBar.setProgress((double) tempI / categories.size());
                    });
                    instantiateCheckBox(categories.get(i), selectedNames, lang1, lang2);
            }

            Platform.runLater(() -> {
                progressLabel.setVisible(false);
                progressLabel.setText("");
                cancelCreate.setVisible(false);
                createBoard.setDisable(false);
                progressBar.setVisible(false);
                image_grid.setVisible(true);
                cancelled[0] = true;
            });
        };

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    public void addNewBox(String imgName, Category imgCategory, String imgPath) {
        try (PrintWriter csvWriter = new PrintWriter(new FileWriter("src/res/words.csv"))) {
            for (Category category : categories)
                if (category.getCategoryName().equals(imgCategory.getCategoryName()))
                    category.getNames().add(imgName);

            for (Category category : categories){
                csvWriter.print(category.getCategoryName() + ",");
                for (String name : category.getNames()) csvWriter.print(name + ",");
                csvWriter.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*imgPaths.add(imgPath);
        try (PrintWriter csvWriter = new PrintWriter(new FileWriter("src/res/paths.csv"))) {
            csvWriter.println(imgPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgPaths.add(imgPath);*/
    }

    private static String translate(String langFrom, String langTo, String text) throws IOException {
        String urlStr = "https://script.google.com/macros/s/AKfycbzhYf6lFqoQlArSCujJBrvzejELr_fZTxx-IJ-ng9hCK6MB3AQf/exec" +
                "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8) +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private void getLanguages() {
        lang1.setConverter(new StringConverter<Lang>() {
            @Override
            public String toString(Lang object) {
                if (object.getName() != null) return object.getName();
                else return null;
            }

            @Override
            public Lang fromString(String string) {
                return lang1.getItems().stream().filter(lang ->
                        lang.getName().equals(string)).findFirst().orElse(null);
            }
        });

        lang2.setConverter(new StringConverter<Lang>() {
            @Override
            public String toString(Lang object) {
                return object.getName();
            }

            @Override
            public Lang fromString(String string) {
                return lang2.getItems().stream().filter(lang ->
                        lang.getName().equals(string)).findFirst().orElse(null);
            }
        });

        lang2.getItems().add(new Lang("none", "None"));
        ArrayList<String> languages = null;

        try {
            languages = new ArrayList<>(Files.readAllLines(Paths.get("src/res/languages.csv")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert languages != null;
        for (String lang : languages) {
            String langID = lang.substring(0, lang.indexOf(","));
            String langName = lang.substring(lang.indexOf(",") + 1, lang.length());
            lang1.getItems().add(new Lang(langID, langName));
            lang2.getItems().add(new Lang(langID, langName));
        }

        lang1.getSelectionModel().select(new Lang("empty", "Language 1"));
        lang2.getSelectionModel().select(new Lang("empty", "Language 2"));
    }

    public void exportButtonPushed() {

        if (lang1.getSelectionModel().isEmpty() || lang2.getSelectionModel().isEmpty()) {
            completeLabel.setVisible(true);
            image_grid.setVisible(false);
            completeLabel.setText("Please complete language settings to continue!");
            completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
            completeLabel.setTextFill(Color.web("red", 1));
        } else if (lang1.getValue().getID().equals(lang2.getValue().getID())) {
            completeLabel.setVisible(true);
            image_grid.setVisible(false);
            completeLabel.setText("Please select different languages!");
            completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
            completeLabel.setTextFill(Color.web("red", 1));
        } else {
            System.out.println("To Printer!");
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                Printer printer = job.getPrinter();
                PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);

                GridPane tempGrid = image_grid;

                double width = tempGrid.getWidth();
                double scaleX = pageLayout.getPrintableWidth() / width / 1.2;
                Scale scale = new Scale(scaleX, scaleX);

                tempGrid.getTransforms().add(scale);
                tempGrid.setTranslateX(tempGrid.getTranslateX() - tempGrid.getBoundsInParent().getMinX());

                job.showPrintDialog(tempGrid.getScene().getWindow());
                job.printPage(tempGrid);
                job.endJob();

                tempGrid.setTranslateX(0);
                tempGrid.getTransforms().remove(scale);
            }
        }
    }

    public void createButtonPushed() {
        image_grid.setGridLinesVisible(false);
        cancelled[0] = false;

        for (Category category : categories) {
            category.getBox().getChildren().clear();
            category.getCbs().clear();
            category.getIcons().clear();
            category.getImages().clear();
        }
        if (lang1.getSelectionModel().isEmpty() || lang2.getSelectionModel().isEmpty() || lang1.getValue().getID().equals("empty") || lang2.getValue().getID().equals("empty")) {
            completeLabel.setVisible(true);
            image_grid.setVisible(false);
            completeLabel.setText("Please complete language settings to continue!");
            completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
            completeLabel.setTextFill(Color.web("red", 1));
        } else if (lang1.getValue().getID().equals(lang2.getValue().getID())) {
            completeLabel.setVisible(true);
            image_grid.setVisible(false);
            completeLabel.setText("Please select different languages!");
            completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
            completeLabel.setTextFill(Color.web("red", 1));
        } else {
            generateBoard(new ArrayList<>(), lang1.getValue(), lang2.getValue());
        }
    }

    public void saveButtonPushed() {
        if (lang1.getValue().getID().equals("empty") || lang2.getValue().getID().equals("empty")) {
            completeLabel.setVisible(true);
            image_grid.setVisible(false);
            completeLabel.setText("Please complete language settings to continue!");
            completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
            completeLabel.setTextFill(Color.web("red", 1));
        } else if (lang1.getValue().getID().equals(lang2.getValue().getID())) {
            completeLabel.setVisible(true);
            image_grid.setVisible(false);
            completeLabel.setText("Please select different languages!");
            completeLabel.setStyle("-fx-font-style: italic ; -fx-font-weight: bold ;");
            completeLabel.setTextFill(Color.web("red", 1));
        } else {
            String fileName = "CommunicationBoard_" + lang1.getValue().getName() + "_" + lang2.getValue().getName() + ".csv";
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Communication Board");
            fileChooser.setInitialFileName(fileName);
            File savedFile = fileChooser.showSaveDialog(null);

            if (savedFile != null) {
                try (PrintWriter csvWriter = new PrintWriter(new FileWriter(savedFile))) {
                    csvWriter.println(lang1.getValue().getID());
                    csvWriter.println(lang1.getValue().getName());
                    csvWriter.println(lang2.getValue().getID());
                    csvWriter.println(lang2.getValue().getName());

                    for (ImageView item : boxes) csvWriter.println(item.getId().substring(item.getId().indexOf(",") + 1, item.getId().length()));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void backButtonPushed(ActionEvent event) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setResizable(true);
        alert.setTitle("Save Before Exiting");
        alert.setHeaderText("If you exit now, you will lose your work.");
        alert.getDialogPane().setContent(new Label("Be sure to export your communication board before exiting."));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            Parent homeParent = loader.load();
            Scene homeScene = new Scene(homeParent, 600, 400);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.getIcons().add(new Image(new File("src/res/speech-bubble.png").toURI().toString()));

            window.setScene(homeScene);
            window.show();
        }
    }

    public void uploadButtonPushed(ActionEvent event) throws IOException {
        FXMLLoader uploadLoader = new FXMLLoader(getClass().getResource("upload.fxml"));
        Parent uploadParent = uploadLoader.load();

        UploadController uploadController = uploadLoader.getController();
        uploadController.getCategories(categories);

        Scene uploadScene = new Scene(uploadParent, 600, 380);

        Stage uploadWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
        uploadWindow.getIcons().add(new Image(new File("src/res/speech-bubble.png").toURI().toString()));

        uploadWindow.setScene(uploadScene);
        uploadWindow.show();
    }
}