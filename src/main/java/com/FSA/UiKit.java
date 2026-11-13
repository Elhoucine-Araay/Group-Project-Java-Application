package com.FSA;

import java.util.Locale;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public final class UiKit {

    private UiKit() {}

    public static Region icon(String styleClass) {
        Region r = new Region();
        r.getStyleClass().add(styleClass);
        return r;
    }

    public static Region grow() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    public static StackPane bar(double ratio, String colorClass) {
        Region fill = new Region();
        fill.getStyleClass().addAll("bar-fill", colorClass);
        StackPane track = new StackPane(fill);
        track.getStyleClass().add("bar-track");
        track.setAlignment(Pos.CENTER_LEFT);
        setRatio(track, fill, ratio);
        return track;
    }

    /** Makes the coloured fill take `ratio` (0 to 1) of the track width, also when resized. */
    public static void setRatio(StackPane track, Region fill, double ratio) {
        double r = Math.max(0, Math.min(1, ratio));
        fill.prefWidthProperty().unbind();
        fill.maxWidthProperty().unbind();
        fill.setMinWidth(0);
        fill.setMaxHeight(Double.MAX_VALUE);
        fill.prefWidthProperty().bind(track.widthProperty().multiply(r));
        fill.maxWidthProperty().bind(track.widthProperty().multiply(r));
    }

    public static String number(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    public static String money(double v) {
        return String.format(Locale.US, "%,.0f DH", v);
    }

    /** Bar colour from the material name: cement, sand, steel, brick. Others get the steel colour. */
    public static String materialColor(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "cement" -> "bar-cement";
            case "sand"   -> "bar-sand";
            case "brick"  -> "bar-brick";
            default       -> "bar-steel";
        };
    }

    /** "On track" / "At risk" / "Over budget", from the share of the budget used (0 to 1+). */
    public static void updatePill(Label pill, double used) {
        String state = used > 1 ? "bad" : used > 0.85 ? "warn" : "ok";
        pill.getStyleClass().removeAll("pill-ok", "pill-warn", "pill-bad");
        pill.getStyleClass().add("pill-" + state);
        pill.setText(switch (state) {
            case "bad" -> "Over budget";
            case "warn" -> "At risk";
            default -> "On track";
        });
    }
}