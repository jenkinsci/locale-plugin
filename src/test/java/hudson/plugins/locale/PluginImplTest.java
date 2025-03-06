package hudson.plugins.locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.util.ListBoxModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PluginImplTest {

    private PluginImpl plugin;

    @BeforeEach
    void setUp(JenkinsRule j) {
        plugin = Jenkins.get().getExtensionList(PluginImpl.class).get(0);
    }

    // Set of allowed locales for the test
    private static final Set<String> ALLOWED_LOCALES = new HashSet<>(Arrays.asList(
            "bg", "ca", "cs", "da", "de", "el", "en", "es", "es_AR", "et", "fi", "fr", "he", "hu", "it", "ja", "ko",
            "lt", "lv", "nb_NO", "nl", "pl", "pt_BR", "pt_PT", "ro", "ru", "sk", "sl", "sr", "sv", "tr", "uk", "zh_CN",
            "zh_TW"));

    @Test
    void testDoFillSystemLocaleItems() {
        // Invoke the method
        ListBoxModel model = plugin.doFillSystemLocaleItems();

        // Expected size of the ListBoxModel
        int expectedSize = ALLOWED_LOCALES.size() + 1; // +1 for the "Use Default Locale" option

        // Verify the returned ListBoxModel size
        assertEquals(expectedSize, model.size(), "The returned ListBoxModel size is not as expected");

        // Verify that the first option is "Use Default Locale"
        String expectedFirstOption = String.format(
                "Use Default Locale - %s (%s)",
                Locale.getDefault().getDisplayName(), Locale.getDefault().toString());
        assertEquals(expectedFirstOption, model.get(0).name, "The first option should be 'Use Default Locale'");

        // Verify that the allowed locales are correctly added to the ListBoxModel, excluding the first option
        for (String localeStr : ALLOWED_LOCALES) {
            Locale locale = Locale.forLanguageTag(localeStr.replace('_', '-'));
            String expectedOption = String.format("%s - %s", locale.getDisplayName(), locale);

            boolean found = false;
            for (int i = 1; i < model.size(); i++) { // Start from 1 to skip the "Use Default Locale" option
                if (model.get(i).name.equals(expectedOption)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "The ListBoxModel does not contain the expected locale: " + locale);
        }
    }

    @Test
    void testSetSystemLocale() {
        // Test setting systemLocale
        String systemLocale = "en_US";
        plugin.setSystemLocale(systemLocale);
        assertEquals(systemLocale, plugin.getSystemLocale());
    }

    @Test
    void testSetIgnoreAcceptLanguage() {
        // Test setting ignoreAcceptLanguage
        boolean ignoreAcceptLanguage = true;
        plugin.setIgnoreAcceptLanguage(ignoreAcceptLanguage);
        assertEquals(ignoreAcceptLanguage, plugin.isIgnoreAcceptLanguage());
    }

    @Test
    void testNullSystemLocale() {
        // Test setting systemLocale to null
        plugin.setSystemLocale(null);
        assertNull(plugin.getSystemLocale(), "System locale should be null");
    }

    @Test
    void testEmptySystemLocale() {
        // Test setting systemLocale to empty string
        plugin.setSystemLocale("");
        assertNull(plugin.getSystemLocale(), "System locale should be empty");
    }
}
