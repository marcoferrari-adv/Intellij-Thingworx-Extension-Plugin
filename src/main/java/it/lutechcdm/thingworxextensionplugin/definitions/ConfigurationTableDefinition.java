package it.lutechcdm.thingworxextensionplugin.definitions;

import java.util.ArrayList;

public class ConfigurationTableDefinition {

    private final String name;
    private final String description;
    private final boolean isMultiRow;
    private final int ordinal;

    private final ArrayList<ConfigurationTableFieldDefinition> fields = new ArrayList<>();

    public ConfigurationTableDefinition(String name, String description, boolean isMultiRow, int ordinal) {
        this.name = name;
        this.description = description;
        this.isMultiRow = isMultiRow;
        this.ordinal = ordinal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@ThingworxConfigurationTableDefinition(name = \"");
        sb.append(name);
        sb.append("\", ");

        sb.append("description=\"");
        sb.append(description != null ? description : "");
        sb.append("\", ");

        sb.append("isMultiRow=");
        sb.append(isMultiRow);
        sb.append(", ");

        sb.append("ordinal=");
        sb.append(ordinal);
        sb.append(", ");

        sb.append("dataShape = @ThingworxDataShapeDefinition(fields ={");
        for(int i = 0; i < fields.size(); i++) {
            sb.append("");
            sb.append(fields.get(i));
            if(i + 1 == fields.size())
                sb.append("");
            else
                sb.append(", ");
        }
        sb.append("})");
        return sb.toString();
    }
}
