package it.lutechcdm.thingworxextensionplugin.definitions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ThingworxBaseTypes {
    NOTHING((byte) -1, "Nothing", null),
    STRING((byte) 0, "String", String.class.getName()),
    NUMBER((byte) 1, "Number", Double.class.getName()),
    BOOLEAN((byte) 2, "Boolean", Boolean.class.getName()),
    DATETIME((byte) 3, "DateTime", "org.joda.time.DateTime"),
    TIMESPAN((byte) 4, "Timespan", "com.thingworx.types.primitives.structs.Timespan"),
    INFOTABLE((byte) 5, "InfoTable", "com.thingworx.types.InfoTable"),
    LOCATION((byte) 6, "Location", "com.thingworx.types.primitives.structs.Location"),
    XML((byte) 7, "XML", "org.w3c.dom.Document"),
    JSON((byte) 8, "JSON", "org.json.JSONObject"),
    QUERY((byte) 9, "Query", "org.json.JSONObject"),
    IMAGE((byte) 10, "Image", byte.class.getName(), true),
    HYPERLINK((byte) 11, "Hyperlink", String.class.getName()),
    IMAGELINK((byte) 12, "ImageLink", String.class.getName()),
    PASSWORD((byte) 13, "Password"),
    HTML((byte) 14, "HTML", String.class.getName()),
    TEXT((byte) 15, "Text", String.class.getName()),
    TAGS((byte) 16, "Tags", "com.thingworx.types.TagCollection"),
    SCHEDULE((byte) 17, "Schedule"),
    VARIANT((byte) 18, "Variant"),
    GUID((byte) 20, "GUID", String.class.getName()),
    BLOB((byte) 21, "BLOB", byte.class.getName(), true),
    INTEGER((byte) 22, "Integer", Integer.class.getName()),
    LONG((byte) 23, "Long", Long.class.getName()),
    PROPERTYNAME((byte) 50, "PropertyName"),
    SERVICENAME((byte) 51, "ServiceName"),
    EVENTNAME((byte) 52, "EventName"),
    THINGGROUPNAME((byte) 99, "ThingGroupName", String.class.getName()),
    THINGNAME((byte) 100, "ThingName", String.class.getName()),
    THINGSHAPENAME((byte) 101, "ThingShapeName", String.class.getName()),
    THINGTEMPLATENAME((byte) 102, "ThingTemplateName", String.class.getName()),
    DATASHAPENAME((byte) 104, "DataShapeName", String.class.getName()),
    MASHUPNAME((byte) 105, "MashupName", String.class.getName()),
    MENUNAME((byte) 106, "MenuName", String.class.getName()),
    BASETYPENAME((byte) 107, "BaseTypeName", String.class.getName()),
    USERNAME((byte) 108, "UserName", String.class.getName()),
    GROUPNAME((byte) 109, "GroupName", String.class.getName()),
    CATEGORYNAME((byte) 110, "CategoryName"),
    STATEDEFINITIONNAME((byte) 111, "StateDefinitionName"),
    STYLEDEFINITIONNAME((byte) 112, "StyleDefinitionName"),
    MODELTAGVOCABULARYNAME((byte) 113, "ModelTagVocabularyName"),
    DATATAGVOCABULARYNAME((byte) 114, "DataTagVocabularyName"),
    NETWORKNAME((byte) 115, "NetworkName"),
    MEDIAENTITYNAME((byte) 116, "MediaEntityName"),
    APPLICATIONKEYNAME((byte) 117, "ApplicationKeyName"),
    LOCALIZATIONTABLENAME((byte) 118, "LocalizationTableName"),
    ORGANIZATIONNAME((byte) 119, "OrganizationName"),
    DASHBOARDNAME((byte) 120, "DashboardName", String.class.getName()),
    PERSISTENCEPROVIDERPACKAGENAME((byte) 121, "PersistenceProviderPackageName", String.class.getName()),
    PERSISTENCEPROVIDERNAME((byte) 122, "PersistenceProviderName"),
    PROJECTNAME((byte) 123, "ProjectName"),
    VEC2((byte) 124, "Vec2", "com.thingworx.types.primitives.structs.Vec2"),
    VEC3((byte) 125, "Vec3", "com.thingworx.types.primitives.structs.Vec3"),
    VEC4((byte) 126, "Vec4", "com.thingworx.types.primitives.structs.Vec4"),
    THINGCODE((byte)127, "ThingCode", "com.thingworx.types.primitives.structs.ThingCode"),
    NOTIFICATIONCONTENTNAME((byte) -128, "NotificationContentName"),
    NOTIFICATIONDEFINITIONNAME((byte) -127, "NotificationDefinitionName"),
    STYLETHEMENAME((byte) -126, "StyleThemeName");
    private final byte code;

    private final String javaClass;

    private final String friendlyName;

    private final boolean isJavaArray;

    ThingworxBaseTypes(byte code, String friendlyName) {
        this(code, friendlyName, null);
    }

    ThingworxBaseTypes(byte code, String friendlyName, String javaClass) {
        this(code, friendlyName, javaClass, false);
    }

    ThingworxBaseTypes(byte code, String friendlyName, String javaClass, boolean isJavaArray) {
        this.code = code;
        this.javaClass = javaClass;
        this.friendlyName = friendlyName;
        this.isJavaArray = isJavaArray;
    }

    public static String[] getPropertyDefinitionBaseTypeList() {
        List<String> alBaseTypes = Arrays.asList(STRING.name(), PASSWORD.name(), NUMBER.name(), INTEGER.name(), LONG
        .name(), BOOLEAN.name(), DATETIME.name(), INFOTABLE.name(), LOCATION.name(), IMAGE.name(),
                XML.name(), JSON.name(), QUERY
                .name(), THINGNAME.name(), USERNAME.name(), GROUPNAME.name(), HYPERLINK.name(), IMAGELINK.name(), MASHUPNAME.name(), MENUNAME
                .name(),
                DASHBOARDNAME.name(), HTML.name(), TEXT.name(), GUID.name(), BLOB.name(), VEC2.name(), VEC3.name(), VEC4
                .name(), THINGCODE.name(), NOTIFICATIONCONTENTNAME.name(),
                NOTIFICATIONDEFINITIONNAME.name(), STYLETHEMENAME.name(), THINGGROUPNAME.name());
        Collections.sort(alBaseTypes);
        return alBaseTypes.toArray(new String[0]);
    }

    public static boolean isNumericType(ThingworxBaseTypes type) {
        return type != null && (type.code == INTEGER.code || type.code == LONG.code || type.code == NUMBER.code);
    }

    public static boolean isJavaClassType(ThingworxBaseTypes type) {
        return type.javaClass != null;
    }

    public static boolean isJavaArrayType(ThingworxBaseTypes type) {
        return type.javaClass != null && type.isJavaArray;
    }

    public String getJavaClass() {
        return javaClass;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
