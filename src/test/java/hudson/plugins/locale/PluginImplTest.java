package hudson.plugins.locale;

import static hudson.plugins.locale.PluginImpl.ALLOWED_LOCALES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.util.ListBoxModel;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PluginImplTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void testDoFillSystemLocaleItems() {
        PluginImpl plugin = PluginImpl.get();

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
        PluginImpl plugin = PluginImpl.get();
        String systemLocale = "en_US";
        plugin.setSystemLocale(systemLocale);
        assertEquals(systemLocale, plugin.getSystemLocale());
    }

    @Test
    void testEmptySystemLocale() {
        PluginImpl plugin = PluginImpl.get();
        plugin.setSystemLocale("");
        assertNull(plugin.getSystemLocale(), "System locale should be empty");
    }

    @Test
    void testNullSystemLocale() {
        PluginImpl plugin = PluginImpl.get();
        plugin.setSystemLocale(null);
        assertNull(plugin.getSystemLocale(), "System locale should be null");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSetIgnoreAcceptLanguage(boolean ignoreAcceptLanguage) {
        PluginImpl plugin = PluginImpl.get();
        plugin.setIgnoreAcceptLanguage(ignoreAcceptLanguage);
        assertEquals(ignoreAcceptLanguage, plugin.isIgnoreAcceptLanguage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSetAllowUserPreferences(boolean allowUserPreferences) {
        PluginImpl plugin = PluginImpl.get();
        plugin.setAllowUserPreferences(allowUserPreferences);
        assertEquals(allowUserPreferences, plugin.isAllowUserPreferences());
    }

    @Test
    @WithoutJenkins
    @Issue("https://github.com/jenkinsci/locale-plugin/pull/309#issuecomment-2912228288")
    void parseLocaleString() {
        assertEquals(new Locale(""), PluginImpl.parse(""));
        assertEquals(new Locale("en"), PluginImpl.parse("en"));
        assertEquals(new Locale("en", "US"), PluginImpl.parse("en_US"));
        assertEquals(new Locale("en-US"), PluginImpl.parse("en-US"));
        assertEquals(new Locale("Locale.ENGLISH"), PluginImpl.parse("Locale.ENGLISH"));
        assertThrows(IllegalArgumentException.class, () -> PluginImpl.parse("string_with_more_than_3_underscores"));
    }
}
