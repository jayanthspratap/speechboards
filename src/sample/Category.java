package sample;

import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class Category {
    private String categoryName;
    private ArrayList<String> names;
    private ArrayList<Image> images;
    private ArrayList<ImageView> icons;
    private ArrayList<CheckBox> cbs;
    private VBox box;

    public Category(String categoryName, ArrayList<String> names) {
        this.categoryName = categoryName;
        this.names = names;
        this.images = new ArrayList<>(names.size());
        this.icons = new ArrayList<>(names.size());
        this.cbs = new ArrayList<>(names.size());
        this.box = new VBox();
    }

    public String getCategoryName() {
        return categoryName;
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public VBox getBox() {
        return box;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public ArrayList<ImageView> getIcons() {
        return icons;
    }

    public ArrayList<CheckBox> getCbs() {
        return cbs;
    }
}
