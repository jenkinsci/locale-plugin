package hudson.plugins.locale.user;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.plugins.locale.Messages;
import hudson.plugins.locale.PluginImpl;
import net.sf.json.JSONObject;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Locale;

public class UserLocaleProperty extends UserProperty {
    private String localeCode;

    private Locale locale; // for the cache purpose

    @Override
    public UserProperty reconfigure(StaplerRequest req, JSONObject form) throws Descriptor.FormException {
        UserProperty userProperty = super.reconfigure(req, form);
        if (userProperty instanceof UserLocaleProperty) {
            try {
                locale = PluginImpl.parse(((UserLocaleProperty) userProperty).getLocaleCode());
            } catch (IllegalArgumentException e) {
                // ignore this exception
            }
        }
        return userProperty;
    }

    public UserLocaleProperty(String localeCode) {
        this.localeCode = localeCode;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLocaleCode() {
        return localeCode;
    }

    @DataBoundSetter
    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    @Extension
    public static final class DescriptorImpl extends UserPropertyDescriptor {
        public String getDisplayName() {
            return Messages.locale();
        }

        public UserProperty newInstance(User user) {
            return new UserLocaleProperty(LocaleProvider.getLocale().toString());
        }

        @Override
        public UserProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new UserLocaleProperty(formData.optString("locale"));
        }
    }
}
