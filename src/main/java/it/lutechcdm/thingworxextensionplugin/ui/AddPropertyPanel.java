package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.ui.ComboBox;
import it.lutechcdm.thingworxextensionplugin.definitions.ThingworxBaseTypes;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;

import javax.swing.*;

public class AddPropertyPanel {

    JPanel mainPanel;
    JTextField nameField;
    JTextField categoryField;
    JComboBox<String> baseTypeField;
    JTextField descriptionField;
    JCheckBox persistent;
    JCheckBox readonly;
    JCheckBox logged;
    JTextField defaultValue;
    JCheckBox isDefault;
    JLabel defaultValueLabel;
    JLabel unitLabel;
    JLabel minValueLabel;
    JLabel maxValueLabel;
    JTextField dataShape;
    JTextField unit;
    JTextField minValue;
    JTextField maxValue;
    JComboBox<String> dataChangeType;
    JLabel dataShapeLabel;
    JLabel infotableTypeLabel;
    JComboBox<String> infotableType;

    public AddPropertyPanel() {

        isDefault.addItemListener(e -> {

            String selectedItem = (String) baseTypeField.getSelectedItem();
            ThingworxBaseTypes selectedTwxType = ThingworxBaseTypes.valueOf(selectedItem);
            if(ThingworxBaseTypes.isNumericType(selectedTwxType)) {
                setNumericFiledVisible(true);
                setInfoTableFieldVisible(false);
                setDefaultFiledVisible(isDefault.isSelected());
            }
            else if(ThingworxBaseTypes.INFOTABLE == selectedTwxType) {
                setInfoTableFieldVisible(true);
                setNumericFiledVisible(false);
                setDefaultFiledVisible(false);
            }
            else {
                setInfoTableFieldVisible(false);
                setNumericFiledVisible(false);
                setDefaultFiledVisible(isDefault.isSelected());
            }
        });

        baseTypeField.addItemListener(evt -> {

            String selectedItem = (String) baseTypeField.getSelectedItem();
            ThingworxBaseTypes selectedTwxType = ThingworxBaseTypes.valueOf(selectedItem);
            if(ThingworxBaseTypes.INFOTABLE == selectedTwxType) {
                setDefaultFiledVisible(false);
                setNumericFiledVisible(false);
                setInfoTableFieldVisible(true);
                isDefault.setVisible(false);
                isDefault.setSelected(false);
            }
            else if(ThingworxBaseTypes.isNumericType(selectedTwxType)) {
                isDefault.setSelected(false);
                isDefault.setVisible(true);
                setNumericFiledVisible(true);
                setDefaultFiledVisible(false);
                setInfoTableFieldVisible(false);
            }
            else {
                isDefault.setSelected(false);
                isDefault.setVisible(true);
                setDefaultFiledVisible(false);
                setInfoTableFieldVisible(false);
                setNumericFiledVisible(false);
            }
        });
    }

    private void setNumericFiledVisible(boolean visible) {
        minValueLabel.setVisible(visible);
        maxValueLabel.setVisible(visible);
        unitLabel.setVisible(visible);

        minValue.setVisible(visible);
        minValue.setText("");
        maxValue.setVisible(visible);
        maxValue.setText("");
        unit.setVisible(visible);
        unit.setText("");
    }

    private void setInfoTableFieldVisible(boolean visible) {
        dataShapeLabel.setVisible(visible);
        dataShape.setVisible(visible);
        dataShape.setText("");
        infotableTypeLabel.setVisible(visible);
        infotableType.setVisible(visible);
        infotableType.setSelectedItem(ThingworxConstants.JUST_INFOTABLE);
    }

    private void setDefaultFiledVisible(boolean visible) {
        defaultValueLabel.setVisible(visible);
        defaultValue.setVisible(visible);
        defaultValue.setText("");
    }

    private void createUIComponents() {
        dataChangeType = new ComboBox<>(new String[]{"ALWAYS", "NEVER", "ON", "OFF", "VALUE", "DEADBAND"});
        dataChangeType.setSelectedItem("VALUE");

        baseTypeField = new ComboBox<>(ThingworxBaseTypes.getPropertyDefinitionBaseTypeList());
        baseTypeField.setSelectedItem(ThingworxBaseTypes.STRING.name());

        infotableType = new ComboBox<>(new String[]{ThingworxConstants.JUST_INFOTABLE, ThingworxConstants.DATA_TABLE_INFOTABLE, ThingworxConstants.STREAM_ENTRY_INFOTABLE, ThingworxConstants.CONTENT_CRAWLER_INFOTABLE});
        infotableType.setSelectedItem(ThingworxConstants.JUST_INFOTABLE);
    }
}
