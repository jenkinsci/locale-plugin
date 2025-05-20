package hudson.plugins.locale;

import com.thoughtworks.xstream.XStream;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.ListBoxModel;
import hudson.util.PluginServletFilter;
import hudson.util.XStream2;
import jakarta.servlet.ServletException;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import jenkins.appearance.AppearanceCategory;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.LocaleUtils;
import org.jenkinsci.Symbol;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
@Symbol("locale")
public class PluginImpl extends GlobalConfiguration {

    private static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.alias("locale", PluginImpl.class);
    }

    private String systemLocale;
    private boolean ignoreAcceptLanguage;
    private boolean allowUserPreferences;

    public static final String USE_BROWSER_LOCALE = "USE_BROWSER_LOCALE";

    // Set of allowed locales
    public static final Set<String> ALLOWED_LOCALES = Set.of(
            "bg", "ca", "cs", "da", "de", "el", "en", "es", "es_AR", "et", "fi", "fr", "he", "hu", "it", "ja", "ko",
            "lt", "lv", "nb_NO", "nl", "pl", "pt_BR", "pt_PT", "ro", "ru", "sk", "sl", "sr", "sv", "tr", "uk", "zh_CN",
            "zh_TW");

    /**
     * The value of {@link Locale#getDefault()} before we replace it.
     */
    private final transient Locale originalLocale = Locale.getDefault();

    public static PluginImpl get() {
        return Jenkins.get().getExtensionList(PluginImpl.class).get(0);
    }

    public PluginImpl() {
        load();
    }

    @Override
    protected XmlFile getConfigFile() {
        return new XmlFile(XSTREAM, new File(Jenkins.get().getRootDir(), "locale.xml")); // for backward compatibility
    }

    @Initializer(after = InitMilestone.EXTENSIONS_AUGMENTED)
    public static void init() throws Exception {
        PluginImpl.get().start();
    }

    private void start() throws ServletException {
        load();
        LocaleProvider.setProvider(new LocaleProvider() {
            final LocaleProvider original = LocaleProvider.getProvider();

            @Override
            public Locale get() {
                if (ignoreAcceptLanguage) {
                    return Locale.getDefault();
                }
                return original.get();
            }
        });

        PluginServletFilter.addFilter(new LocaleFilter());
    }

    @Override
    public void load() {
        super.load();
        // make the loaded value take effect
        if (systemLocale == null || systemLocale.isEmpty()) {
            setSystemLocale(USE_BROWSER_LOCALE);
        } else {
            setSystemLocale(systemLocale);
        }
    }

    @Override
    public boolean configure(StaplerRequest2 req, JSONObject jsonObject) {
        req.bindJSON(this, jsonObject);
        save();
        return false;
    }

    public boolean isIgnoreAcceptLanguage() {
        return ignoreAcceptLanguage;
    }

    public boolean isAllowUserPreferences() {
        return allowUserPreferences;
    }

    public String getSystemLocale() {
        return systemLocale;
    }

    public void setSystemLocale(String systemLocale) {
        systemLocale = Util.fixEmptyAndTrim(systemLocale);
        if (USE_BROWSER_LOCALE.equals(systemLocale)) {
            Locale.setDefault(originalLocale);
            this.systemLocale = USE_BROWSER_LOCALE;
        } else {
            Locale.setDefault((systemLocale == null || systemLocale.isEmpty()) ? originalLocale : parse(systemLocale));
            this.systemLocale = systemLocale;
        }
    }

    /**
     * Sets whether the plugin should take user preferences into account.
     * @param ignoreAcceptLanguage If {@code true},
     *      ignore browser preference and force this language to all users
     * @since 1.3
     */
    public void setIgnoreAcceptLanguage(boolean ignoreAcceptLanguage) {
        this.ignoreAcceptLanguage = ignoreAcceptLanguage;
    }

    /**
     * Sets whether the plugin should take user preferences into account.
     * @param allowUserPreferences If {@code true},
     *      ignore browser preference and use the language a user configured
     */
    public void setAllowUserPreferences(boolean allowUserPreferences) {
        this.allowUserPreferences = allowUserPreferences;
    }

    /**
     * Parses a string like "ja_JP" into a {@link Locale} object.
     *
     * @param s the locale string using underscores as delimiters
     * @return the Locale object
     */
    public static Locale parse(String s) {
        // TODO: Migrate to Locale.of() once we upgrade to Java 21
        return LocaleUtils.toLocale(s.trim());
    }

    @NonNull
    @Override
    public GlobalConfigurationCategory getCategory() {
        return GlobalConfigurationCategory.get(AppearanceCategory.class);
    }

    /**
     * Retrieves a ListBoxModel containing the available system locales.
     * This method populates a ListBoxModel with the available system locales,
     * sorted lexicographically by their string representations. Each locale's
     * display name and string representation are added as options to the model.
     *
     * @return A ListBoxModel containing the available system locales.
     */
    @RequirePOST
    public ListBoxModel doFillSystemLocaleItems() {
        ListBoxModel items = new ListBoxModel();

        // Use originalLocale to display the "Use Default Locale" option
        String originalLocaleDisplay =
                String.format("Use Default Locale - %s (%s)", originalLocale.getDisplayName(), originalLocale);
        items.add(new ListBoxModel.Option(originalLocaleDisplay, USE_BROWSER_LOCALE));

        Locale[] availableLocales = Locale.getAvailableLocales();
        List<Locale> sortedLocales = Arrays.stream(availableLocales)
                .filter(locale -> ALLOWED_LOCALES.contains(locale.toString())) // Ensure no empty or null locale strings
                .sorted(Comparator.comparing(Locale::getDisplayName))
                .toList();

        for (Locale locale : sortedLocales) {
            String displayText = String.format("%s - %s", locale.getDisplayName(), locale);
            items.add(new ListBoxModel.Option(displayText, locale.toString()));
        }

        return items;
    }
}
