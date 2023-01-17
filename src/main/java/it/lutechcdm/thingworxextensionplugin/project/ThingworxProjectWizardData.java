package it.lutechcdm.thingworxextensionplugin.project;

public class ThingworxProjectWizardData {

    final String sdkLocation;

    final String vendor;

    final String packageVersion;

    final String minTwxVersion;

    final boolean haCompatible;

    public ThingworxProjectWizardData(String sdkLocation, String vendor, String packageVersion, String minTwxVersion, boolean haCompatible) {
        this.sdkLocation = sdkLocation;
        this.vendor = vendor;
        this.packageVersion = packageVersion;
        this.minTwxVersion = minTwxVersion;
        this.haCompatible = haCompatible;
    }
}
