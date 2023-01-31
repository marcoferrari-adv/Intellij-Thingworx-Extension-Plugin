package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.table.JBTable;
import it.lutechcdm.thingworxextensionplugin.definitions.ThingworxBaseTypes;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;
import it.lutechcdm.thingworxextensionplugin.definitions.ServiceParameter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.util.ArrayList;

public class AddServicePanel {
    JPanel mainPanel;
    JTextField nameField;
    JTextField descriptionField;
    JTextField categoryField;
    JCheckBox allowOverride;
    JCheckBox async;
    JTable parameterTable;
    private DefaultTableModel parameterTableModel;
    JButton addButton;
    JButton deleteButton;
    JTabbedPane tabbedPane1;
    JTextField outputNameField;
    JTextField outputDescriptionField;
    JComboBox<String> outputBaseType;
    JLabel dataShapeLabel;
    JTextField dataShape;
    JComboBox<String> infoTableType;


    final ArrayList<ServiceParameter> serviceParameters = new ArrayList<>();

    public AddServicePanel(Project project) {

        deleteButton.addActionListener(e -> {
            int selectedRow = parameterTable.getSelectedRow();
            if(selectedRow > -1) {
                parameterTableModel.removeRow(selectedRow);
                serviceParameters.remove(selectedRow);
            }
        });

        addButton.addActionListener(e -> {

            AddServiceParameterDialogWrapper dialog = new AddServiceParameterDialogWrapper(project);
            dialog.setModal(true);
            dialog.setSize(400, 300);
            if(dialog.showAndGet()) {
                ServiceParameter serviceParameterDefinition = dialog.createdDefinition;
                if(serviceParameterDefinition != null) {
                    for(ServiceParameter p : serviceParameters) {
                        if(p.getName().equals(serviceParameterDefinition.getName())) {
                            Messages.showErrorDialog(project, "Unable to add parameter" + p.getName() + "name already used", "Parameter Error");
                            return;
                        }
                    }

                    serviceParameters.add(serviceParameterDefinition);
                    parameterTableModel.addRow(new Object[]{serviceParameterDefinition.getName(), serviceParameterDefinition.getBaseType().name()});
                }
            }
        });

        outputBaseType.addItemListener(evt -> {
            String selectedItem = (String) outputBaseType.getSelectedItem();
            ThingworxBaseTypes selectedTwxType = ThingworxBaseTypes.valueOf(selectedItem);
            setInfoTableFieldVisible(ThingworxBaseTypes.INFOTABLE == selectedTwxType);
        });

    }

    private void setInfoTableFieldVisible(boolean visible) {
        dataShapeLabel.setVisible(visible);
        dataShape.setVisible(visible);
        dataShape.setText("");
        infoTableType.setVisible(visible);
        infoTableType.setSelectedItem(ThingworxConstants.JUST_INFOTABLE);
    }

    private void createUIComponents() {
        outputBaseType = new ComboBox<>(ThingworxBaseTypes.getPropertyDefinitionBaseTypeList());
        outputBaseType.addItem(ThingworxBaseTypes.NOTHING.name());
        outputBaseType.setSelectedItem(ThingworxBaseTypes.NOTHING.name());

        parameterTableModel = new DefaultTableModel(0, 0);
        parameterTableModel.setColumnIdentifiers(new String[]{"Name", "Base Type"});
        parameterTable = new JBTable(parameterTableModel);
        parameterTable.setTableHeader(new JTableHeader(parameterTable.getColumnModel()));
        parameterTable.setShowGrid(true);
        parameterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        infoTableType = new ComboBox<>(new String[]{ThingworxConstants.JUST_INFOTABLE, ThingworxConstants.DATA_TABLE_INFOTABLE, ThingworxConstants.STREAM_ENTRY_INFOTABLE, ThingworxConstants.CONTENT_CRAWLER_INFOTABLE});
        infoTableType.setSelectedItem(ThingworxConstants.JUST_INFOTABLE);
    }
}
