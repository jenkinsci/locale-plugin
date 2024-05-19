package hudson.plugins.locale;

import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

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

    @LocalData
    @Test
    public void testGetLocales(){
        // Test that getLocales() returns a non-empty list of locales
        assertEquals(false, plugin.getLocales().isEmpty());
    }

    @LocalData
    @Test
    public void testSetSystemLocale(){
        // Test setting systemLocale
        String systemLocale = "en_US";
        plugin.setSystemLocale(systemLocale);
        assertEquals(systemLocale, plugin.getSystemLocale());
    }

    @LocalData
    @Test
    public void testSetIgnoreAcceptLanguage() {
        // Test setting ignoreAcceptLanguage
        boolean ignoreAcceptLanguage = true;
        plugin.setIgnoreAcceptLanguage(ignoreAcceptLanguage);
        assertEquals(ignoreAcceptLanguage, plugin.isIgnoreAcceptLanguage());
    }

    @LocalData
    @Test
    public void testNullSystemLocale() {
        // Test setting systemLocale to null
        plugin.setSystemLocale(null);
        assertEquals(Locale.getDefault().toString(), plugin.getSystemLocale());
    }

    @LocalData
    @Test
    public void testEmptySystemLocale() {
        // Test setting systemLocale to empty string
        plugin.setSystemLocale("");
        assertEquals(Locale.getDefault().toString(), plugin.getSystemLocale());
    }
}
