package com.Kenta;

import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Collection;

public class TableCreator {

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