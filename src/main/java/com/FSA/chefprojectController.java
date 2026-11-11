package com.FSA;

import java.util.Locale;


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

/** Controller of home.fxml: the Dashboard page (attendance, materials, equipment, budget). */
public class chefprojectController {

    @FXML private StackPane root;
    @FXML private StackPane sidebar;
    @FXML private VBox sidebarContent;

    // ---- sidebar ----
    @FXML private Label avatarText;
    @FXML private Label profileName;
    @FXML private Label profileEmail;
    @FXML private Button settingsButton;
    @FXML private HBox searchRow;
    @FXML private TextField searchField;
    @FXML private Button dashboardButton;
    @FXML private Button projectsButton;
    @FXML private Button notificationsButton;
    @FXML private Button reportsButton;
    @FXML private Button teamButton;
    @FXML private Button logoutButton;
    @FXML private HBox themeRow;
    @FXML private StackPane themeSwitch;

    // ---- page ----
    @FXML private VBox attendanceList;
    @FXML private VBox materialsList;
    @FXML private VBox equipmentList;

    @FXML private Label budgetPill;
    @FXML private Label totalBudgetLabel;
    @FXML private Label spentLabel;
    @FXML private Label remainingLabel;
    @FXML private Label usedPercentLabel;
    @FXML private Label remainsLabel;
    @FXML private StackPane budgetBar;
    @FXML private Region budgetFill;


    @FXML private VBox attendanceCard;
    @FXML private VBox materialsCard;
    @FXML private VBox equipmentCard;
    @FXML private VBox budgetCard;

    @FXML
    private void initialize() {
        // one line replaces all the old sidebar code
        new SidebarBehavior(root, sidebar, sidebarContent, searchRow, searchField, themeRow, themeSwitch).install();

        // ---- sidebar actions ----
        settingsButton.setOnAction(e -> { /* TODO: open the settings screen */ });
        dashboardButton.setOnAction(e -> { /* TODO: already here */ });
        projectsButton.setOnAction(e -> { /* TODO: go to projects */ });
        notificationsButton.setOnAction(e -> { /* TODO: go to notifications */ });
        reportsButton.setOnAction(e -> { /* TODO: go to reports */ });
        teamButton.setOnAction(e -> { /* TODO: go to team */ });
        logoutButton.setOnAction(e -> { /* TODO: log out and go back to the login screen */ });

        // ---- click on a box -> pop-up ----
        attendanceCard.setOnMouseClicked(e -> openAttendancePopup());
        materialsCard.setOnMouseClicked(e -> openMaterialsPopup());
        equipmentCard.setOnMouseClicked(e -> openEquipmentPopup());
        budgetCard.setOnMouseClicked(e -> openBudgetPopup());

        loadSampleData();
    }

    // build and show your pop-ups in these 4 methods
    private void openAttendancePopup() { /* TODO */ }
    private void openMaterialsPopup()  { /* TODO */ }
    private void openEquipmentPopup()  { /* TODO */ }
    private void openBudgetPopup()     { /* TODO */ }

    // =====================================================================
    //  DATA: call these with real values from your database / services.
    //  Statuses: Present | Absent | Late          (attendance)
    //            Operational | Maintenance | Idle | Broken   (equipment)
    // =====================================================================

    /** Sample data from the design. Replace with your real data. */
    private void loadSampleData() {
        addAttendance("Abdelghafour Agdz", "Present");
        addAttendance("Elhoucine Araay", "Present");
        addAttendance("Fatima Zahra Bachkou", "Absent");
        addAttendance("Achraf nait bourdou", "Absent");
        addAttendance("Mouhssine Tazrakine", "Late");
        addAttendance("Abdessamad Bouderqa", "Late");

        addMaterial("Cement", 125, 200, "Tons", "bar-cement");
        addMaterial("Sand", 50, 100, "Tons", "bar-sand");
        addMaterial("Steel", 12, 80, "Tons", "bar-steel");
        addMaterial("Brick", 530, 700, "Tons", "bar-brick");

        addEquipment("Crane", "Operational");
        addEquipment("Crane", "Maintenance");
        addEquipment("Crane", "Idle");
        addEquipment("Crane", "Broken");

        setBudget(500_000, 320_000);
    }

    /** Changes the profile shown at the top of the sidebar (call it after login). */
    public void setUser(String name, String email) {
        profileName.setText(name);
        profileEmail.setText(email);
        String[] parts = name.trim().split("\\s+");
        String initials = parts[0].substring(0, 1)
                + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "");
        avatarText.setText(initials.toUpperCase());
    }

    public void addAttendance(String name, String status) {
        StackPane avatar = new StackPane(icon("avatar-sm-icon"));
        avatar.getStyleClass().add("avatar-sm");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("row-name");

        HBox row = new HBox(avatar, nameLabel, grow(), statusBox(status), icon("icon-chevron"));
        row.getStyleClass().addAll("data-row", "row-line");
        attendanceList.getChildren().add(row);
    }

    public void addEquipment(String name, String status) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("row-name");

        HBox row = new HBox(icon("icon-wrench"), nameLabel, grow(), statusBox(status));
        row.getStyleClass().addAll("data-row", "row-line");
        equipmentList.getChildren().add(row);
    }

    /**
     * @param colorClass CSS class of the bar colour: bar-cement, bar-sand, bar-steel or bar-brick
     *                   (add more in home.css)
     */
    public void addMaterial(String name, double current, double capacity, String unit, String colorClass) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("material-name");

        Label amount = new Label(number(current) + "/" + number(capacity) + " " + unit);
        amount.getStyleClass().add("muted");

        HBox top = new HBox(nameLabel, grow(), amount);
        top.setAlignment(Pos.CENTER_LEFT);

        double ratio = capacity <= 0 ? 0 : current / capacity;
        VBox row = new VBox(top, bar(ratio, colorClass));
        row.getStyleClass().add("material-row");
        materialsList.getChildren().add(row);
    }

    /** Updates the whole Budget Status card. */
    public void setBudget(double total, double spent) {
        double used = total <= 0 ? 0 : spent / total;

        totalBudgetLabel.setText(money(total));
        spentLabel.setText(money(spent));
        remainingLabel.setText(money(total - spent));
        usedPercentLabel.setText(Math.round(used * 100) + "%");
        remainsLabel.setText(Math.round(Math.max(0, 1 - used) * 100) + "% of budget remains");
        setRatio(budgetBar, budgetFill, used);

        // Thresholds are a suggestion: change them to your own rule.
        String state = used > 1 ? "bad" : used > 0.85 ? "warn" : "ok";
        budgetPill.getStyleClass().removeAll("pill-ok", "pill-warn", "pill-bad");
        budgetPill.getStyleClass().add("pill-" + state);
        budgetPill.setText(switch (state) {
            case "bad" -> "Over budget";
            case "warn" -> "At risk";
            default -> "On track";
        });
    }

    // =====================================================================
    //  small builders
    // =====================================================================

    private HBox statusBox(String status) {
        Region dot = new Region();
        dot.getStyleClass().addAll("dot", "dot-" + status.toLowerCase(Locale.ROOT));
        Label text = new Label(status);
        text.getStyleClass().add("status-text");
        HBox box = new HBox(dot, text);
        box.getStyleClass().add("status-box");
        return box;
    }

    private StackPane bar(double ratio, String colorClass) {
        Region fill = new Region();
        fill.getStyleClass().addAll("bar-fill", colorClass);
        StackPane track = new StackPane(fill);
        track.getStyleClass().add("bar-track");
        track.setAlignment(Pos.CENTER_LEFT);
        setRatio(track, fill, ratio);
        return track;
    }

    /** Makes the coloured fill take `ratio` (0 to 1) of the track width, also when resized. */
    private void setRatio(StackPane track, Region fill, double ratio) {
        double r = Math.max(0, Math.min(1, ratio));
        fill.prefWidthProperty().unbind();
        fill.maxWidthProperty().unbind();
        fill.setMinWidth(0);
        fill.setMaxHeight(Double.MAX_VALUE);
        fill.prefWidthProperty().bind(track.widthProperty().multiply(r));
        fill.maxWidthProperty().bind(track.widthProperty().multiply(r));
    }

    private Region icon(String styleClass) {
        Region r = new Region();
        r.getStyleClass().add(styleClass);
        return r;
    }

    private Region grow() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private String number(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private String money(double v) {
        return String.format(Locale.US, "%,.0f DH", v);
    }

}
