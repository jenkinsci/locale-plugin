package hudson.plugins.locale;

import com.thoughtworks.xstream.XStream;
import hudson.Extension;
import hudson.Util;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.PluginServletFilter;
import hudson.util.XStream2;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import jenkins.model.Jenkins;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
@Symbol("locale")
public class PluginImpl extends GlobalConfiguration {

    private String systemLocale;
    private boolean ignoreAcceptLanguage;

    /**
     * The value of {@link Locale#getDefault()} before we replace it.
     */
    private transient final Locale originalLocale = Locale.getDefault();

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
        setSystemLocale(systemLocale);  // make the loaded value take effect
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
        Locale.setDefault(systemLocale == null ? originalLocale : parse(systemLocale));
        this.systemLocale = systemLocale;
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

    private static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.alias("locale", PluginImpl.class);
    }
}
