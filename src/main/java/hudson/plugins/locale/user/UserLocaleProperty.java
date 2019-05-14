package hudson.plugins.locale.user;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.plugins.locale.Messages;
import net.sf.json.JSONObject;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

public class UserLocaleProperty extends UserProperty {
    private String userLocale;

    public UserLocaleProperty(String userLocale) {
        this.userLocale = userLocale;
    }

    public String getUserLocale() {
        return userLocale;
    }

    @DataBoundSetter
    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }

    @Extension
    public static final class DescriptorImpl extends UserPropertyDescriptor {
        public String getDisplayName() {
            return Messages.userLocale();
        }

        public UserProperty newInstance(User user) {
            return new UserLocaleProperty(LocaleProvider.getLocale().toString());
        }

        @Override
        public UserProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new UserLocaleProperty(formData.optString("userLocale"));
        }
    }
}
