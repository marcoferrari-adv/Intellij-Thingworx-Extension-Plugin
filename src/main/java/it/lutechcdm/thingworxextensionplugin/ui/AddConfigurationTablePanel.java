package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class AddConfigurationTablePanel {
    JPanel mainPanel;
    JTextField nameField;
    JTabbedPane tabbedPane1;
    JTextField descriptionField;
    JTabbedPane tabbedPane2;
    JTable fieldDefinitionsTable;
    private DefaultTableModel parameterTableModel;
    JButton addButton;
    JButton deleteButton;
    JTextField ordinalField;
    JCheckBox isMultiRowTable;

    private void createUIComponents() {
        parameterTableModel = new DefaultTableModel(0, 0);
        parameterTableModel.setColumnIdentifiers(new String[]{"Ordinal", "Name", "Base Type"});

        fieldDefinitionsTable = new JBTable(parameterTableModel);
        fieldDefinitionsTable.setTableHeader(new JTableHeader(fieldDefinitionsTable.getColumnModel()));
        fieldDefinitionsTable.setShowGrid(true);
        fieldDefinitionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}
