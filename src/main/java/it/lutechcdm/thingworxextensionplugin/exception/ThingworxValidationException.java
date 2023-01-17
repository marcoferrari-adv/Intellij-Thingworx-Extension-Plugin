package it.lutechcdm.thingworxextensionplugin.exception;

import javax.swing.*;

public class ThingworxValidationException extends Exception {

    private JComponent source;

    public ThingworxValidationException(String message) {
        super(message);
    }

    public ThingworxValidationException(String message, JComponent source) {
        super(message);
        this.source = source;
    }

    public JComponent getSource() {
        return source;
    }
}
