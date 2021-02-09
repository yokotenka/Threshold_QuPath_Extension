package com.Kenta;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.controlsfx.control.action.Action;
import qupath.lib.gui.ActionTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.tools.MenuTools;

public class Testing_Extension implements QuPathExtension {
    @Override
    public void installExtension(QuPathGUI quPathGUI) {
        Action testing_the_gui_yay = ActionTools.createAction(new GUI_Test(), "Testing the gui yay");

        MenuTools.addMenuItems(
                quPathGUI.getMenu("Extensions>Kenta>Nice", true),
                testing_the_gui_yay);

    }

    @Override
    public String getName() {
        return "Test Extension by Kenta";
    }

    @Override
    public String getDescription() {
        return "This is a test to see how things work";
    }
}
