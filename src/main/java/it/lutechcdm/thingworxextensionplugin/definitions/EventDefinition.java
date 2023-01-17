package it.lutechcdm.thingworxextensionplugin.definitions;

import java.util.ArrayList;
import java.util.List;

public class EventDefinition {

    private final String name;
    private final String description;
    private final String category;

    private final String dataShape;

    private final boolean isLocalOnly;

    private final boolean isInvocable;

    private final boolean isPropertyEvent;

    private final List<String> aspects = new ArrayList<>();

    public EventDefinition(String name, String dataShape, String description, String category) {
        this.name = name;
        this.dataShape = dataShape;
        this.description = description;
        this.category = category;
        this.isLocalOnly = false;
        this.isInvocable = true;
        this.isPropertyEvent = false;
    }

    public void addAspect(String aspect) {
        aspects.add(aspect);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@ThingworxEventDefinition(name = \"");
        sb.append(name);
        sb.append("\", ");

        sb.append("description=\"");
        sb.append(description != null ? description : "");
        sb.append("\", ");

        sb.append("category=\"");
        sb.append(category != null ? category : "");
        sb.append("\", ");

        sb.append("dataShape=\"");
        sb.append(dataShape);
        sb.append("\", ");

        sb.append("isLocalOnly=");
        sb.append(isLocalOnly);
        sb.append(", ");

        sb.append("isInvocable=");
        sb.append(isInvocable);
        sb.append(", ");

        sb.append("isPropertyEvent=");
        sb.append(isPropertyEvent);
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
