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
    private String locale;

    public UserLocaleProperty(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    @DataBoundSetter
    public void setLocale(String locale) {
        this.locale = locale;
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
