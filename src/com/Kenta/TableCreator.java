package com.Kenta;

import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Collection;

public class TableCreator {
    private TableView<MarkerTableEntry> table;


    public TableCreator (){
        this.table = new TableView<>();
    }

    public void addItems(ObservableList<MarkerTableEntry> markers){
        table.setItems(markers);
    }

    public void addColumn(String title, String variableName){
        table.getColumns().add(TableCreator.createColumn(title, variableName));
    }

    public TableView getTable(){
        return table;
    }



    public static TableColumn<MarkerTableEntry, Object> createColumn(String title, String variableName){
        TableColumn<MarkerTableEntry, Object> nameCol = new TableColumn<>(title);
        nameCol.setCellValueFactory(new PropertyValueFactory<>(variableName));
        return nameCol;
    }

    @SafeVarargs
    public static TableView<MarkerTableEntry> createTable(ObservableList<MarkerTableEntry> markers,
                                                          TableColumn<MarkerTableEntry, Object>... tableColumns){
        TableView<MarkerTableEntry> table = new TableView<>(markers);
        table.getColumns().addAll(tableColumns);
        return table;
    }
}