package it.lutechcdm.thingworxextensionplugin;

public class ThingworxConstants {

    public static final String THINGWORX_THING_TEMPLATE_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxBaseTemplateDefinition";
    public static final String THINGWORX_PROPERTY_DEFINITIONS_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxPropertyDefinitions";
    public static final String THINGWORX_PROPERTY_DEFINITION_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxPropertyDefinition";
    public static final String THINGWORX_EVENT_DEFINITIONS_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxEventDefinitions";
    public static final String THINGWORX_EVENT_DEFINITION_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxEventDefinition";
    public static final String THINGWORX_SERVICE_DEFINITION_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxServiceDefinition";
    public static final String THINGWORX_SERVICE_RESULT_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxServiceResult";
    public static final String THINGWORX_SERVICE_PARAMETER_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxServiceParameter";
    public static final String THINGWORX_CONFIG_TABLE_DEFINITIONS_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxConfigurationTableDefinitions";
    public static final String THINGWORX_CONFIG_TABLE_DEFINITION_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxConfigurationTableDefinition";
    public static final String THINGWORX_DATA_SHAPE_DEFINITION_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxDataShapeDefinition";
    public static final String THINGWORX_FIELD_DEFINITION_ANNOTATION = "com.thingworx.metadata.annotations.ThingworxFieldDefinition";

    public static final String TWX_LIB = "twx-lib";
    public static final String TWX_SDK = "twx-sdk";
    public static final String BIN_FOLDER = "bin";
    public static final String SRC_FOLDER = "src";
    public static final String LIB_FOLDER = "lib";
    public static final String CONFIG_FOLDER = "configfiles";
    public static final String UI_FOLDER = "ui";

    public static final String MODULE_LIBRARY_NAME = "project-libs";

    public static final String JUST_INFOTABLE = "Just Infotable";
    public static final String DATA_TABLE_INFOTABLE = "Is Data Table";
    public static final String STREAM_ENTRY_INFOTABLE = "Is Stream Entry";
    public static final String CONTENT_CRAWLER_INFOTABLE = "Is Content Crawler Entry";

    public static final String BUILD_EXTENSION_TEMPLATE_CONTENT = """
           <?xml version="1.0" encoding="UTF-8"?>
           <project name="{0}" basedir="." default="build">
                    
            <property name="extension.jar" value="{1}.jar" />
                    
            <property name="target"         value="1.8" />
            <property name="source"         value="1.8" />
            <property name="debuglevel"     value="source,lines,vars" />
            <property name="common"         value="common" />
            <property name="ui"             value="ui" />
            <property name="lib"            value="lib" />
            <property name="entities"       value="Entities" />
            <property name="localization"   value="Localization" />
            <property name="src.dir"        value="$'{'basedir'}'/src" />
            <property name="build.dir"      value="$'{'basedir'}'/bin" />
            <property name="config.dir"     value="$'{'basedir'}'/configfiles" />
            <property name="ui.dir"         value="$'{'basedir'}'/$'{'ui'}'" />
            <property name="lib.dir"        value="$'{'basedir'}'/$'{'lib'}'" />
            <property name="zip.dir"        value="$'{'basedir'}'/build/distributions" />
            <property name="entity.dir"     value="$'{'basedir'}'/Entities" />
            <property name="localization.dir"  value="$'{'basedir'}'/$'{'localization'}'" />
                    
            <property file="extension.properties" />
                    
            <!-- ExtensionPackage directory structure props -->
            <property name="package.lib.basedir" value="$'{'lib'}'" />
            <property name="package.ui.basedir" value="$'{'ui'}'" />
            <property name="package.common.lib.dir" value="$'{'package.lib.basedir'}'/$'{'common'}'" />
            <property name="package.common.ui.dir" value="$'{'package.ui.basedir'}'/$'{'common'}'" />
                    
            <!--  Extension file info -->
            <property name="zip.file.name" value="$'{'ant.project.name'}'.zip" />
                    
            <tstamp>
                <format property="NOW" pattern="yyyy-MM-dd HH:mm:ss" />
            </tstamp>
                    
            <!-- define the classpath so it picks up the ThingWorx SDK jar relative to this basedir -->
            <path id="jar.classpath">
                <pathelement location="$'{'build.dir'}'" />
                <fileset dir="$'{'basedir'}'/twx-lib" includes="*.jar" />
                <fileset dir="$'{'lib.dir'}'" includes="*.jar" erroronmissingdir="false" />
            </path>
                    
            <target name="clean">
                <delete dir="$'{'build.dir'}'" />
                <delete dir="$'{'zip.dir'}'" />
            </target>
                    
            <target name="init" depends="clean">
                    
                <mkdir dir="$'{'build.dir'}'" />
                    
                <copy includeemptydirs="false" todir="$'{'build.dir'}'">
                    <fileset dir="$'{'src.dir'}'">
                        <exclude name="**/*.launch" />
                        <exclude name="**/*.java" />
                    </fileset>
                </copy>
                    
            </target>
                    
            <target name="build-source" depends="init">
                <echo message="$'{'ant.project.name'}': $'{'ant.file'}'" />
                <javac debug="true" debuglevel="$'{'debuglevel'}'" destdir="$'{'build.dir'}'" source="$'{'source'}'" target="$'{'target'}'" includeantruntime="false">
                    <src path="$'{'src.dir'}'" />
                    <classpath refid="jar.classpath" />
                </javac>
            </target>
                    
            <target name="check-bin" depends="build-source">
                <fileset dir="$'{'build.dir'}'" id="binfileset" />
                <condition property="bindir.empty">
                    <length length="0">
                        <fileset refid="binfileset" />
                    </length>
                </condition>
            </target>
                    
            <target name="build-jars" depends="check-bin" unless="bindir.empty">
                <echo message="building $'{'extension.jar'}' to $'{'build.dir'}'..." />
                <jar destfile="$'{'build.dir'}'/$'{'extension.jar'}'">
                    <!-- generate MANIFEST inline -->
                    <manifest>
                        <attribute name="Built-By" value="Eclipse Plugin for ThingWorx Extension Development $'{'plugin_version'}'" />
                        <attribute name="Build-Date" value="$'{'NOW'}'" />
                        <section name="$'{'ant.project.name'}'">
                            <attribute name="Package-Title" value="$'{'ant.project.name'}'" />
                            <attribute name="Package-Version" value="$'{'package_version'}'" />
                            <attribute name="Package-Vendor" value="$'{'project_vendor'}'" />
                        </section>
                    </manifest>
                    
                    <fileset dir="$'{'build.dir'}'" />
                </jar>
            </target>
                    
            <target name="package-extension" depends="build-jars">
                <zip destfile="$'{'zip.dir'}'/$'{'zip.file.name'}'">
                    <mappedresources>
                        <fileset dir="$'{'build.dir'}'" includes="$'{'extension.jar'}'" />
                        <globmapper from="*" to="$'{'package.common.lib.dir'}'/*" />
                    </mappedresources>
                    
                    <zipfileset dir="$'{'config.dir'}'" includes="metadata.xml" />
                    
                    <zipfileset dir="$'{'basedir'}'" includes="$'{'entities'}'/**/*.xml" />
                    <zipfileset dir="$'{'basedir'}'" includes="$'{'localization'}'/**/*.json" />
                    <zipfileset dir="$'{'basedir'}'" includes="$'{'ui'}'/**/*.*" />
                    <zipfileset dir="$'{'lib.dir'}'" includes="**/*.jar" prefix="$'{'package.common.lib.dir'}'/"/>
                    
                </zip>
            </target>
                    
            <target name="build" depends="package-extension">
                <echo message="Building $'{'ant.project.name'}' extension package..."/>
            </target>
                    
           </project>
            """;

    public static final String METADATA_INFO_TEMPLATE_CONTENT = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Entities>
                    <ExtensionPackages>
                        <ExtensionPackage dependsOn="" description="" haCompatible="{0}" minimumThingWorxVersion="{1}" name="{2}" packageVersion="{3}" vendor="{4}" />
                    </ExtensionPackages>
                </Entities>
            """;

    public static final String WIDGET_CSS_TEMPLATE_CONTENT = """ 
            /* Place custom CSS styling for {0} widget in Composer in this file */
                      
            .widget-{0} '{
            
            }'
            """;

    public static final String WIDGET_RUNTIME_JS_TEMPLATE_CONTENT = """
            TW.Runtime.Widgets.{0} = function () '{'
            	var valueElem;
            	this.renderHtml = function () '{'
            		// return any HTML you want rendered for your widget
            		// If you want it to change depending on properties that the user
            		// has set, you can use this.getProperty(propertyName). In
            		// this example, we''ll just return static HTML
            		return 	''<div class="widget-content widget-{0}">'' +
            					''<span class="{0}-property">'' + this.getProperty(''{0} Property'') + ''</span>'' +
            				''</div>'';
            	'}';
                        
            	this.afterRender = function () '{'
            		// NOTE: this.jqElement is the jquery reference to your html dom element
            		// 		 that was returned in renderHtml()
                        
            		// get a reference to the value element
            		valueElem = this.jqElement.find(''.{0}-property'');
            		// update that DOM element based on the property value that the user set
            		// in the mashup builder
            		valueElem.text(this.getProperty(''{0} Property''));
            	'}';
                        
            	// this is called on your widget anytime bound data changes
            	this.updateProperty = function (updatePropertyInfo) '{'
            		// TargetProperty tells you which of your bound properties changed
            		if (updatePropertyInfo.TargetProperty === ''{0} Property'') '{'
            			valueElem.text(updatePropertyInfo.SinglePropertyValue);
            			this.setProperty(''{0} Property'', updatePropertyInfo.SinglePropertyValue);
            		'}'
            	'}';
            '}';
            """;

        public static final String WIDGET_IDE_JS_TEMPLATE_CONTENT = """
                TW.IDE.Widgets.{0} = function () '{'
                            
                    this.widgetIconUrl = function() '{'
                        return  "''../Common/extensions/{1}/ui/{0}/default_widget_icon.ide.png''";
                    '}';
                            
                    this.widgetProperties = function () '{'
                        return '{'
                            ''name'': ''{0}'',
                            ''description'': '''',
                            ''category'': [''Common''],
                            ''properties'': '{'
                                ''{0} Property'': '{'
                                    ''baseType'': ''STRING'',
                                    ''defaultValue'': ''{0} Property default value'',
                                    ''isBindingTarget'': true
                                '}'
                            '}'
                        '}'
                    '}';
                            
                    this.afterSetProperty = function (name, value) '{'
                        var thisWidget = this;
                        var refreshHtml = false;
                        switch (name) '{'
                            case ''Style'':
                            case ''{0} Property'':
                                thisWidget.jqElement.find(''.{0}-property'').text(value);
                            case ''Alignment'':
                                refreshHtml = true;
                                break;
                            default:
                                break;
                        '}'
                        return refreshHtml;
                    '}';
                            
                    this.renderHtml = function () '{'
                        // return any HTML you want rendered for your widget
                        // If you want it to change depending on properties that the user
                        // has set, you can use this.getProperty(propertyName).
                        return 	''<div class="widget-content widget-{0}">'' +
                                    ''<span class="{0}-property">'' + this.getProperty(''{0} Property'') + ''</span>'' +
                                ''</div>'';
                    '}';
                            
                    this.afterRender = function () '{'
                        // NOTE: this.jqElement is the jquery reference to your html dom element
                        // 		 that was returned in renderHtml()
                            
                        // get a reference to the value element
                        valueElem = this.jqElement.find(''.{0}-property'');
                        // update that DOM element based on the property value that the user set
                        // in the mashup builder
                        valueElem.text(this.getProperty(''{0} Property''));
                    '}';
                            
                '}';
                """;
}
