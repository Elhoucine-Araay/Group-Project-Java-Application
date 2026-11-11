package com.FSA;

import java.util.Locale;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SidebarBehavior {

    private static final double BASE_FONT = 13.0;
    private static final double DESIGN_WIDTH = 1280.0;
    private static final double DESIGN_HEIGHT = 720.0;
    private static final double RAIL_EM = 5.0;    // closed width (icons only)
    private static final double OPEN_EM = 17.0;   // open width (on hover)

    private static final PseudoClass ON = PseudoClass.getPseudoClass("on");

    // NEW: everything that is "text" and must disappear when the sidebar is closed
    private static final String[] FADING = {
            ".profile-text", ".nav-label", ".switch-track", ".icon-btn"
    };

    private final StackPane root;
    private final StackPane sidebar;
    private final VBox content;
    private final HBox themeRow;
    private final StackPane themeSwitch;

    private final DoubleProperty fontPx = new SimpleDoubleProperty(BASE_FONT);
    private final DoubleProperty width = new SimpleDoubleProperty(0);
    private Timeline timeline;
    private boolean hovered = false;
    private boolean dark = false;

    public SidebarBehavior(StackPane root, StackPane sidebar, VBox content,
                           HBox themeRow, StackPane themeSwitch) {
        this.root = root;
        this.sidebar = sidebar;
        this.content = content;
        this.themeRow = themeRow;
        this.themeSwitch = themeSwitch;
    }

    public void install() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(sidebar.widthProperty());
        clip.heightProperty().bind(sidebar.heightProperty());
        sidebar.setClip(clip);

        width.addListener((obs, oldV, newV) -> apply(newV.doubleValue()));

        root.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;
            fontPx.bind(Bindings.min(
                    scene.widthProperty().divide(DESIGN_WIDTH),
                    scene.heightProperty().divide(DESIGN_HEIGHT)
            ).multiply(BASE_FONT));
            root.styleProperty().bind(Bindings.format(Locale.US, "-fx-font-size: %.2fpx;", fontPx));
            width.set(railWidth());
            apply(railWidth());
        });

        fontPx.addListener((obs, oldV, newV) -> {
            if (timeline != null) timeline.stop();
            width.set(hovered ? openWidth() : railWidth());
            apply(width.get());
        });

        // NEW: a button's text node only exists after its "skin" is created,
        // so when that happens we apply the fade again.
        for (Node n : content.lookupAll(".nav-item")) {
            if (n instanceof Control c) {
                c.skinProperty().addListener((obs, o, s) -> Platform.runLater(() -> apply(width.get())));
            }
        }

        sidebar.setOnMouseEntered(e -> slideTo(true));
        sidebar.setOnMouseExited(e -> slideTo(false));


        themeRow.setOnMouseClicked(e -> {
            dark = !dark;
            root.getStyleClass().remove("dark");
            if (dark) root.getStyleClass().add("dark");
            themeSwitch.pseudoClassStateChanged(ON, dark);
        });
    }

    private double railWidth() { return fontPx.get() * RAIL_EM; }
    private double openWidth() { return fontPx.get() * OPEN_EM; }

    private void apply(double w) {
        sidebar.setMinWidth(w);  sidebar.setPrefWidth(w);  sidebar.setMaxWidth(w);
        content.setMinWidth(w);  content.setPrefWidth(w);  content.setMaxWidth(w);

        // NEW: progress = 0 when closed, 1 when fully open.
        // The text stays invisible for the first 40% of the slide, then fades in.
        double rail = railWidth();
        double open = openWidth();
        double progress = open > rail ? (w - rail) / (open - rail) : 1;
        double opacity = Math.max(0, Math.min(1, (progress - 0.4) / 0.6));
        fadeText(opacity);
    }

    // NEW
    private void fadeText(double opacity) {
        for (String selector : FADING) {
            for (Node n : content.lookupAll(selector)) {
                n.setOpacity(opacity);
            }
        }
        // A button has an icon AND text; we only want to hide the text.
        // lookup(".text") finds the text node inside the button.
        for (Node button : content.lookupAll(".nav-item")) {
            Node text = button.lookup(".text");
            if (text != null) text.setOpacity(opacity);
        }
    }

    private void slideTo(boolean open) {
        hovered = open;
        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.millis(200),
                new KeyValue(width, open ? openWidth() : railWidth(), Interpolator.EASE_BOTH)));
        timeline.play();
    }
}