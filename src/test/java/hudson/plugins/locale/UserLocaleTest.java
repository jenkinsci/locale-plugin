package hudson.plugins.locale;

import hudson.model.Item;
import hudson.model.User;
import hudson.plugins.locale.user.UserLocaleProperty;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;

import static org.junit.Assert.assertEquals;

public class UserLocaleTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void differentUsers() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy authorizationStrategy = new MockAuthorizationStrategy();
        authorizationStrategy.grant(Jenkins.READ).onRoot().toEveryone();
        authorizationStrategy.grant(Item.READ).everywhere().to("bob");
        authorizationStrategy.grant(Item.READ).everywhere().to("alice");
        j.jenkins.setAuthorizationStrategy(authorizationStrategy);

        PluginImpl plugin = (PluginImpl) j.jenkins.getPlugin("locale");
        plugin.setUserPrefer(true);

        // test for zh_CN user
        User userBob = User.get("bob", true, null);
        UserLocaleProperty userLocaleProperty = userBob.getProperty(UserLocaleProperty.class);
        userLocaleProperty.setLocaleCode("zh_CN");

        JenkinsRule.WebClient wc = j.createWebClient().login("bob");
        String language = wc.goTo("", "text/html")
                .getWebResponse().getResponseHeaderValue("X-Jenkins-Language");
        assertEquals("zh_CN", language);

        // test for en user
        User userAlice = User.get("alice", true, null);
        userLocaleProperty = userAlice.getProperty(UserLocaleProperty.class);
        userLocaleProperty.setLocaleCode("en");

        wc = j.createWebClient().login("alice");
        language = wc.goTo("", "text/html")
                .getWebResponse().getResponseHeaderValue("X-Jenkins-Language");
        assertEquals("en", language);
    }
}
