package hudson.plugins.locale;

import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.jvnet.hudson.test.JenkinsRule;
import hudson.util.ListBoxModel;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PluginImplTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private PluginImpl plugin;

    @Before
    public void setUp(){
        plugin = Jenkins.get().getExtensionList(PluginImpl.class).get(0);
    }

    @Test
    public void testDoFillSystemLocaleItems() {
        // Invoke the method
        ListBoxModel model = plugin.doFillSystemLocaleItems();

        // Verify the returned ListBoxModel size
        assertEquals("The returned ListBoxModel size is not as expected", Locale.getAvailableLocales().length , model.size());

        // Verify that the first option is "Use Browser Locale"
        assertEquals("The first option should be 'Use Browser Locale'", "Use Browser Locale - " + Locale.getDefault().getDisplayName() + " (" + Locale.getDefault().toString() + ")", model.get(0).name);

        // Verify that the locales are correctly added to the ListBoxModel, excluding the first option
        for (Locale locale : Locale.getAvailableLocales()) {
            if(locale==null || locale.toString().isEmpty()) continue;

            boolean found = false;
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i).name.equals(locale.getDisplayName() + " - " + locale.toString())) {
                    found = true;
                    break;
                }
            }
            assertEquals("The ListBoxModel does not contain the expected locale: " + locale, true, found);
        }
    }


    @Test
    public void testSetSystemLocale(){
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
