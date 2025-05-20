package hudson.plugins.locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.User;
import hudson.plugins.locale.user.UserLocaleProperty;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.misc.junit.jupiter.WithJenkinsConfiguredWithCode;
import java.util.Map;
import org.junit.jupiter.api.Test;

@WithJenkinsConfiguredWithCode
class ConfigurationAsCodeTest {

    @Test
    @ConfiguredWithCode("configuration-as-code.yml")
    void should_support_configuration_as_code(JenkinsConfiguredWithCodeRule r) {
        PluginImpl plugin = PluginImpl.get();
        assertEquals("fr", plugin.getSystemLocale());
        assertFalse(plugin.isIgnoreAcceptLanguage());
        assertTrue(plugin.isAllowUserPreferences());

        User user = User.get("admin", false, Map.of());
        assertNotNull(user);
        UserLocaleProperty property = user.getProperty(UserLocaleProperty.class);
        assertNotNull(property);
        assertEquals("de", property.getLocaleCode());
    }
}
