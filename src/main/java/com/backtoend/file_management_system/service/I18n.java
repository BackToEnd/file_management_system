package com.backtoend.file_management_system.service;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Handling locales and translations via resource bundle
 */
public final class I18n {
    private static final String BASE_NAME = "com.backtoend.file_management_system.messages";
    private static final I18n INSTANCE = new I18n();
    private final ObjectProperty<ResourceBundle> bundle =
            new SimpleObjectProperty<>(
                    ResourceBundle.getBundle(BASE_NAME, Locale.ENGLISH)
            );
    private I18n() {
    }

    /**
     * Get the singleton instance of this class
     * @return Global I18n instance
     */
    public static I18n getInstance() {
        return INSTANCE;
    }

    /**
     * Get the translation for the specified key
     * @param key Resource bundle key
     * @return Translation
     */
    public String get(String key) {
        return bundle.get().getString(key);
    }

    /**
     * Get the StringBinding for the specified key<br>
     * Used primarily for translated UI elements
     * @param key Resource bundle key
     * @return StringBinding of the given key
     */
    public StringBinding text(String key) {
        return Bindings.createStringBinding(() -> get(key), bundle);
    }

    /**
     * Changes the locale in order to switch languages<br>
     * Currently only supports en and de
     * @param locale The new locale
     */
    public void setLocale(Locale locale) {
        bundle.set(ResourceBundle.getBundle(BASE_NAME, locale));
    }

    /**
     * Get the currently selected resource bundle
     * @return The current resource bundle
     */
    public ResourceBundle getBundle() {
        return bundle.get();
    }

}
