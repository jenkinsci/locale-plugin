package hudson.plugins.locale;

import jenkins.model.Jenkins;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigurationAsCodeTest {

    @Rule public JenkinsConfiguredWithCodeRule r = new JenkinsConfiguredWithCodeRule();

    @Test
    @ConfiguredWithCode("configuration-as-code.yml")
    public void should_support_configuration_as_code() throws Exception {
        PluginImpl plugin = Jenkins.get().getExtensionList(PluginImpl.class).get(0);
        assertEquals("fr", plugin.getSystemLocale());
        assertEquals(true, plugin.isIgnoreAcceptLanguage());
    }
}
