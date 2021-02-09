package com.Kenta;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

public class TableRow {
    private CheckBox checkBox;
    private String name;
    private ComboBox comboBox;

    public TableRow(String name, boolean isSelected, ComboBox comboBox){
        this.comboBox = comboBox;
        this.checkBox = new CheckBox();
        checkBox.setSelected(isSelected);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public ComboBox getComboBox() {
        return comboBox;
    }


}
