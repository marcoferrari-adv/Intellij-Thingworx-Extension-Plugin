package it.lutechcdm.thingworxextensionplugin.definitions;

import java.util.ArrayList;
import java.util.List;

public class PropertyDefinition {

    private final String name;
    private final String description;
    private final String category;
    private final ThingworxBaseTypes baseType;
    private final boolean isLocalOnly;
    private final List<String> aspects = new ArrayList<>();

    public PropertyDefinition(String name, ThingworxBaseTypes baseTypes, String description, String category, boolean isLocalOnly) {
        this.name = name;
        this.baseType = baseTypes;
        this.description = description;
        this.category = category;
        this.isLocalOnly = isLocalOnly;
    }

    public void addAspect(String aspect) {
        aspects.add(aspect);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@ThingworxPropertyDefinition(name = \"");
        sb.append(name);
        sb.append("\", ");

        sb.append("description=\"");
        sb.append(description != null ? description : "");
        sb.append("\", ");

        sb.append("category=\"");
        sb.append(category != null ? category : "");
        sb.append("\", ");

        sb.append("baseType=\"");
        sb.append(baseType.name());
        sb.append("\", ");

        sb.append("isLocalOnly=");
        sb.append(isLocalOnly);
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
