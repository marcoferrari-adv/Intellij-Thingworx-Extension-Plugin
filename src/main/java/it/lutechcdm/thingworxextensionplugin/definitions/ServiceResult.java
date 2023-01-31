package it.lutechcdm.thingworxextensionplugin.definitions;

import java.util.ArrayList;
import java.util.List;

public class ServiceResult {

    private final String name;
    private final String description;
    private final ThingworxBaseTypes baseType;
    private final List<String> aspects = new ArrayList<>();

    public ServiceResult(String name, String description, ThingworxBaseTypes baseTypes) {
        this.name = name;
        this.description = description;
        this.baseType = baseTypes;
    }

    public void addAspect(String aspect) {
        aspects.add(aspect);
    }

    public ThingworxBaseTypes getBaseType() {
        return baseType == null ? ThingworxBaseTypes.NOTHING : baseType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@ThingworxServiceResult(name = \"");
        sb.append(name);
        sb.append("\", ");

        sb.append("description=\"");
        sb.append(description != null ? description : "");
        sb.append("\", ");

        sb.append("baseType=\"");
        sb.append(baseType.name());
        sb.append("\", ");

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
