package it.lutechcdm.thingworxextensionplugin.definitions;

import java.util.ArrayList;
import java.util.List;

public class ServiceDefinition {

    private final String name;
    private final String description;
    private final String category;
    private final boolean isAllowOverride;
    private final List<String> aspects = new ArrayList<>();
    private ServiceResult serviceResult = null;
    private final List<ServiceParameter> serviceParameters = new ArrayList<>();

    public ServiceDefinition(String name, String description, String category, boolean isAllowOverride) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.isAllowOverride = isAllowOverride;
    }

    public void addAspect(String aspect) {
        aspects.add(aspect);
    }

    public void addServiceParameter(ServiceParameter parameter) {
        serviceParameters.add(parameter);
    }

    public void setServiceResult(ServiceResult serviceResult) {
        this.serviceResult = serviceResult;
    }

    public ServiceResult getServiceResult() {
        return serviceResult;
    }

    public List<ServiceParameter> getServiceParameters() {
        return serviceParameters;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("@ThingworxServiceDefinition(name = \"");
        sb.append(name);
        sb.append("\", ");

        sb.append("description=\"");
        sb.append(description != null ? description : "");
        sb.append("\", ");

        sb.append("category=\"");
        sb.append(category != null ? category : "");
        sb.append("\", ");

        sb.append("isAllowOverride=");
        sb.append(isAllowOverride);
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
