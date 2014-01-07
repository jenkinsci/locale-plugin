package hudson.plugins.locale;

import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

public class UserLocaleProperty extends UserProperty {

    private String localeStr;

    private transient Locale locale;

    @DataBoundConstructor
    public UserLocaleProperty(String localeStr) {
        setLocaleStr(localeStr);
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
        this.locale = LocaleUtils.toLocale(localeStr);
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public Locale getLocale() {
        return locale;
    }

    private Object readResolve() {
        locale = LocaleUtils.toLocale(localeStr);
        return this;
    }

    @Extension
    public static final class DescriptorImpl extends UserPropertyDescriptor {

        @Override
        public UserProperty newInstance(User user) {
            return new UserLocaleProperty(null);
        }

        @Override
        public String getDisplayName() {
            return "Locale";
        }
    }
}
