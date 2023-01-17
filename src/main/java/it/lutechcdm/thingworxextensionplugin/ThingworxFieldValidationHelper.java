package it.lutechcdm.thingworxextensionplugin;

import it.lutechcdm.thingworxextensionplugin.exception.ThingworxValidationException;

import javax.swing.*;

public class ThingworxFieldValidationHelper {

    public static void validatePropertyName(String name, JComponent source) throws ThingworxValidationException {

        if(name == null || name.isBlank()) {
            throw new ThingworxValidationException("Name attribute is required", source);
        }

        if(!name.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
            throw new ThingworxValidationException("Name attribute must follow [a-zA-Z]\\d+ pattern", source);
        }
    }

    public static void validateWidgetName(String name, JComponent source) throws ThingworxValidationException {

        if(name == null || name.isBlank()) {
            throw new ThingworxValidationException("Name attribute is required", source);
        }

        if(!name.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            throw new ThingworxValidationException("Name attribute must follow [a-zA-Z]\\d+ pattern", source);
        }
    }

    public static void validateDefaultValueForType(String value, ThingworxBaseTypes type, JComponent source) throws ThingworxValidationException {

        if(ThingworxBaseTypes.isNumericType(type) || type == ThingworxBaseTypes.INFOTABLE) {
            if (value == null || value.isBlank()) {
                throw new ThingworxValidationException("Default value is empty attribute is required", source);
            }
        }

        if(ThingworxBaseTypes.isNumericType(type)) {
            try {
                Double.parseDouble(value);
            }
            catch (NumberFormatException nfe) {
                throw new ThingworxValidationException("Value is not a valid numeric format", source);
            }
        }
    }

    public static void validateNumericValue(String value, boolean required, JComponent source) throws ThingworxValidationException {
        if (required && (value == null || value.isBlank())) {
            throw new ThingworxValidationException("Value is required", source);
        }

        try {
            Double.parseDouble(value);
        }
        catch (NumberFormatException nfe) {
            throw new ThingworxValidationException("Value is not a valid numeric format", source);
        }
    }

    public static void validateRequired(String value, JComponent source) throws ThingworxValidationException {
        if (value == null || value.isBlank()) {
            throw new ThingworxValidationException("Value is required", source);
        }
    }
}