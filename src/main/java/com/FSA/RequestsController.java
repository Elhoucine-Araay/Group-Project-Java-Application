package com.FSA;

import com.FSA.DAO.DemandeDAO;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Requests page of the chef de département: approve or reject the requests of the project chefs. */
public class RequestsController {

    @FXML private StackPane root;
    @FXML private StackPane sidebar;
    @FXML private VBox sidebarContent;
    @FXML private Button settingsButton, dashboardButton, projectsRButton,
            notificationsButton, reportsButton, teamButton, logoutButton;
    @FXML private HBox themeRow;
    @FXML private StackPane themeSwitch;

    @FXML private HBox tabsBox;
    @FXML private VBox requestList;

    private final DemandeDAO dao = new DemandeDAO();
    private String filter = "EN_ATTENTE";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);

    @FXML
    private void initialize() {
        new SidebarBehavior(root, sidebar, sidebarContent, themeRow, themeSwitch).install();

        // ---- sidebar ----
        dashboardButton.setOnAction(e -> Navigation.go(root, "dashboard.fxml"));
        projectsRButton.setOnAction(e -> { /* already here */ });
        logoutButton.setOnAction(e -> Navigation.go(root, "login.fxml"));

        // ---- the three tabs ----
        ToggleGroup group = new ToggleGroup();
        ToggleButton pending = tab("Pending", "seg-left", "EN_ATTENTE", group);
        ToggleButton approved = tab("Approved", "seg-mid", "APPROUVEE", group);
        ToggleButton rejected = tab("Rejected", "seg-right", "REFUSEE", group);
        tabsBox.getChildren().addAll(pending, approved, rejected);
        pending.setSelected(true);
        group.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null) old.setSelected(true);                  // one tab is always selected
        });

        load();
    }

    private ToggleButton tab(String text, String position, String statut, ToggleGroup group) {
        ToggleButton b = new ToggleButton(text);
        b.getStyleClass().addAll("seg", position);
        b.setToggleGroup(group);
        b.selectedProperty().addListener((o, was, is) -> {
            if (is) { filter = statut; load(); }
        });
        return b;
    }

    private void load() {
        String statut = filter;
        Task<List<DemandeDAO.Row>> task = new Task<>() {
            @Override protected List<DemandeDAO.Row> call() throws Exception {
                return dao.liste(statut);
            }
        };
        task.setOnSucceeded(e -> {
            requestList.getChildren().clear();
            for (DemandeDAO.Row r : task.getValue()) requestList.getChildren().add(card(r, statut));
            if (task.getValue().isEmpty()) {
                Label empty = new Label(switch (statut) {
                    case "EN_ATTENTE" -> "No pending requests.";
                    case "APPROUVEE"  -> "No approved requests yet.";
                    default           -> "No rejected requests.";
                });
                empty.getStyleClass().add("req-empty");
                requestList.getChildren().add(empty);
            }
        });
        task.setOnFailed(e -> {
            requestList.getChildren().clear();
            Label err = new Label("Could not load the requests: " + task.getException().getMessage());
            err.setStyle("-fx-text-fill: #dc2626;");
            requestList.getChildren().add(err);
        });
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private HBox card(DemandeDAO.Row r, String statut) {
        boolean budget = r.type().equals("BUDGET");

        Label type = new Label(budget ? "Budget" : "Material");
        type.getStyleClass().addAll("req-type", budget ? "req-type-budget" : "req-type-material");
        Label project = new Label(r.projet());
        project.getStyleClass().add("req-title");
        HBox head = new HBox(10, type, project);
        head.setAlignment(Pos.CENTER_LEFT);

        Label who = new Label("Requested by " + (r.chef() == null ? "-" : r.chef())
                + "  ·  " + r.date().format(DATE));
        who.getStyleClass().add("req-meta");

        Label amount = new Label(r.texte());
        amount.getStyleClass().add("req-amount");

        Label reason = new Label(r.motif().isEmpty() ? "No reason given." : "“" + r.motif() + "”");
        reason.getStyleClass().add("req-reason");
        reason.setWrapText(true);

        Label context = new Label(r.contexte());
        context.getStyleClass().add("req-context");

        Label error = new Label();
        error.setStyle("-fx-text-fill: #dc2626;");
        error.setWrapText(true);

        VBox info = new VBox(6, head, who, amount, reason, context, error);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox card = new HBox(16, info);
        card.getStyleClass().add("req-card");
        card.setAlignment(Pos.CENTER_LEFT);

        if (statut.equals("EN_ATTENTE")) {
            Button approve = new Button("Approve");
            approve.getStyleClass().add("btn-primary");
            Button reject = new Button("Reject");
            reject.getStyleClass().add("btn-danger");

            // two clicks to approve: the first one only asks for confirmation (3 seconds)
            approve.setOnAction(e -> {
                if (!approve.getText().equals("Confirm?")) {
                    approve.setText("Confirm?");
                    PauseTransition p = new PauseTransition(Duration.seconds(3));
                    p.setOnFinished(x -> approve.setText("Approve"));
                    p.play();
                    return;
                }
                decide(true, r.id(), error, approve, reject);
            });
            reject.setOnAction(e -> decide(false, r.id(), error, approve, reject));

            VBox buttons = new VBox(8, approve, reject);
            buttons.setAlignment(Pos.CENTER);
            card.getChildren().add(buttons);
        } else {
            Label state = new Label(statut.equals("APPROUVEE") ? "Approved" : "Rejected");
            state.getStyleClass().addAll("req-state", statut.equals("APPROUVEE") ? "req-ok" : "req-no");
            card.getChildren().add(state);
        }
        return card;
    }

    private void decide(boolean approve, int id, Label error, Button b1, Button b2) {
        error.setText("");
        b1.setDisable(true);
        b2.setDisable(true);
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                if (approve) dao.approuver(id); else dao.refuser(id);
                return null;
            }
        };
        task.setOnSucceeded(e -> load());                    // the request leaves the Pending list
        task.setOnFailed(e -> {
            b1.setDisable(false);
            b2.setDisable(false);
            error.setText(task.getException().getMessage());
        });
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
}