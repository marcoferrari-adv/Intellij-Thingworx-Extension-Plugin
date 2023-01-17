package it.lutechcdm.thingworxextensionplugin.project;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

public class ThingworxModuleWizardStep extends ModuleWizardStep implements Disposable {

    private final WizardContext context;
    private final ThingworxGeneratorPeer myPeer;

    public ThingworxModuleWizardStep(final WizardContext context) {
        this.context = context;
        myPeer = new ThingworxGeneratorPeer();
    }

    @Override
    public JComponent getComponent() {
        return myPeer.getComponent();
    }

    @Override
    public void updateDataModel() {
        final ProjectBuilder projectBuilder = context.getProjectBuilder();
        if (projectBuilder instanceof ThingworxModuleBuilder) {
            ((ThingworxModuleBuilder) projectBuilder).setWizardData(myPeer.getSettings());
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        super.validate();
        return myPeer.validateInIntelliJ();
    }


    @Override
    public void dispose() {

    }
}
