package it.lutechcdm.thingworxextensionplugin.ui;

import com.intellij.openapi.ui.ComboBox;
import it.lutechcdm.thingworxextensionplugin.definitions.ThingworxBaseTypes;
import it.lutechcdm.thingworxextensionplugin.ThingworxConstants;

import javax.swing.*;

public class AddServiceParameterPanel {
    JPanel mainPanel;
    JCheckBox required;
    JTextField nameField;
    JTextField descriptionField;
    JComboBox<String> baseType;
    JTextField unit;
    JTextField minValue;
    JTextField maxValue;
    JTextField dataShape;
    JComboBox<String> infoTableType;
    JTextField defaultValue;
    JCheckBox isDefault;
    JLabel defaultValueLabel;
    JLabel infoTableTypeLabel;
    JLabel dataShapeLabel;
    JLabel unitLabel;
    JLabel maxValueLabel;
    JLabel minValueLabel;

    public AddServiceParameterPanel() {

        isDefault.addItemListener(e -> {

            String selectedItem = (String) baseType.getSelectedItem();
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

        baseType.addItemListener(evt -> {

            String selectedItem = (String) baseType.getSelectedItem();
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
        infoTableTypeLabel.setVisible(visible);
        infoTableType.setVisible(visible);
        infoTableType.setSelectedItem(ThingworxConstants.JUST_INFOTABLE);
    }

    private void setDefaultFiledVisible(boolean visible) {
        defaultValueLabel.setVisible(visible);
        defaultValue.setVisible(visible);
        defaultValue.setText("");
    }

    private void createUIComponents() {
        baseType = new ComboBox<>(ThingworxBaseTypes.getPropertyDefinitionBaseTypeList());
        baseType.setSelectedItem(ThingworxBaseTypes.STRING.name());

        infoTableType = new ComboBox<>(new String[]{ThingworxConstants.JUST_INFOTABLE, ThingworxConstants.DATA_TABLE_INFOTABLE, ThingworxConstants.STREAM_ENTRY_INFOTABLE, ThingworxConstants.CONTENT_CRAWLER_INFOTABLE});
        infoTableType.setSelectedItem(ThingworxConstants.JUST_INFOTABLE);
    }
}
