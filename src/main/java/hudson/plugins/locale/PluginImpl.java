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
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import jenkins.appearance.AppearanceCategory;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
@Symbol("locale")
public class PluginImpl extends GlobalConfiguration {

    private String systemLocale;
    private boolean ignoreAcceptLanguage;

    public static final String USE_BROWSER_LOCALE = "USE_BROWSER_LOCALE";

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
            LocaleProvider original = LocaleProvider.getProvider();

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
        setSystemLocale(USE_BROWSER_LOCALE);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject jsonObject) throws FormException {
        req.bindJSON(this, jsonObject);
        save();
        return false;
    }

    public boolean isIgnoreAcceptLanguage() {
        return ignoreAcceptLanguage;
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

    public String getUseBrowserLocale() {
        return USE_BROWSER_LOCALE;
    }

    /**
     * Sets whether the plugin should take user preferences into account.
     * @param ignoreAcceptLanguage If {@code true},
     *      Ignore browser preference and force this language to all users
     * @since 1.3
     */
    public void setIgnoreAcceptLanguage(boolean ignoreAcceptLanguage) {
        this.ignoreAcceptLanguage = ignoreAcceptLanguage;
    }

    /**
     * Parses a string like "ja_JP" into a {@link Locale} object.
     *
     * @param s the locale string using underscores as delimiters
     * @return the Locale object
     */
    public static Locale parse(String s) {
        String[] tokens = s.trim().split("_");
        switch (tokens.length) {
            case 1:
                return new Locale(tokens[0]);
            case 2:
                return new Locale(tokens[0], tokens[1]);
            case 3:
                return new Locale(tokens[0], tokens[1], tokens[2]);
            default:
                throw new IllegalArgumentException(s + " is not a valid locale");
        }
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
    public ListBoxModel doFillSystemLocaleItems() {
        ListBoxModel items = new ListBoxModel();

        // Use originalLocale to display the "Use Browser Locale" option
        String originalLocaleDisplay = String.format("Use Browser Locale - %s (%s)",
                originalLocale.getDisplayName(),
                originalLocale.toString());
        items.add(new ListBoxModel.Option(originalLocaleDisplay, USE_BROWSER_LOCALE));

        Locale[] availableLocales = Locale.getAvailableLocales();
        List<Locale> sortedLocales = Arrays.stream(availableLocales)
                .filter(locale -> locale != null && !locale.toString().isEmpty()) // Ensure no empty or null locale strings
                .sorted((locale1, locale2) -> locale1.toString().compareTo(locale2.toString()))
                .collect(Collectors.toList());

        for (Locale locale : sortedLocales) {
            String displayText = String.format("%s - %s", locale.getDisplayName(), locale.toString());
            items.add(new ListBoxModel.Option(displayText, locale.toString()));
        }

        return items;
    }



    private static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.alias("locale", PluginImpl.class);
    }
}
