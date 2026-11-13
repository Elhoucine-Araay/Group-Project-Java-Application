package com.FSA;

import java.util.Locale;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import com.FSA.DAO.ChantierDAO;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;

public class DashboardController {

    @FXML private StackPane root;
    @FXML private StackPane sidebar;
    @FXML private VBox sidebarContent;

    // sidebar
    @FXML private Button settingsButton;
    @FXML private Button dashboardButton;
    @FXML private Button projectsButton;
    @FXML private Button notificationsButton;
    @FXML private Button reportsButton;
    @FXML private Button teamButton;
    @FXML private Button logoutButton;
    @FXML private HBox themeRow;
    @FXML private StackPane themeSwitch;

    // page
    @FXML private Button addProjectButton;
    @FXML private VBox scheduledCards;
    @FXML private VBox activeCards;
    @FXML private VBox completedCards;
    @FXML private Label scheduledCount;
    @FXML private Label activeCount;
    @FXML private Label completedCount;
    // ---- pop-up ----
    @FXML private StackPane popupLayer;
    @FXML private VBox popupCard;
    @FXML private Label popupTitle;
    @FXML private VBox popupContent;
    @FXML private Button popupClose;

    @FXML
    private void initialize() {
        // one line replaces all the old sidebar code
        new SidebarBehavior(root, sidebar, sidebarContent,
                themeRow, themeSwitch).install();

        // ---- sidebar actions ----
        settingsButton.setOnAction(e -> {
            Label info = new Label("Here you can show the Settings.");
            showPopup("Settings", info);
        });
        dashboardButton.setOnAction(e -> { /* TODO: already here */ });
        projectsButton.setOnAction(e -> { /* TODO: go to projects */ });
        notificationsButton.setOnAction(e -> { /* TODO: go to notifications */ });
        reportsButton.setOnAction(e -> { /* TODO: go to reports */ });
        teamButton.setOnAction(e -> { /* TODO: go to team */ });
        logoutButton.setOnAction(e -> Navigation.go(root, "login.fxml"));
        addProjectButton.setOnAction(e -> openAddProjectPopup());
        loadProjects();

        popupClose.setOnAction(e -> hidePopup());            // the X button closes it
        popupLayer.setOnMouseClicked(e -> hidePopup());      // a click on the dark background closes it
        popupCard.setOnMouseClicked(e -> e.consume());       // but a click inside the card must NOT close it
    }

    // ---------------- project cards ----------------

    /**
     * Builds a project card with the same look as the design.
     *
     * @param imageClass CSS class that holds the photo, e.g. "img-warehouse"
     * @param tag        small label on the photo (e.g. "Next"), or null
     * @param members    number of members, or null to hide the badge
     */
    public VBox buildCard(String imageUrl, String tag, String title, String location,
                          String dates, String owner, Integer members) {

        StackPane img = new StackPane();
        img.getStyleClass().add("card-img");                 // size and "cover" still come from the CSS
        if (imageUrl != null) {
            img.setStyle("-fx-background-image: url(\"" + imageUrl + "\");");
        }
        img.setAlignment(Pos.TOP_LEFT);
        if (tag != null) {
            Label t = new Label(tag);
            t.getStyleClass().add("tag");
            img.getChildren().add(t);
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(new Label(owner));
        footer.getStyleClass().add("card-footer");
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().get(0).getStyleClass().add("owner");
        if (members != null) {
            Region gap = new Region();
            HBox.setHgrow(gap, Priority.ALWAYS);
            Label badge = new Label(members + " members");
            badge.getStyleClass().add("members");
            footer.getChildren().addAll(gap, badge);
        }

        VBox body = new VBox(titleLabel,
                metaRow("icon-pin", location),
                metaRow("icon-cal", dates),
                spacer, footer);
        body.getStyleClass().add("card-body");

        VBox card = new VBox(img, body);
        card.getStyleClass().add("card");
        roundCorners(card, 0.2);
        return card;
    }

    private final ChantierDAO chantierDAO = new ChantierDAO();

    private void openAddProjectPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/FSA/view/add-project.fxml"));
            Node form = loader.load();
            AddProjectController controller = loader.getController();

            controller.setCallbacks(
                    this::hidePopup,                          // Cancel
                    () -> { hidePopup(); loadProjects(); });  // saved: close, then reload the cards

            showPopup("New project", true, form);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot open add-project.fxml", ex);
        }
    }

    /** Reads the projects from the database and fills the 3 columns. */
    private void loadProjects() {
        Task<List<ChantierDAO.Card>> task = new Task<>() {
            @Override protected List<ChantierDAO.Card> call() throws Exception {
                return chantierDAO.toutes();
            }
        };

        task.setOnSucceeded(e -> {
            scheduledCards.getChildren().clear();
            activeCards.getChildren().clear();
            completedCards.getChildren().clear();
            scheduledCount.setText("0");
            activeCount.setText("0");
            completedCount.setText("0");

            for (ChantierDAO.Card c : task.getValue()) {
                boolean waiting = c.chef() == null;
                VBox card = buildCard(ImageStore.url(c.image()),
                        waiting ? "Waiting for a chef" : null,        // small label on the photo
                        c.nom(), c.adresse(),
                        formatDates(c.debut(), c.fin()),
                        waiting ? "No chef yet" : c.chef(),           // the name at the bottom of the card
                        c.membres());

                String statut = c.statut() == null ? "" : c.statut();
                switch (statut) {
                    case ChantierDAO.PLANIFIE -> addCard(scheduledCards, scheduledCount, card);
                    case ChantierDAO.TERMINE  -> addCard(completedCards, completedCount, card);
                    default -> {
                        addCard(activeCards, activeCount, card);
                        makeClickable(card, c);            // hover + click: only the Active column
                    }
                }
            }
        });

        task.setOnFailed(e -> showPopup("Error",
                new Label("Cannot load the projects: " + task.getException().getMessage())));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
    private void makeClickable(VBox card, ChantierDAO.Card data) {
        card.getStyleClass().add("clickable");              // the hover style is in dashboard.css
        card.setOnMouseClicked(e -> openProjectPopup(data));
    }

    private void openProjectPopup(ChantierDAO.Card c) {
        Task<ChantierDAO.Details> task = new Task<>() {
            @Override protected ChantierDAO.Details call() throws Exception {
                return chantierDAO.details(c.id());
            }
        };
        task.setOnSucceeded(e -> showDetails(c, task.getValue()));
        task.setOnFailed(e -> showPopup("Error",
                new Label("Cannot load the project: " + task.getException().getMessage())));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void showDetails(ChantierDAO.Card c, ChantierDAO.Details d) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/FSA/view/project-details.fxml"));
            Node content = loader.load();
            ProjectDetailsController controller = loader.getController();

            String dates = formatDates(c.debut(), c.fin());

            // the small copy of the card (it is not clickable)
            VBox mini = buildCard(ImageStore.url(c.image()), null, c.nom(), c.adresse(), dates,
                    c.chef() == null ? "No chef yet" : c.chef(), c.membres());
            mini.getStyleClass().add("mini-card");

            controller.setProject(c, d, dates, mini);
            showPopup("", "popup-wide", content);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot open project-details.fxml", ex);
        }
    }
    private String formatDates(LocalDate start, LocalDate end) {
        if (start == null || end == null) return "";
        DateTimeFormatter month = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        return start.format(month) + " - " + end.format(month) + " " + end.getYear();
    }

    private HBox metaRow(String iconClass, String text) {
        Region icon = new Region();
        icon.getStyleClass().add(iconClass);
        Label label = new Label(text);
        label.getStyleClass().add("meta-text");
        HBox row = new HBox(icon, label);
        row.getStyleClass().add("meta");
        return row;
    }

    /** Adds a card to a column and updates the number in its badge. */
    public void addCard(VBox column, Label counter, VBox card) {
        column.getChildren().add(card);
        counter.setText(String.valueOf(column.getChildren().size()));
    }

    private void roundCorners(Region node, double ratio) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(node.widthProperty());
        clip.heightProperty().bind(node.heightProperty());
        clip.arcWidthProperty().bind(node.widthProperty().multiply(ratio));
        clip.arcHeightProperty().bind(node.widthProperty().multiply(ratio));
        node.setClip(clip);
    }
    private void showPopup(String title, Node... content) {
        popupCard.getStyleClass().removeAll("popup-form", "popup-wide");   // back to the normal size
        popupTitle.setText(title);
        popupContent.getChildren().setAll(content);

        popupLayer.setOpacity(0);
        popupLayer.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(150), popupLayer);
        fade.setToValue(1);
        fade.play();
    }

    // the Add Project form
    private void showPopup(String title, boolean form, Node... content) {
        showPopup(title, content);
        if (form) popupCard.getStyleClass().add("popup-form");
    }

    // NEW: any size defined in the CSS, e.g. "popup-wide" for the project details
    private void showPopup(String title, String sizeClass, Node... content) {
        showPopup(title, content);
        popupCard.getStyleClass().add(sizeClass);
    }
    private void hidePopup() {
        FadeTransition fade = new FadeTransition(Duration.millis(120), popupLayer);
        fade.setToValue(0);
        fade.setOnFinished(e -> popupLayer.setVisible(false));
        fade.play();
    }
}
