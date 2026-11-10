package com.FSA;

import java.util.Locale;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

public class DashboardController {

    @FXML private StackPane root;
    @FXML private StackPane sidebar;
    @FXML private VBox sidebarContent;

    // sidebar
    @FXML private Button settingsButton;
    //@FXML private HBox searchRow;
    //@FXML private TextField searchField;
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

    // Base font size (px) at a 1280x720 window. Raise it to make everything bigger.
    private static final double BASE_FONT = 13.0;
    // Sidebar widths in em: icon rail when idle, full width while hovered.
    // (The rail must match the left padding of .main in dashboard.css: 5em)
    private static final double RAIL_EM = 4.2;
    private static final double OPEN_EM = 17.0;

    private static final PseudoClass ON = PseudoClass.getPseudoClass("on");

    private final DoubleProperty fontPx = new SimpleDoubleProperty(BASE_FONT);
    private final DoubleProperty sidebarWidth = new SimpleDoubleProperty(0);
    private Timeline timeline;
    private boolean hovered = false;
    private boolean dark = false;

    @FXML
    private void initialize() {

        // Clip the sidebar so nothing sticks out while it is just icons
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(sidebar.widthProperty());
        clip.heightProperty().bind(sidebar.heightProperty());
        sidebar.setClip(clip);

        sidebarWidth.addListener((obs, oldV, newV) -> applySidebarWidth(newV.doubleValue()));

        // Scale the text and icons with the window
        root.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;

            fontPx.bind(Bindings.min(
                    scene.widthProperty().divide(1280.0),
                    scene.heightProperty().divide(720.0)
            ).multiply(BASE_FONT));

            root.styleProperty().bind(Bindings.format(Locale.US, "-fx-font-size: %.2fpx;", fontPx));

            sidebarWidth.set(railWidth());
            applySidebarWidth(railWidth());
        });

        // When the window is resized, keep the sidebar at the right size
        fontPx.addListener((obs, oldV, newV) -> {
            if (timeline != null) timeline.stop();
            sidebarWidth.set(hovered ? openWidth() : railWidth());
            applySidebarWidth(sidebarWidth.get());
        });

        // Expand on hover, shrink back to the icons when the mouse leaves
        sidebar.setOnMouseEntered(e -> slideTo(true));
        sidebar.setOnMouseExited(e -> slideTo(false));

        // Round the corners of cards (images are not clipped by the CSS radius)
        root.lookupAll(".card").forEach(n -> roundCorners((Region) n, 0.2));

        // ---------- actions ----------
        addProjectButton.setOnAction(e -> {
            // TODO: open the "add project" form / dialog
        });

        settingsButton.setOnAction(e -> {
            // TODO: open the settings screen
        });

        //searchRow.setOnMouseClicked(e -> searchField.requestFocus());

        dashboardButton.setOnAction(e -> {
            // TODO: go to the dashboard
        });
        projectsButton.setOnAction(e -> {
            // TODO: go to projects
        });
        notificationsButton.setOnAction(e -> {
            // TODO: go to notifications
        });
        reportsButton.setOnAction(e -> {
            // TODO: go to reports
        });
        teamButton.setOnAction(e -> {
            // TODO: go to team
        });
        logoutButton.setOnAction(e -> {
            // TODO: log out and go back to the login screen
        });

        // Dark mode switch: the page is white by default
        themeRow.setOnMouseClicked(e -> {
            dark = !dark;
            root.getStyleClass().remove("dark");
            if (dark) root.getStyleClass().add("dark");
            themeSwitch.pseudoClassStateChanged(ON, dark);
        });
    }

    // ---------------- sidebar ----------------

    private double railWidth() { return fontPx.get() * RAIL_EM; }
    private double openWidth() { return fontPx.get() * OPEN_EM; }

    private void applySidebarWidth(double w) {
        sidebar.setMinWidth(w);
        sidebar.setPrefWidth(w);
        sidebar.setMaxWidth(w);

        // The content follows the sidebar width so the rounded item backgrounds
        // stay inside the rail (instead of being cut off at the edge).
        sidebarContent.setMinWidth(w);
        sidebarContent.setPrefWidth(w);
        sidebarContent.setMaxWidth(w);
    }

    private void slideTo(boolean open) {
        hovered = open;
        if (timeline != null) timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.millis(200),
                new KeyValue(sidebarWidth, open ? openWidth() : railWidth(), Interpolator.EASE_BOTH)));
        timeline.play();
    }

    // ---------------- project cards ----------------

    /**
     * Builds a project card with the same look as the design.
     *
     * @param imageClass CSS class that holds the photo, e.g. "img-warehouse"
     * @param tag        small label on the photo (e.g. "Next"), or null
     * @param members    number of members, or null to hide the badge
     */
    public VBox buildCard(String imageClass, String tag, String title, String location,
                          String dates, String owner, Integer members) {

        StackPane img = new StackPane();
        img.getStyleClass().addAll("card-img", imageClass);
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
}
