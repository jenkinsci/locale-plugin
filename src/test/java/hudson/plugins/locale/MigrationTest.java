package hudson.plugins.locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import hudson.Plugin;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

/**
 * Tests data loading from Locale Plugin 1.3.
 * @author Oleg Nenashev
 */
public class MigrationTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @LocalData
    @Test
    public void dataMigration_13() {
        PluginImpl plugin = Jenkins.get().getExtensionList(PluginImpl.class).get(0);
        assertEquals("en-US", plugin.getSystemLocale());
        assertEquals(true, plugin.isIgnoreAcceptLanguage());
    }

    @LocalData
    @Test
    public void dataMigration_UnsetLocale() {
        PluginImpl plugin = Jenkins.get().getExtensionList(PluginImpl.class).get(0);

        // Assuming the default behavior if systemLocale is unset
        assertEquals(PluginImpl.USE_BROWSER_LOCALE, plugin.getSystemLocale());
        assertFalse(plugin.isIgnoreAcceptLanguage());
    }
}
