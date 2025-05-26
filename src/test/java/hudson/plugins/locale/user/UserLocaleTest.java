package hudson.plugins.locale.user;

import static hudson.plugins.locale.PluginImpl.ALLOWED_LOCALES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Item;
import hudson.model.User;
import hudson.plugins.locale.PluginImpl;
import hudson.util.ListBoxModel;
import java.util.Locale;
import java.util.Map;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.localizer.LocaleProvider;

@WithJenkins
class UserLocaleTest {

    private JenkinsRule j;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        j = rule;
    }

    @Test
    void testDifferentUsers() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy authorizationStrategy = new MockAuthorizationStrategy();
        authorizationStrategy.grant(Jenkins.READ).onRoot().toEveryone();
        authorizationStrategy.grant(Item.READ).everywhere().to("bob");
        authorizationStrategy.grant(Item.READ).everywhere().to("alice");
        j.jenkins.setAuthorizationStrategy(authorizationStrategy);

        PluginImpl plugin = PluginImpl.get();
        plugin.setAllowUserPreferences(true);

        // test for zh_CN user
        User userBob = User.get("bob", true, Map.of());
        UserLocaleProperty userLocaleProperty = userBob.getProperty(UserLocaleProperty.class);
        userLocaleProperty.setLocaleCode("zh_CN");

        try (JenkinsRule.WebClient wc = j.createWebClient().login("bob")) {
            String language = wc.goTo("", "text/html").getWebResponse().getResponseHeaderValue("X-Jenkins-Language");
            assertEquals("zh_CN", language);
        }

        // test for en user
        User userAlice = User.get("alice", true, Map.of());
        userLocaleProperty = userAlice.getProperty(UserLocaleProperty.class);
        userLocaleProperty.setLocaleCode("en");

        try (JenkinsRule.WebClient wc = j.createWebClient().login("alice")) {
            String language = wc.goTo("", "text/html").getWebResponse().getResponseHeaderValue("X-Jenkins-Language");
            assertEquals("en", language);
        }
    }

    @Test
    void testNonExistentLocaleCode() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy authorizationStrategy = new MockAuthorizationStrategy();
        authorizationStrategy.grant(Jenkins.READ).onRoot().toEveryone();
        authorizationStrategy.grant(Item.READ).everywhere().to("bob");
        j.jenkins.setAuthorizationStrategy(authorizationStrategy);

        PluginImpl plugin = PluginImpl.get();
        plugin.setAllowUserPreferences(true);

        // test for non-existent locale code
        User userBob = User.get("bob", true, Map.of());
        UserLocaleProperty userLocaleProperty = userBob.getProperty(UserLocaleProperty.class);
        userLocaleProperty.setLocaleCode("doesnotexist");

        try (JenkinsRule.WebClient wc = j.createWebClient().login("bob")) {
            String language = wc.goTo("", "text/html").getWebResponse().getResponseHeaderValue("X-Jenkins-Language");
            assertNull(language);
        }
    }

    @Test
    void testNoLocaleCode() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy authorizationStrategy = new MockAuthorizationStrategy();
        authorizationStrategy.grant(Jenkins.READ).onRoot().toEveryone();
        authorizationStrategy.grant(Item.READ).everywhere().to("bob");
        j.jenkins.setAuthorizationStrategy(authorizationStrategy);

        PluginImpl plugin = PluginImpl.get();
        plugin.setAllowUserPreferences(true);

        // test for no locale code
        try (JenkinsRule.WebClient wc = j.createWebClient().login("bob")) {
            String language = wc.goTo("", "text/html").getWebResponse().getResponseHeaderValue("X-Jenkins-Language");
            assertNull(language);
        }
    }

    @Test
    void testDoFillLocaleCodeItems() {
        UserLocaleProperty.DescriptorImpl descriptor = new UserLocaleProperty.DescriptorImpl();

        // Invoke the method
        ListBoxModel model = descriptor.doFillLocaleCodeItems();

        // Expected size of the ListBoxModel
        int expectedSize = ALLOWED_LOCALES.size() + 1; // +1 for the "Use Default Locale" option

        // Verify the returned ListBoxModel size
        assertEquals(expectedSize, model.size(), "The returned ListBoxModel size is not as expected");

        // Verify that the first option is "Use Default Locale"
        String expectedFirstOption = String.format(
                "Use Default Locale - %s (%s)",
                LocaleProvider.getLocale().getDisplayName(),
                LocaleProvider.getLocale().toString());
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
    void testRoundTrip() throws Throwable {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy authorizationStrategy = new MockAuthorizationStrategy();
        authorizationStrategy.grant(Jenkins.READ).onRoot().toEveryone();
        authorizationStrategy.grant(Item.READ).everywhere().to("bob");
        j.jenkins.setAuthorizationStrategy(authorizationStrategy);

        PluginImpl plugin = PluginImpl.get();
        plugin.setAllowUserPreferences(true);

        // test locale code is saved and loaded correctly
        User userBob = User.get("bob", true, Map.of());
        UserLocaleProperty userLocaleProperty = userBob.getProperty(UserLocaleProperty.class);
        userLocaleProperty.setLocaleCode("de");
        userBob.save();

        j.restart();

        userBob = User.get("bob", false, Map.of());
        userLocaleProperty = userBob.getProperty(UserLocaleProperty.class);
        assertEquals("de", userLocaleProperty.getLocaleCode());
    }
}
