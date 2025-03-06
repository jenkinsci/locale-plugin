package hudson.plugins.locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

/**
 * Tests data loading from Locale Plugin 1.3.
 * @author Oleg Nenashev
 */
@WithJenkins
class MigrationTest {

    @LocalData
    @Test
    void dataMigration_13(JenkinsRule j) {
        PluginImpl plugin = Jenkins.get().getExtensionList(PluginImpl.class).get(0);
        assertEquals("en-US", plugin.getSystemLocale());
        assertTrue(plugin.isIgnoreAcceptLanguage());
    }

    @LocalData
    @Test
    void dataMigration_UnsetLocale(JenkinsRule j) {
        PluginImpl plugin = Jenkins.get().getExtensionList(PluginImpl.class).get(0);

        // Assuming the default behavior if systemLocale is unset
        assertEquals(PluginImpl.USE_BROWSER_LOCALE, plugin.getSystemLocale());
        assertFalse(plugin.isIgnoreAcceptLanguage());
    }
}
