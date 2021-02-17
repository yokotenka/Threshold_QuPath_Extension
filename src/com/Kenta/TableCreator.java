package com.Kenta;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Class which helps create a javafx TableView class
 */
public class TableCreator {
    // The TableView instance
    private TableView<MarkerTableEntry> table;

    /**
     * Constructor
     */
    public TableCreator (){
        this.table = new TableView<>();
    }

    /**
     * Set items to the TableView
     * @param markers
     */
    public void setItems(ObservableList<MarkerTableEntry> markers){
        table.setItems(markers);
    }

    /**
     * Adding columns to the table
     * @param title Title of the column
     * @param variableName variable name in MarkerTableEntry
     */
    public void addColumn(String title, String variableName){
        table.getColumns().add(TableCreator.createColumn(title, variableName));
    }

    /**
     * Getter for table
     * @return table
     */
    public TableView getTable(){
        return table;
    }

    /**
     * Static creator for a table column
     * @param title Title of the column
     * @param variableName Name of the variable in MarkerTableEntry
     * @return Instance of TableColumn
     */
    public static TableColumn<MarkerTableEntry, Object> createColumn(String title, String variableName){
        TableColumn<MarkerTableEntry, Object> nameCol = new TableColumn<>(title);
        nameCol.setCellValueFactory(new PropertyValueFactory<>(variableName));
        return nameCol;
    }

    /**
     * Static creator for a TableView
     * @param markers Items to be set
     * @param tableColumns TableColumns
     * @return Instance of TableView
     */
    @SafeVarargs
    public static TableView<MarkerTableEntry> createTable(ObservableList<MarkerTableEntry> markers,
                                                          TableColumn<MarkerTableEntry, Object>... tableColumns){
        TableView<MarkerTableEntry> table = new TableView<>(markers);
        table.getColumns().addAll(tableColumns);
        return table;
    }
}