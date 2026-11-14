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
import com.FSA.DAO.ChefDashboardDAO;
import javafx.concurrent.Task;
import com.FSA.DAO.ChefActionsDAO;
import com.FSA.DAO.ChefDashboardDAO;
import java.math.BigDecimal;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
//--popup
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

/** Controller of home.fxml: the Dashboard page (attendance, materials, equipment, budget). */
public class chefprojectController {

    @FXML private StackPane root;
    @FXML private StackPane sidebar;
    @FXML private VBox sidebarContent;
    // ---- pop-up ----
    @FXML private StackPane popupLayer;
    @FXML private VBox popupCard;
    @FXML private Label popupTitle;
    @FXML private VBox popupContent;
    @FXML private Button popupClose;

    // ---- sidebar ----
    @FXML private Label avatarText;
    @FXML private Label profileName;
    @FXML private Label profileEmail;
    @FXML private Button settingsButton;
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
        new SidebarBehavior(root, sidebar, sidebarContent, themeRow, themeSwitch).install();

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

        // ---- click on a box -> pop-up ----
        attendanceCard.setOnMouseClicked(e -> openAttendancePopup());
        materialsCard.setOnMouseClicked(e -> openMaterialsPopup());
        equipmentCard.setOnMouseClicked(e -> openEquipmentPopup());
        budgetCard.setOnMouseClicked(e -> openBudgetPopup());

        //popup
        popupClose.setOnAction(e -> hidePopup());            // the X button closes it
        popupLayer.setOnMouseClicked(e -> hidePopup());      // a click on the dark background closes it
        popupCard.setOnMouseClicked(e -> e.consume());       // but a click inside the card must NOT close it

// Escape closes it too
        root.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.ESCAPE && popupLayer.isVisible()) hidePopup();
                });
            }
        });
    }
    private void showPopup(String title, Node... content) {
        popupTitle.setText(title);
        popupContent.getChildren().setAll(content);
        if (popupLayer.isVisible() && popupLayer.getOpacity() > 0.5) return;   // already open

        popupLayer.setOpacity(0);
        popupLayer.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(150), popupLayer);
        fade.setToValue(1);
        fade.play();
    }

    private void hidePopup() {
        FadeTransition fade = new FadeTransition(Duration.millis(120), popupLayer);
        fade.setToValue(0);
        fade.setOnFinished(e -> popupLayer.setVisible(false));
        fade.play();
    }
    // build and show your pop-ups in these 4 methods
    private void openAttendancePopup() {
        if (current == null) { noProject("Attendance"); return; }
        int idChantier = current.projet().id();
        Label error = errorLabel();

        // one row per member: the name and a status you can change
        VBox list = new VBox(8);
        for (ChefDashboardDAO.Worker w : current.ouvriers()) {
            Label name = new Label(w.nom());
            name.getStyleClass().add("row-name");
            ComboBox<String> status = statusBox(w.statut(), "Present", "Absent", "Late");
            status.setOnAction(e -> {
                if (status.getValue().equals(w.statut())) return;
                save(() -> actions.changerPresence(w.id(), status.getValue()),
                        error, null, this::openAttendancePopup);
            });
            HBox row = new HBox(10, name, grow(), status);
            row.setAlignment(Pos.CENTER_LEFT);
            list.getChildren().add(row);
        }
        if (current.ouvriers().isEmpty()) emptyRow(list, "No members yet.");

        // add a member
        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        HBox.setHgrow(nameField, Priority.ALWAYS);
        ComboBox<String> newStatus = statusBox("Present", "Present", "Absent", "Late");
        Button add = primaryButton("Add member");
        add.setOnAction(e -> {
            error.setText("");
            String n = nameField.getText().trim();
            if (n.isEmpty())      { error.setText("Type the name of the member."); return; }
            if (n.length() > 150) { error.setText("The name is too long."); return; }
            String st = newStatus.getValue();
            save(() -> actions.ajouterOuvrier(idChantier, n, st), error, add, this::openAttendancePopup);
        });

        showPopup("Attendance", scroll(list), sectionTitle("Add a member"),
                new HBox(10, nameField, newStatus, add), error);
    }

    private void openBudgetPopup() {
        if (current == null) { noProject("Budget Status"); return; }
        int idChantier = current.projet().id();
        Label error = errorLabel();

        TextField amount = new TextField();
        amount.setPromptText("Extra amount in DH, e.g. 50000");
        TextField reason = new TextField();
        reason.setPromptText("Why do you need it?");
        Button send = primaryButton("Request more budget");
        send.setOnAction(e -> {
            error.setText("");
            BigDecimal value = parseNumber(amount.getText());
            String why = reason.getText().trim();
            if (value == null || value.signum() <= 0) { error.setText("The amount must be a number above 0."); return; }
            if (value.compareTo(new BigDecimal("999999999999")) > 0) { error.setText("The amount is too big."); return; }
            if (why.isEmpty())      { error.setText("Tell why you need more budget."); return; }
            if (why.length() > 255) { error.setText("The reason is too long (255 characters maximum)."); return; }
            save(() -> actions.demanderBudget(idChantier, value, why), error, send, this::openBudgetPopup);
        });

        VBox history = new VBox(6);
        fillHistory("BUDGET", history, idChantier);

        showPopup("Budget Status",
                new Label("Total: " + totalBudgetLabel.getText()),
                new Label("Spent: " + spentLabel.getText()),
                new Label("Remaining: " + remainingLabel.getText()),
                sectionTitle("Request more budget"), amount, reason, send, error,
                sectionTitle("Your requests"), scroll(history));
    }

    private void openMaterialsPopup() {
        if (current == null) { noProject("Materials Inventory"); return; }
        int idChantier = current.projet().id();
        Label error = errorLabel();

        ComboBox<ChefActionsDAO.MaterielOption> material = new ComboBox<>();
        material.setMaxWidth(Double.MAX_VALUE);
        material.setPromptText("Loading materials...");
        TextField quantity = new TextField();
        quantity.setPromptText("Quantity");
        TextField reason = new TextField();
        reason.setPromptText("Why do you need it?");
        Button send = primaryButton("Request material");

        Task<List<ChefActionsDAO.MaterielOption>> load = new Task<>() {
            @Override protected List<ChefActionsDAO.MaterielOption> call() throws Exception {
                return actions.materielsCatalogue();
            }
        };
        load.setOnSucceeded(e -> {
            material.getItems().setAll(load.getValue());
            material.setPromptText(load.getValue().isEmpty() ? "No material in the database yet" : "Choose a material");
        });
        load.setOnFailed(e -> error.setText("Could not load the materials: " + load.getException().getMessage()));
        background(load);

        send.setOnAction(e -> {
            error.setText("");
            ChefActionsDAO.MaterielOption chosen = material.getValue();
            BigDecimal q = parseNumber(quantity.getText());
            String why = reason.getText().trim();
            if (chosen == null)                  { error.setText("Choose a material."); return; }
            if (q == null || q.signum() <= 0)    { error.setText("The quantity must be a number above 0."); return; }
            if (q.compareTo(new BigDecimal("1000000000")) > 0) { error.setText("The quantity is too big."); return; }
            if (why.isEmpty())                   { error.setText("Tell why you need it."); return; }
            if (why.length() > 255)              { error.setText("The reason is too long (255 characters maximum)."); return; }
            save(() -> actions.demanderMateriel(idChantier, chosen.id(), q.doubleValue(), why),
                    error, send, this::openMaterialsPopup);
        });

        VBox history = new VBox(6);
        fillHistory("MATERIEL", history, idChantier);

        showPopup("Materials Inventory",
                sectionTitle("Request more material"), material, quantity, reason, send, error,
                sectionTitle("Your requests"), scroll(history));
    }

    private void openEquipmentPopup() {
        if (current == null) { noProject("Equipment Status"); return; }
        int idChantier = current.projet().id();
        Label error = errorLabel();
        String[] states = {"Operational", "Maintenance", "Idle", "Broken"};

        // one row per machine: the name and a status you can change
        VBox list = new VBox(8);
        for (ChefDashboardDAO.Equipment q : current.machines()) {
            Label name = new Label(q.nom());
            name.getStyleClass().add("row-name");
            ComboBox<String> status = statusBox(q.statut(), states);
            status.setOnAction(e -> {
                if (status.getValue().equals(q.statut())) return;
                save(() -> actions.changerEtatMachine(q.id(), status.getValue()),
                        error, null, this::openEquipmentPopup);
            });
            HBox row = new HBox(10, name, grow(), status);
            row.setAlignment(Pos.CENTER_LEFT);
            list.getChildren().add(row);
        }
        if (current.machines().isEmpty()) emptyRow(list, "No machines yet.");

        // add a machine
        TextField nameField = new TextField();
        nameField.setPromptText("Machine name");
        HBox.setHgrow(nameField, Priority.ALWAYS);
        TextField typeField = new TextField();
        typeField.setPromptText("Type (optional)");
        ComboBox<String> newStatus = statusBox("Operational", states);
        Button add = primaryButton("Add machine");
        add.setOnAction(e -> {
            error.setText("");
            String n = nameField.getText().trim();
            String type = typeField.getText().trim();
            if (n.isEmpty())      { error.setText("Type the name of the machine."); return; }
            if (n.length() > 100 || type.length() > 100) { error.setText("The text is too long (100 characters maximum)."); return; }
            String st = newStatus.getValue();
            save(() -> actions.ajouterMachine(idChantier, n, type.isEmpty() ? null : type, st),
                    error, add, this::openEquipmentPopup);
        });

        showPopup("Equipment Status", scroll(list), sectionTitle("Add a machine"),
                new HBox(10, nameField, typeField), new HBox(10, newStatus, add), error);
    }

    // =====================================================================
    //  DATA: call these with real values from your database / services.
    //  Statuses: Present | Absent | Late          (attendance)
    //            Operational | Maintenance | Idle | Broken   (equipment)
    // =====================================================================
    private final ChefDashboardDAO dao = new ChefDashboardDAO();
    private final ChefActionsDAO actions = new ChefActionsDAO();
    private int chefId;
    private ChefDashboardDAO.Data current;
    private interface Job { void run() throws Exception; }

    private void background(Task<?> task) {
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    /** Runs a database write in the background, then reloads the page and runs `afterOk`. */
    private void save(Job job, Label error, Button button, Runnable afterOk) {
        if (button != null) button.setDisable(true);
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception { job.run(); return null; }
        };
        t.setOnSucceeded(e -> reload(afterOk));
        t.setOnFailed(e -> {
            if (button != null) button.setDisable(false);
            error.setText("Error: " + t.getException().getMessage());
        });
        background(t);
    }

    private Label errorLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #dc2626;");
        l.setWrapText(true);
        return l;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-primary");
        return b;
    }

    private ComboBox<String> statusBox(String selected, String... items) {
        ComboBox<String> box = new ComboBox<>(FXCollections.observableArrayList(items));
        box.setValue(selected);
        return box;
    }

    private ScrollPane scroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPrefViewportHeight(240);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return sp;
    }

    private void noProject(String title) {
        showPopup(title, new Label("No project is assigned to you yet."));
    }

    private BigDecimal parseNumber(String text) {
        try {
            return new BigDecimal(text.trim().replace(",", "").replace(" ", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Fills `box` with the requests of the project ("BUDGET" or "MATERIEL") and their status. */
    private void fillHistory(String type, VBox box, int idChantier) {
        Task<List<ChefActionsDAO.Request>> t = new Task<>() {
            @Override protected List<ChefActionsDAO.Request> call() throws Exception {
                return actions.demandes(idChantier, type);
            }
        };
        t.setOnSucceeded(e -> {
            box.getChildren().clear();
            for (ChefActionsDAO.Request r : t.getValue()) {
                Label text = new Label(r.texte());
                text.setWrapText(true);
                Label st = new Label(r.statut());
                st.getStyleClass().add("muted");
                HBox row = new HBox(10, text, grow(), st);
                row.setAlignment(Pos.CENTER_LEFT);
                box.getChildren().add(row);
            }
            if (t.getValue().isEmpty()) emptyRow(box, "No requests yet.");
        });
        background(t);
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
    /** Called by the login: fills the page with the project of this chef. */
    public void setChef(int idUtilisateur, String nom, String email) {
        setUser(nom, email);
        this.chefId = idUtilisateur;
        reload(null);
    }

    private void reload(Runnable after) {
        Task<ChefDashboardDAO.Data> task = new Task<>() {
            @Override protected ChefDashboardDAO.Data call() throws Exception {
                return dao.pourChef(chefId);
            }
        };
        task.setOnSucceeded(e -> {
            show(task.getValue());
            if (after != null) after.run();
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            clearLists();
            emptyRow(attendanceList, "Could not load: " + task.getException().getMessage());
        });
        background(task);
    }

    private void show(ChefDashboardDAO.Data d) {
        current = d;
        clearLists();

        if (d == null) {                                   // chef without a project
            setBudget(0, 0);
            budgetPill.setText("No project");
            emptyRow(attendanceList, "No project is assigned to you yet.");
            return;
        }

        for (ChefDashboardDAO.Worker w : d.ouvriers()) addAttendance(w.nom(), w.statut());
        if (d.ouvriers().isEmpty()) emptyRow(attendanceList, "No workers assigned yet.");

        String[] colors = {"bar-cement", "bar-sand", "bar-steel", "bar-brick"};
        int i = 0;
        for (ChefDashboardDAO.Material m : d.materiaux()) {
            addMaterial(m.nom(), m.utilisee(), m.prevue(), m.unite(), barClass(m.nom(), colors[i++ % 4]));
        }
        if (d.materiaux().isEmpty()) emptyRow(materialsList, "No materials assigned yet.");

        for (ChefDashboardDAO.Equipment q : d.machines()) addEquipment(q.nom(), q.statut());
        if (d.machines().isEmpty()) emptyRow(equipmentList, "No equipment assigned yet.");

        setBudget(d.projet().budget(), d.projet().depenses());
    }

    private void clearLists() {
        attendanceList.getChildren().clear();
        materialsList.getChildren().clear();
        equipmentList.getChildren().clear();
    }

    private void emptyRow(VBox list, String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        list.getChildren().add(l);
    }

    /** Cement, sand, steel keep their colour; any other material gets the fallback one. */
    private String barClass(String name, String fallback) {
        String n = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (n.contains("cement") || n.contains("ciment")) return "bar-cement";
        if (n.contains("sand") || n.contains("sable"))    return "bar-sand";
        if (n.contains("steel") || n.contains("acier"))   return "bar-steel";
        if (n.contains("brick") || n.contains("brique"))  return "bar-brick";
        return fallback;
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
