package hudson.plugins.locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import hudson.util.ListBoxModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PluginImplTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private PluginImpl plugin;

    @Before
    public void setUp() {
        plugin = Jenkins.get().getExtensionList(PluginImpl.class).get(0);
    }

    // Set of allowed locales for the test
    private static final Set<String> ALLOWED_LOCALES = new HashSet<>(Arrays.asList(
            "bg", "ca", "cs", "da", "de", "el", "en_GB", "es", "es_AR", "et", "fi", "fr", "he", "hu", "it", "ja", "ko",
            "lt", "lv", "nb_NO", "nl", "pl", "pt_BR", "pt_PT", "ro", "ru", "sk", "sl", "sr", "sv_SE", "tr", "uk",
            "zh_CN", "zh_TW"));

    @Test
    public void testDoFillSystemLocaleItems() {
        // Invoke the method
        ListBoxModel model = plugin.doFillSystemLocaleItems();

        // Expected size of the ListBoxModel
        int expectedSize = ALLOWED_LOCALES.size() + 1; // +1 for the "Use Browser Locale" option

        // Verify the returned ListBoxModel size
        assertEquals("The returned ListBoxModel size is not as expected", expectedSize, model.size());

        // Verify that the first option is "Use Browser Locale"
        String expectedFirstOption = String.format(
                "Use Browser Locale - %s (%s)",
                Locale.getDefault().getDisplayName(), Locale.getDefault().toString());
        assertEquals("The first option should be 'Use Browser Locale'", expectedFirstOption, model.get(0).name);

        // Verify that the allowed locales are correctly added to the ListBoxModel, excluding the first option
        for (String localeStr : ALLOWED_LOCALES) {
            Locale locale = Locale.forLanguageTag(localeStr.replace('_', '-'));
            String expectedOption = String.format("%s - %s", locale.getDisplayName(), locale.toString());

            boolean found = false;
            for (int i = 1; i < model.size(); i++) { // Start from 1 to skip the "Use Browser Locale" option
                if (model.get(i).name.equals(expectedOption)) {
                    found = true;
                    break;
                }
            }
            assertEquals("The ListBoxModel does not contain the expected locale: " + locale, true, found);
        }
    }

    @Test
    public void testSetSystemLocale() {
        // Test setting systemLocale
        String systemLocale = "en_US";
        plugin.setSystemLocale(systemLocale);
        assertEquals(systemLocale, plugin.getSystemLocale());
    }

    @Test
    public void testSetIgnoreAcceptLanguage() {
        // Test setting ignoreAcceptLanguage
        boolean ignoreAcceptLanguage = true;
        plugin.setIgnoreAcceptLanguage(ignoreAcceptLanguage);
        assertEquals(ignoreAcceptLanguage, plugin.isIgnoreAcceptLanguage());
    }

    @Test
    public void testNullSystemLocale() {
        // Test setting systemLocale to null
        plugin.setSystemLocale(null);
        assertNull("System locale should be null", plugin.getSystemLocale());
    }

    @Test
    public void testEmptySystemLocale() {
        // Test setting systemLocale to empty string
        plugin.setSystemLocale("");
        assertNull("System locale should be empty", plugin.getSystemLocale());
    }
}
