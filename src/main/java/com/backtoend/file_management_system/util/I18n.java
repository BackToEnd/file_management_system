package com.backtoend.file_management_system.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.ResourceBundle;

/// Service class responsible for resource bundles and locales; contains methods relevant for translations and String bindings.
/// @author Rogue049
public class I18n {
    /// Points to the resource bundle of all translation files
    private static final String LOCATION = "com.backtoend.file_management_system.messages";
    /// Creates a single, static I18n instance
    private static final I18n INSTANCE = new I18n();
    /// Loads the default resource bundle for English translations
    private final ObjectProperty<ResourceBundle> bundle = new SimpleObjectProperty<>(
            ResourceBundle.getBundle(LOCATION, Locale.ENGLISH)
    );

    /// Private constructor; this class uses a singleton of itself to distribute translation keys and String bindings.
    /// Other classes are not meant to create their own instance of this class.
    private I18n() {
    }

    // region Public methods that can be utilized by other classes

    /// Get the {@link I18n} singleton instance
    /// @return instance
    public static I18n getInstance() {
        return INSTANCE;
    }

    /// Translate the provided String based on current locale
    /// @param key translation key
    /// @return translation of the current locale
    public String get(String key) {
        return bundle.get().getString(key);
    }

    /// Create a {@link StringBinding} of the provided key. String bindings automatically change value
    /// if the locale and resource bundle changes.
    /// @param key translation key
    /// @return `StringBinding` of the provided key
    public StringBinding bind(String key) {
        return Bindings.createStringBinding(() -> get(key), bundle);
    }

    /// Change the current locale to the provided one
    /// @param locale new locale
    public void changeLocale(Locale locale) {
        bundle.set(ResourceBundle.getBundle(LOCATION, locale));
    }

    /// Get the current resource bundle. It's not advised to use this method for regular
    /// translations. Use {@link I18n#get(String)} or {@link I18n#bind(String)} instead.
    /// @return the current resource bundle
    public ResourceBundle getBundle() {
        return bundle.get();
    }
}
