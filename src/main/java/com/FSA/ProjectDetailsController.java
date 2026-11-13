package com.FSA;

import com.FSA.DAO.ChantierDAO;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ProjectDetailsController {

    @FXML private StackPane miniCardHolder;
    @FXML private Label chefName;
    @FXML private Label datesLabel;
    @FXML private Label locationLabel;
    @FXML private VBox membersList;
    @FXML private VBox materialsList;

    @FXML private Label budgetPill;
    @FXML private Label totalBudgetLabel;
    @FXML private Label spentLabel;
    @FXML private Label remainingLabel;
    @FXML private Label usedPercentLabel;
    @FXML private Label budgetNote;
    @FXML private StackPane budgetBar;
    @FXML private Region budgetFill;

    /** Fills the whole pop-up. `dates` is already formatted, e.g. "Jan - Mar 2026". */
    public void setProject(ChantierDAO.Card c, ChantierDAO.Details d, String dates, Node miniCard) {
        miniCardHolder.getChildren().setAll(miniCard);
        chefName.setText(c.chef() == null ? "No chef yet" : c.chef());
        datesLabel.setText(dates);
        locationLabel.setText(c.adresse() == null ? "" : c.adresse());

        membersList.getChildren().clear();
        for (String name : d.membres()) membersList.getChildren().add(memberRow(name));
        if (d.membres().isEmpty()) membersList.getChildren().add(emptyLabel("No worker assigned yet."));

        materialsList.getChildren().clear();
        for (ChantierDAO.Material m : d.materiaux()) materialsList.getChildren().add(materialRow(m));
        if (d.materiaux().isEmpty()) materialsList.getChildren().add(emptyLabel("No material assigned yet."));

        double budget = d.budget().doubleValue();
        double spent = d.depenses().doubleValue();
        double used = budget <= 0 ? 0 : spent / budget;

        totalBudgetLabel.setText(UiKit.money(budget));
        spentLabel.setText(UiKit.money(spent));
        remainingLabel.setText(UiKit.money(budget - spent));
        usedPercentLabel.setText(Math.round(used * 100) + "%");
        budgetNote.setText(UiKit.money(spent) + " of " + UiKit.money(budget));
        UiKit.setRatio(budgetBar, budgetFill, used);
        UiKit.updatePill(budgetPill, used);
    }

    private HBox memberRow(String name) {
        StackPane avatar = new StackPane(UiKit.icon("avatar-sm-icon"));
        avatar.getStyleClass().add("avatar-sm");

        Label label = new Label(name);
        label.getStyleClass().add("row-name");

        HBox row = new HBox(avatar, label);
        row.getStyleClass().addAll("data-row", "row-line");
        return row;
    }

    private VBox materialRow(ChantierDAO.Material m) {
        Label name = new Label(m.nom());
        name.getStyleClass().add("material-name");

        Label amount = new Label(UiKit.number(m.utilisee()) + "/" + UiKit.number(m.prevue()) + " " + m.unite());
        amount.getStyleClass().add("muted");

        HBox top = new HBox(name, UiKit.grow(), amount);
        top.setAlignment(Pos.CENTER_LEFT);

        double ratio = m.prevue() <= 0 ? 0 : m.utilisee() / m.prevue();
        VBox row = new VBox(top, UiKit.bar(ratio, UiKit.materialColor(m.nom())));
        row.getStyleClass().add("material-row");
        return row;
    }

    private Label emptyLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        return l;
    }
}