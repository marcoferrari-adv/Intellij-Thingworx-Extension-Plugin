package it.lutechcdm.thingworxextensionplugin.config;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import it.lutechcdm.thingworxextensionplugin.exception.InvalidMetadataFormatException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.Objects;

public class MetadataConfigFile {

    private static final String WIDGETS_NODE_NAME = "Widgets";
    private static final String WIDGET_NODE_NAME = "Widget";

    private static final String JAR_RESOURCES_NODE_NAME = "JarResources";
    private static final String FILE_RESOURCE_NODE_NAME = "FileResource";

    private static final String THING_PACKAGES_NODE_NAME = "ThingPackages";
    private static final String THING_PACKAGE_NODE_NAME = "ThingPackage";

    private static final String THING_TEMPLATES_NODE_NAME = "ThingTemplates";
    private static final String THING_TEMPLATE_NODE_NAME = "ThingTemplate";

    private static final String THING_SHAPES_NODE_NAME = "ThingShapes";
    private static final String THING_SHAPE_NODE_NAME = "ThingShape";

    private static final String AUTHENTICATORS_NODE_NAME = "Authenticators";
    private static final String AUTHENTICATOR_NODE_NAME = "Authenticator";

    private static final String DIRECTORY_SERVICES_NODE_NAME = "DirectoryServices";
    private static final String DIRECTORY_SERVICE_NODE_NAME = "DirectoryService";

    private static final String SCRIPT_FUNCTION_LIBRARIES_NODE_NAME = "ScriptFunctionLibraries";
    private static final String SCRIPT_FUNCTION_LIBRARY_NODE_NAME = "ScriptFunctionLibrary";

    private static final String UI_RESOURCES_NODE_NAME = "UIResources";

    private static final String CLASSNAME_ATTRIBUTE = "className";

    private static final String NAME_ATTRIBUTE = "name";

    private static final String FILE_ATTRIBUTE = "file";

    private static final String DESCRIPTION_ATTRIBUTE = "file";

    private static final String EDITABLE_EXTENSION_ATTRIBUTE = "aspect.isEditableExtensionObject";

    public static final String METADATA_FILE_NAME = "metadata.xml";

    private final XmlFile file;
    private final XmlTag rootTag;

    public MetadataConfigFile(XmlFile file) {
        Objects.requireNonNull(file, "Metadata xml file is null");
        this.file = file;
        this.rootTag = getMetadataRoot();

        if(rootTag == null)
            throw new InvalidMetadataFormatException("Metadata file doesn't contains a valid root node, check the file format");
    }

    @Nullable
    public XmlTag getMetadataRoot() {
        XmlDocument document = file.getDocument();
        if(document == null)
            return null;

        if(rootTag != null)
            return rootTag;

        return document.getRootTag();
    }

    public void removeNodeIfEmpty(XmlTag tag) {
        if(tag != null && tag.getSubTags().length == 0)
            tag.delete();
    }

    private XmlTag getOrCreateNode(XmlTag parentNode, String node) {
        XmlTag thingPackages = parentNode.findFirstSubTag(node);
        if(thingPackages == null) {
            thingPackages = parentNode.createChildTag(node, null, null, false);
            thingPackages = parentNode.addSubTag(thingPackages, false);
        }
        return thingPackages;
    }

    public void addWidget(String name, String description, VirtualFile...fileResources) {
        XmlTag widgets = getOrCreateNode(rootTag, WIDGETS_NODE_NAME);

        XmlTag widget = widgets.createChildTag(WIDGET_NODE_NAME, null, null, false);
        widget.setAttribute(NAME_ATTRIBUTE, name);
        widget.setAttribute(DESCRIPTION_ATTRIBUTE, description);
        widget = widgets.addSubTag(widget, false);

        XmlTag uiResources = widget.createChildTag(UI_RESOURCES_NODE_NAME, null, null, false);
        uiResources = widget.addSubTag(uiResources, false);

        for(VirtualFile fileResource : fileResources) {
            XmlTag fileResourceTag = uiResources.createChildTag(FILE_RESOURCE_NODE_NAME, null, null, false);
            fileResourceTag.setAttribute(FILE_ATTRIBUTE, fileResource.getName());
            fileResourceTag.setAttribute("isDevelopment", "" + !fileResource.getName().contains("runtime"));
            fileResourceTag.setAttribute("isRuntime", "" + fileResource.getName().contains("runtime"));
            fileResourceTag.setAttribute("type", fileResource.getExtension() != null ? fileResource.getExtension().toUpperCase() : "");
            uiResources.addSubTag(fileResourceTag, false);
        }
    }

    public void removeWidget(String widgetName) {
        deleteThingworxParentedNodeByTagValue(rootTag, NAME_ATTRIBUTE, widgetName, WIDGETS_NODE_NAME, WIDGET_NODE_NAME);
    }

    public void addJarResource(String jarPath) {
        XmlTag extensionPackages = rootTag.findFirstSubTag("ExtensionPackages");
        if(extensionPackages == null)
            throw new InvalidMetadataFormatException("Metadata file doesn't contains ExtensionPackages node, check the file format");

        XmlTag extensionPackage = extensionPackages.findFirstSubTag("ExtensionPackage");
        if(extensionPackage == null)
            throw new InvalidMetadataFormatException("Metadata file doesn't contains ExtensionPackage node, check the file format");

        XmlTag jarResources =  extensionPackage.findFirstSubTag(JAR_RESOURCES_NODE_NAME);
        if(jarResources == null) {
            jarResources = extensionPackage.createChildTag(JAR_RESOURCES_NODE_NAME, null, null, false);
            jarResources = extensionPackage.addSubTag(jarResources, false);
        }

        XmlTag fileResource = jarResources.createChildTag(FILE_RESOURCE_NODE_NAME, null, null, false);
        fileResource.setAttribute("type", "JAR");
        fileResource.setAttribute(FILE_ATTRIBUTE, new File(jarPath).getName());
        jarResources.addSubTag(fileResource, false);
    }

    public void removeJarResource(String jarName) {
        XmlTag extensionPackages = rootTag.findFirstSubTag("ExtensionPackages");
        if(extensionPackages == null)
            throw new InvalidMetadataFormatException("Metadata file doesn't contains ExtensionPackages node, check the file format");

        XmlTag extensionPackage = extensionPackages.findFirstSubTag("ExtensionPackage");
        if(extensionPackage == null)
            throw new InvalidMetadataFormatException("Metadata file doesn't contains ExtensionPackage node, check the file format");

        deleteThingworxParentedNodeByTagValue(extensionPackage, FILE_ATTRIBUTE, jarName, JAR_RESOURCES_NODE_NAME, FILE_RESOURCE_NODE_NAME);
    }

    public void addThingTemplate(String className) {
        XmlTag thingPackages = getOrCreateNode(rootTag, THING_PACKAGES_NODE_NAME);

        XmlTag thingPackage = thingPackages.createChildTag(THING_PACKAGE_NODE_NAME, null, null, false);

        String name =  className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
        thingPackage.setAttribute(CLASSNAME_ATTRIBUTE, className);
        thingPackage.setAttribute(DESCRIPTION_ATTRIBUTE, "");
        thingPackage.setAttribute(NAME_ATTRIBUTE, name + "Package");
        thingPackages.addSubTag(thingPackage, false);

        XmlTag thingTemplates = getOrCreateNode(rootTag, THING_TEMPLATES_NODE_NAME);

        XmlTag thingTemplate =  thingTemplates.createChildTag(THING_TEMPLATE_NODE_NAME, null, null, false);
        thingTemplate.setAttribute(EDITABLE_EXTENSION_ATTRIBUTE, "false");
        thingTemplate.setAttribute(DESCRIPTION_ATTRIBUTE, "");
        thingTemplate.setAttribute(NAME_ATTRIBUTE, name);
        thingTemplate.setAttribute("thingPackage", name + "Package");
        thingTemplates.addSubTag(thingTemplate, false);
    }

    public boolean removeThingTemplate(String className) {
        deleteThingworxParentedNodeByTagValue(rootTag, CLASSNAME_ATTRIBUTE, className, THING_PACKAGES_NODE_NAME, THING_PACKAGE_NODE_NAME);
        String thingName = className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
        return deleteThingworxParentedNodeByTagValue(rootTag, NAME_ATTRIBUTE, thingName, THING_TEMPLATES_NODE_NAME, THING_TEMPLATE_NODE_NAME);
    }

    public void addAuthenticator(String className) {
        Map<String, String> attributes = Map.of("enabled", "false", "priority", "1", "requiresChallenge", "false", "supportsSession", "false");
        createThingworxParentedNode(AUTHENTICATORS_NODE_NAME, AUTHENTICATOR_NODE_NAME, className, true, attributes);
    }

    public boolean removeAuthenticator(String className) {
        return deleteThingworxParentedNodeByTagValue(rootTag, CLASSNAME_ATTRIBUTE, className, AUTHENTICATORS_NODE_NAME, AUTHENTICATOR_NODE_NAME);
    }

    public void addThingShape(String className) {
        createThingworxParentedNode(THING_SHAPES_NODE_NAME, THING_SHAPE_NODE_NAME, className, false, Map.of());
    }

    public boolean removeThingShape(String className) {
        return deleteThingworxParentedNodeByTagValue(rootTag, CLASSNAME_ATTRIBUTE, className, THING_SHAPES_NODE_NAME, THING_SHAPE_NODE_NAME);
    }

    public void addDirectoryService(String className) {
        createThingworxParentedNode(DIRECTORY_SERVICES_NODE_NAME, DIRECTORY_SERVICE_NODE_NAME, className, true, Map.of());
    }

    public boolean removeDirectoryService(String className) {
        return deleteThingworxParentedNodeByTagValue(rootTag, CLASSNAME_ATTRIBUTE, className, DIRECTORY_SERVICES_NODE_NAME, DIRECTORY_SERVICE_NODE_NAME);
    }

    public void addScriptFunctionLibrary(String className) {
        createThingworxParentedNode(SCRIPT_FUNCTION_LIBRARIES_NODE_NAME, SCRIPT_FUNCTION_LIBRARY_NODE_NAME, className, false, Map.of());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeScriptFunctionLibrary(String className) {
        return deleteThingworxParentedNodeByTagValue(rootTag, CLASSNAME_ATTRIBUTE, className, SCRIPT_FUNCTION_LIBRARIES_NODE_NAME, SCRIPT_FUNCTION_LIBRARY_NODE_NAME);
    }

    private void createThingworxParentedNode(String collectorName, String nodeName, String className, boolean editable, Map<String, String> additionalAttributes) {
        XmlTag collectorNode = getOrCreateNode(rootTag, collectorName);
        XmlTag newNode = collectorNode.createChildTag(nodeName, null, null, false);
        String name =  className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
        newNode.setAttribute(CLASSNAME_ATTRIBUTE, className);
        newNode.setAttribute(DESCRIPTION_ATTRIBUTE, "");
        newNode.setAttribute(NAME_ATTRIBUTE, name);
        newNode.setAttribute(EDITABLE_EXTENSION_ATTRIBUTE, "" + editable);

        if(additionalAttributes != null) {
            for (Map.Entry<String, String> additionalEntry : additionalAttributes.entrySet()) {
                newNode.setAttribute(additionalEntry.getKey(), additionalEntry.getValue());
            }
        }

        collectorNode.addSubTag(newNode, false);
    }


    private boolean deleteThingworxParentedNodeByTagValue(XmlTag startNode, String attributeName, String attributeValue, String collectorNodeName, String objectNodeName) {
        boolean childRemoved = false;
        XmlTag collector = startNode.findFirstSubTag(collectorNodeName);
        if(collector != null) {
            XmlTag[] subTags = collector.findSubTags(objectNodeName);
            for(XmlTag subTag : subTags) {
                if(attributeValue.equalsIgnoreCase(subTag.getAttributeValue(attributeName))) {
                    subTag.delete();
                    childRemoved = true;
                }
            }

            if(childRemoved) {
                removeNodeIfEmpty(collector);
            }
        }
        return childRemoved;
    }

    private boolean existsThingworxParentedNodeByTagValue(XmlTag startNode, String attributeName, String attributeValue, String collectorNodeName, String objectNodeName) {
        XmlTag collector = startNode.findFirstSubTag(collectorNodeName);
        if(collector != null) {
            XmlTag[] subTags = collector.findSubTags(objectNodeName);
            for(XmlTag subTag : subTags) {
                if(attributeValue.equalsIgnoreCase(subTag.getAttributeValue(attributeName))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isThingTemplate(String className) {
        String thingName = className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
        return existsThingworxParentedNodeByTagValue(rootTag, NAME_ATTRIBUTE, thingName, THING_TEMPLATES_NODE_NAME, THING_TEMPLATE_NODE_NAME);
    }

    public boolean isThingworxWidget(String name) {
        return existsThingworxParentedNodeByTagValue(rootTag, NAME_ATTRIBUTE, name, THING_PACKAGES_NODE_NAME, THING_PACKAGE_NODE_NAME);
    }

    public boolean isThingShape(String className) {
        return existsThingworxParentedNodeByTagValue(rootTag, CLASSNAME_ATTRIBUTE, className, THING_SHAPES_NODE_NAME, THING_SHAPE_NODE_NAME);
    }

    public boolean isAuthenticator(String className) {
        return existsThingworxParentedNodeByTagValue(rootTag, CLASSNAME_ATTRIBUTE, className, AUTHENTICATORS_NODE_NAME, AUTHENTICATOR_NODE_NAME);
    }

    public boolean isDirectoryService(String className) {
        return existsThingworxParentedNodeByTagValue(rootTag, CLASSNAME_ATTRIBUTE, className, DIRECTORY_SERVICES_NODE_NAME, DIRECTORY_SERVICE_NODE_NAME);
    }

    public boolean isScriptLibrary(String className) {
        return existsThingworxParentedNodeByTagValue(rootTag, CLASSNAME_ATTRIBUTE, className, SCRIPT_FUNCTION_LIBRARIES_NODE_NAME, SCRIPT_FUNCTION_LIBRARY_NODE_NAME);
    }
}
