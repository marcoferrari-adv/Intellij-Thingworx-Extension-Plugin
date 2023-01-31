package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.table.JBTable;
import it.lutechcdm.thingworxextensionplugin.definitions.ConfigurationTableFieldDefinition;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.util.ArrayList;

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

    private final int startOrdinal;

    final ArrayList<ConfigurationTableFieldDefinition> configurationTableParameters = new ArrayList<>();

    public AddConfigurationTablePanel(Project project, int startOrdinal) {
        this.startOrdinal = startOrdinal;

        deleteButton.addActionListener(actionEvent -> {
            int selectedIndex = fieldDefinitionsTable.getSelectedRow();
            if(selectedIndex > 0 && selectedIndex < configurationTableParameters.size()) {
                configurationTableParameters.remove(selectedIndex);
            }
        });

        addButton.addActionListener(actionEvent -> {
            AddConfigurationTableParameterDialogWrapper dialog = new AddConfigurationTableParameterDialogWrapper(project, configurationTableParameters.size(), isMultiRowTable.isVisible());
            dialog.setModal(true);
            dialog.setSize(400, 300);
            if(dialog.showAndGet()) {
                ConfigurationTableFieldDefinition fieldDefinition = dialog.createdDefinition;
                for(ConfigurationTableFieldDefinition p : configurationTableParameters) {
                    if(p.getName().equals(fieldDefinition.getName())) {
                        Messages.showErrorDialog(project, "Unable to add parameter" + p.getName() + "name already used", "Parameter Error");
                        return;
                    }
                }

                configurationTableParameters.add(fieldDefinition);
                parameterTableModel.addRow(new Object[]{"" + fieldDefinition.getOrdinal(), fieldDefinition.getName(), fieldDefinition.getBaseType().name()});
            }
        });
    }

    private void createUIComponents() {
        parameterTableModel = new DefaultTableModel(0, 0);
        parameterTableModel.setColumnIdentifiers(new String[]{"Ordinal", "Name", "Base Type"});

        fieldDefinitionsTable = new JBTable(parameterTableModel);
        fieldDefinitionsTable.setTableHeader(new JTableHeader(fieldDefinitionsTable.getColumnModel()));
        fieldDefinitionsTable.setShowGrid(true);
        fieldDefinitionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ordinalField = new JTextField();
        ordinalField.setText("" + startOrdinal);
    }
}
