package com.FSA;

import com.FSA.DAO.ChantierDAO;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class AddProjectController {

    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private TextField budgetField;
    @FXML private DatePicker startPicker;
    @FXML private DatePicker endPicker;
    @FXML private ComboBox<ChantierDAO.ChefOption> chefBox;
    @FXML private Button chooseImageButton;
    @FXML private Label imageName;
    @FXML private ImageView preview;
    @FXML private Label errorLabel;
    @FXML private Button cancelButton;
    @FXML private Button createButton;

    private final ChantierDAO dao = new ChantierDAO();
    private File chosenImage;
    private Runnable onCancel = () -> {};
    private Runnable onCreated = () -> {};

    /** The page tells the form what to do when it closes (cancel) or when the project is saved. */
    public void setCallbacks(Runnable onCancel, Runnable onCreated) {
        this.onCancel = onCancel;
        this.onCreated = onCreated;
    }

    @FXML
    private void initialize() {
        chooseImageButton.setOnAction(e -> chooseImage());
        cancelButton.setOnAction(e -> onCancel.run());
        createButton.setOnAction(e -> create());
        loadChefs();
    }

    // ---------------- chefs ----------------

    private void loadChefs() {
        Task<List<ChantierDAO.ChefOption>> task = new Task<>() {
            @Override protected List<ChantierDAO.ChefOption> call() throws Exception {
                return dao.chefsDisponibles();
            }
        };
        task.setOnSucceeded(e -> {
            chefBox.getItems().setAll(task.getValue());
            if (task.getValue().isEmpty()) chefBox.setPromptText("No chef available");
        });
        task.setOnFailed(e -> showError(explain(task.getException())));
        run(task);
    }

    // ---------------- image ----------------

    private void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a project image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        File file = chooser.showOpenDialog(chooseImageButton.getScene().getWindow());
        if (file == null) return;                       // the user closed the dialog

        if (file.length() > 5_000_000) {
            showError("The image is too big (5 MB maximum).");
            return;
        }
        chosenImage = file;
        imageName.setText(file.getName());
        // small preview: JavaFX decodes it at 200px wide, not at full size
        preview.setImage(new Image(file.toURI().toString(), 200, 0, true, true));
        hideError();
    }

    // ---------------- save ----------------

    private void create() {
        hideError();

        String nom = nameField.getText().trim();
        String adresse = locationField.getText().trim();
        LocalDate debut = startPicker.getValue();
        LocalDate fin = endPicker.getValue();
        ChantierDAO.ChefOption chef = chefBox.getValue();
        BigDecimal budget = parseBudget(budgetField.getText());

        if (nom.isEmpty())                 { showError("Give the project a name."); return; }
        if (nom.length() > 100)            { showError("The name is too long (100 characters maximum)."); return; }
        if (adresse.isEmpty())             { showError("Give the location of the site."); return; }
        if (adresse.length() > 255)        { showError("The location is too long (255 characters maximum)."); return; }
        if (budget == null || budget.signum() <= 0)
        { showError("The budget must be a number above 0, e.g. 500000."); return; }
        if (budget.compareTo(new BigDecimal("999999999999")) > 0)
        { showError("The budget is too big."); return; }
        if (debut == null || fin == null)  { showError("Choose the start and finish dates."); return; }
        if (fin.isBefore(debut))           { showError("The finish date must be after the start date."); return; }
        if (chef == null)                  { showError("Choose the chef who will run the project."); return; }

        File imageFile = chosenImage;
        createButton.setDisable(true);

        // copying the image and the INSERT can take time: do them in the background
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                String saved = imageFile == null ? null : ImageStore.save(imageFile);
                dao.ajouter(nom, adresse, debut, fin, ChantierDAO.statutPour(debut, fin),
                        chef.id(), budget, saved);
                return null;
            }
        };
        task.setOnSucceeded(e -> onCreated.run());
        task.setOnFailed(e -> {
            createButton.setDisable(false);
            showError(explain(task.getException()));
        });
        run(task);
    }

    private BigDecimal parseBudget(String text) {
        try {
            return new BigDecimal(text.trim().replace(" ", "").replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ---------------- helpers ----------------

    private String explain(Throwable t) {
        if (t instanceof SQLException sql) {
            if ("23505".equals(sql.getSQLState())) return "This chef already runs another project.";
            if ("42703".equals(sql.getSQLState()))
                return "The database has no budget/image column yet: run the ALTER TABLE first.";
            return "Database error: " + sql.getMessage();
        }
        return "Error: " + t.getMessage();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void run(Task<?> task) {
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
}