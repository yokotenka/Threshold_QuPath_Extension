package com.Kenta;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI_Test implements Runnable {


    public Stage createDialog(){
        Button bt = new Button("HELLO");



        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("Nice");
        list.add("Very Nice");

        ObservableList<TableRow> rows = FXCollections.observableArrayList();
        rows.add(new TableRow("John", true, new ComboBox<>(list)));
        rows.add(new TableRow("Steve", false, new ComboBox<>(list)));

        TableColumn<TableRow, Object> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<TableRow, Object> selectCol = new TableColumn<>("Select");
        selectCol.setCellValueFactory(new PropertyValueFactory<>("checkBox"));

        TableColumn<TableRow, Object> comboBoxCol = new TableColumn<>("Combo");
        comboBoxCol.setCellValueFactory(new PropertyValueFactory<>("comboBox"));

        TableView<TableRow> table = new TableView<TableRow>();
        table.setItems(rows);
        table.getColumns().addAll(nameCol, selectCol, comboBoxCol);

        GridPane gp = new GridPane();
        gp.add(table , 0, 0);


        Stage dialog = new Stage();
        Scene scene = new Scene(gp);
        dialog.setScene(scene);
        return dialog;
    }

    @Override
    public void run(){
        Stage stage = createDialog();
        stage.show();
    }
}
