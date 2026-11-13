package com.FSA;

import java.util.Locale;

import javafx.animation.*;
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
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import com.FSA.DAO.ChantierDAO;
import javafx.scene.control.PasswordField;
import java.sql.SQLException;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

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
        img.getStyleClass().add("card-img");
        if (imageUrl != null) {
            img.setStyle("-fx-background-image: url(\"" + imageUrl + "\");");
        }
        img.setAlignment(Pos.TOP_LEFT);
        // dark layer over the photo: invisible, it only shows on hover (see the CSS)
        Region overlay = new Region();
        overlay.getStyleClass().add("card-overlay");
        overlay.setMouseTransparent(true);
        img.getChildren().add(overlay);

        if (tag != null) {
            Label t = new Label(tag);
            t.getStyleClass().add("tag");
            img.getChildren().add(t);
        }
        Label chip = new Label("View details");
        chip.getStyleClass().add("view-chip");
        chip.setMouseTransparent(true);
        StackPane.setAlignment(chip, Pos.BOTTOM_RIGHT);
        img.getChildren().add(chip);

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
            Label badge = new Label(members + (members == 1 ? " member" : " members"));
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
        roundCorners(card, 0.18);
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
                    case ChantierDAO.PLANIFIE -> {
                        addCard(scheduledCards, scheduledCount, card);
                        if (waiting) makeAssignable(card, c);      // NEW: only cards with no chef
                    }
                    case ChantierDAO.TERMINE  -> addCard(completedCards, completedCount, card);
                    default -> {
                        addCard(activeCards, activeCount, card);
                        makeClickable(card, c);
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
    private void makeAssignable(Node card, ChantierDAO.Card c) {
        card.getStyleClass().add("clickable");           // same hover as the Active cards
        Node chip = card.lookup(".view-chip");
        if (chip instanceof Label l) l.setText("Assign chef");
        card.setOnMouseClicked(e -> openAssignPopup(c));
    }

    private void openAssignPopup(ChantierDAO.Card c) {
        // ---- the two tabs ----
        ToggleButton existingTab = new ToggleButton("Existing chef");
        ToggleButton newTab = new ToggleButton("New chef");
        existingTab.getStyleClass().addAll("seg", "seg-left");
        newTab.getStyleClass().addAll("seg", "seg-right");
        ToggleGroup group = new ToggleGroup();
        existingTab.setToggleGroup(group);
        newTab.setToggleGroup(group);
        existingTab.setSelected(true);
        group.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null) old.setSelected(true);          // one tab is always selected
        });
        HBox tabs = new HBox(existingTab, newTab);

        // ---- tab 1: a chef that is already in the database ----
        ComboBox<ChantierDAO.ChefOption> chefBox = new ComboBox<>();
        chefBox.setMaxWidth(Double.MAX_VALUE);
        chefBox.setPromptText("Loading chefs...");
        Label existingHint = new Label("Only chefs who don't run a project yet are listed.");
        existingHint.getStyleClass().add("form-hint");
        VBox existingPane = new VBox(8, chefBox, existingHint);

        // ---- tab 2: create a new chef ----
        TextField first = new TextField();
        first.setPromptText("First name");
        TextField last = new TextField();
        last.setPromptText("Last name");
        HBox names = new HBox(10, first, last);
        HBox.setHgrow(first, Priority.ALWAYS);
        HBox.setHgrow(last, Priority.ALWAYS);
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password (min. 6 characters)");
        Label hint = new Label("Account: firstname.lastname@cms.ma");
        hint.getStyleClass().add("form-hint");
        VBox newPane = new VBox(10, names, pass, hint);

        Runnable preview = () -> {
            String p = first.getText().trim(), n = last.getText().trim();
            hint.setText(p.isEmpty() || n.isEmpty()
                    ? "Account: firstname.lastname@cms.ma"
                    : "Account: " + ChantierDAO.emailPour(p, n));
        };
        first.textProperty().addListener((o, a, b) -> preview.run());
        last.textProperty().addListener((o, a, b) -> preview.run());

        // show only the pane of the selected tab
        existingPane.visibleProperty().bind(existingTab.selectedProperty());
        existingPane.managedProperty().bind(existingPane.visibleProperty());
        newPane.visibleProperty().bind(newTab.selectedProperty());
        newPane.managedProperty().bind(newPane.visibleProperty());

        Label error = new Label();
        error.setStyle("-fx-text-fill: #dc2626;");
        error.setWrapText(true);

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("btn-secondary");
        Button assign = new Button("Assign chef");
        assign.getStyleClass().add("btn-primary");
        HBox buttons = new HBox(10, cancel, assign);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        cancel.setOnAction(e -> hidePopup());

        // ---- load the free chefs in the background ----
        Task<List<ChantierDAO.ChefOption>> load = new Task<>() {
            @Override protected List<ChantierDAO.ChefOption> call() throws Exception {
                return chantierDAO.chefsDisponibles();
            }
        };
        load.setOnSucceeded(e -> {
            List<ChantierDAO.ChefOption> chefs = load.getValue();
            if (chefs.isEmpty()) {
                existingTab.setDisable(true);                // nobody to choose from
                newTab.setSelected(true);
                existingHint.setText("No free chef: create a new one.");
            } else {
                chefBox.getItems().setAll(chefs);
                chefBox.setPromptText("Choose a chef");
            }
        });
        load.setOnFailed(e -> error.setText("Could not load the chefs: " + load.getException().getMessage()));
        new Thread(load).start();

        // ---- the Assign button ----
        assign.setOnAction(e -> {
            error.setText("");
            String statut = ChantierDAO.statutPour(c.debut(), c.fin());
            Task<Void> task;

            if (existingTab.isSelected()) {
                ChantierDAO.ChefOption chosen = chefBox.getValue();
                if (chosen == null) { error.setText("Choose a chef."); return; }
                task = new Task<>() {
                    @Override protected Void call() throws Exception {
                        chantierDAO.assignerChefExistant(c.id(), chosen.id(), statut);
                        return null;
                    }
                };
            } else {
                String p = first.getText().trim(), n = last.getText().trim(), m = pass.getText();
                if (p.isEmpty() || n.isEmpty() || m.isEmpty()) {
                    error.setText("Fill the first name, last name and password."); return;
                }
                if (m.length() < 6) {
                    error.setText("The password must have at least 6 characters."); return;
                }
                String email = ChantierDAO.emailPour(p, n);
                if (email.startsWith(".") || email.contains("..") || email.startsWith("@")) {
                    error.setText("The name must contain letters or digits."); return;
                }
                task = new Task<>() {
                    @Override protected Void call() throws Exception {
                        chantierDAO.assignerChef(c.id(), p, n, m, statut);
                        return null;
                    }
                };
            }

            assign.setDisable(true);
            Task<Void> running = task;
            running.setOnSucceeded(ev -> { hidePopup(); loadProjects(); });
            running.setOnFailed(ev -> {
                assign.setDisable(false);
                Throwable t = running.getException();
                error.setText(t instanceof SQLException s && "23505".equals(s.getSQLState())
                        ? "This chef (or this email) is already used by another project."
                        : "Error: " + t.getMessage());
            });
            new Thread(running).start();
        });

        VBox form = new VBox(14, new Label("Chef de projet"), tabs, existingPane, newPane, error, buttons);
        showPopup("Assign a chef to " + c.nom(), true, form);
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
        clip.arcWidthProperty().bind(node.heightProperty().multiply(ratio));    // was widthProperty
        clip.arcHeightProperty().bind(node.heightProperty().multiply(ratio));
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
