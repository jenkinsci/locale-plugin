package hudson.plugins.locale;

import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.jvnet.hudson.test.JenkinsRule;
import hudson.util.ListBoxModel;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PluginImplTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private PluginImpl plugin;

    @Before
    public void setUp(){
        plugin = Jenkins.get().getExtensionList(PluginImpl.class).get(0);
    }

    @Test
    public void testDoFillSystemLocaleItems(){
        // Invoke the method
        ListBoxModel model = plugin.doFillSystemLocaleItems();


        // Verify the returned ListBoxModel
        assertEquals("The returned ListBoxModel size is not as expected", Locale.getAvailableLocales().length, model.size());

        // Verify that the locales are correctly added to the ListBoxModel
        for (Locale locale : Locale.getAvailableLocales()) {
            boolean found = false;
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i).name.equals(locale.getDisplayName())) {
                    found = true;
                    break;
                }
            }
            assertEquals("The ListBoxModel does not contain the expected locale", true, found);
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
        assertEquals(Locale.getDefault().toString(), plugin.getSystemLocale());
    }

    @Test
    public void testEmptySystemLocale() {
        // Test setting systemLocale to empty string
        plugin.setSystemLocale("");
        assertEquals(Locale.getDefault().toString(), plugin.getSystemLocale());
    }
}
