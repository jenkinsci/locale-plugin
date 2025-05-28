package hudson.plugins.locale.user;

import static hudson.plugins.locale.PluginImpl.ALLOWED_LOCALES;
import static hudson.plugins.locale.PluginImpl.USE_BROWSER_LOCALE;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.model.userproperty.UserPropertyCategory;
import hudson.plugins.locale.Messages;
import hudson.plugins.locale.PluginImpl;
import hudson.util.ListBoxModel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class UserLocaleProperty extends UserProperty {
    private static final Logger LOGGER = Logger.getLogger(UserLocaleProperty.class.getName());

    private String localeCode;
    private Locale locale;

    @DataBoundConstructor
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
        try {
            locale = PluginImpl.parse(localeCode);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, ex, () -> "Unable to determine locale for locale code " + localeCode);
        }
        this.localeCode = localeCode;
    }

    @Extension
    @Symbol("userLocale")
    public static final class DescriptorImpl extends UserPropertyDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.locale();
        }

        public UserProperty newInstance(User user) {
            return new UserLocaleProperty(LocaleProvider.getLocale().toString());
        }

        @Override
        public UserProperty newInstance(StaplerRequest2 req, @NonNull JSONObject formData) {
            return new UserLocaleProperty(formData.optString("localeCode"));
        }

        @Override
        public boolean isEnabled() {
            PluginImpl plugin = PluginImpl.get();
            return plugin.isAllowUserPreferences();
        }

        @Override
        public @NonNull UserPropertyCategory getUserPropertyCategory() {
            return UserPropertyCategory.get(UserPropertyCategory.Appearance.class);
        }

        /**
         * Retrieves a ListBoxModel containing the available user locales.
         * This method populates a ListBoxModel with the available user locales,
         * sorted lexicographically by their string representations. Each locale's
         * display name and string representation are added as options to the model.
         *
         * @return A ListBoxModel containing the available user locales.
         */
        @RequirePOST
        public ListBoxModel doFillLocaleCodeItems() {
            ListBoxModel items = new ListBoxModel();
            Locale originalLocale = LocaleProvider.getLocale();

            // Use originalLocale to display the "Use Default Locale" option
            String originalLocaleDisplay =
                    String.format("Use Default Locale - %s (%s)", originalLocale.getDisplayName(), originalLocale);
            items.add(new ListBoxModel.Option(originalLocaleDisplay, USE_BROWSER_LOCALE));

            Locale[] availableLocales = Locale.getAvailableLocales();
            List<Locale> sortedLocales = Arrays.stream(availableLocales)
                    .filter(locale ->
                            ALLOWED_LOCALES.contains(locale.toString())) // Ensure no empty or null locale strings
                    .sorted(Comparator.comparing(Locale::getDisplayName))
                    .toList();

            for (Locale locale : sortedLocales) {
                String displayText = String.format("%s - %s", locale.getDisplayName(), locale);
                items.add(new ListBoxModel.Option(displayText, locale.toString()));
            }

            return items;
        }
    }
}
