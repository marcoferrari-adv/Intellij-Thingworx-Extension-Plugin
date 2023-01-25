package it.lutechcdm.thingworxextensionplugin.definitions;

import it.lutechcdm.thingworxextensionplugin.ThingworxBaseTypes;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationTableFieldDefinition {

    private final String name;
    private final String description;
    private final ThingworxBaseTypes baseType;
    private final int ordinal;
    private final List<String> aspects = new ArrayList<>();

    public ConfigurationTableFieldDefinition(String name, String description, ThingworxBaseTypes baseType, int ordinal) {
        this.name = name;
        this.description = description;
        this.baseType = baseType;
        this.ordinal = ordinal;
    }

    public void addAspect(String aspect) {
        aspects.add(aspect);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@ThingworxFieldDefinition(name = \"");
        sb.append(name);
        sb.append("\", ");

        sb.append("description=\"");
        sb.append(description != null ? description : "");
        sb.append("\", ");

        sb.append("baseType=\"");
        sb.append(baseType.name());
        sb.append("\", ");

        sb.append("ordinal=");
        sb.append(ordinal);
        sb.append(", ");

        sb.append("aspects = {");
        for(int i = 0; i < aspects.size(); i++) {
            sb.append("\"");
            sb.append(aspects.get(i));
            if(i + 1 == aspects.size())
                sb.append("\"");
            else
                sb.append("\", ");
        }
        sb.append("})");
        return sb.toString();
    }
}
