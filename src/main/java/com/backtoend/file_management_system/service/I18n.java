package com.backtoend.file_management_system.service;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.util.Locale;
import java.util.ResourceBundle;

/// Service class handling translation keys, locales, and resource bundles
/// @author Rogue049
public final class I18n {
    /// Source path of all resource bundles
    private static final String BASE_NAME = "com.backtoend.file_management_system.messages";
    /// Static instance of itself that can then be used by all other classes
    private static final I18n INSTANCE = new I18n();
    /// Initially loading the English resource bundle at app launch
    private final ObjectProperty<ResourceBundle> bundle =
            new SimpleObjectProperty<>(
                    ResourceBundle.getBundle(BASE_NAME, Locale.ENGLISH)
            );
    private I18n() {
    }

    /// Returns the usable I18n instance
    /// @return I18n instance
    public static I18n getInstance() {
        return INSTANCE;
    }

    /// Retrieve the current locale's translation for the given key
    /// @param key Translation key
    /// @return Translation String
    public String get(String key) {
        return bundle.get().getString(key);
    }

    /// Returns a StringBinding of the given key. The value of the StringBinding will change
    /// whenever the user picks a different language; should be used for UI elements instead
    /// of {@link get} because those are not updated unless the FXML loader is regenerated.
    /// @param key Translation key
    /// @return A StringBinding of the translation key
    public StringBinding text(String key) {
        return Bindings.createStringBinding(() -> get(key), bundle);
    }

    /// Changes the current locale to the provided one
    /// @param locale Supported locale
    public void setLocale(Locale locale) {
        bundle.set(ResourceBundle.getBundle(BASE_NAME, locale));
    }

    /// Gets the current resource bundle, for edge cases in which {@link get} or
    /// {@link text} don't suffice.
    /// @return The current resource bundle
    public ResourceBundle getBundle() {
        return bundle.get();
    }

}
