package hudson.plugins.locale;

import com.thoughtworks.xstream.XStream;
import hudson.Plugin;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.util.PluginServletFilter;
import hudson.util.XStream2;
import net.sf.json.JSONObject;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Kohsuke Kawaguchi
 */
public class PluginImpl extends Plugin {

    private String systemLocale;

    private boolean ignoreAcceptLanguage;

    /**
     * The value of {@link Locale#getDefault()} before we replace it.
     */
    private transient final Locale originalLocale = Locale.getDefault();

    @Override
    public void start() throws Exception {
        load();
        LocaleProvider.setProvider(new LocaleProvider() {
            LocaleProvider original = LocaleProvider.getProvider();
            public Locale get() {
                if(ignoreAcceptLanguage)
                    return Locale.getDefault();
                return original.get();
            }
        });

        PluginServletFilter.addFilter(new LocaleFilter());
    }

    protected void load() throws IOException {
        super.load();
        setSystemLocale(systemLocale);  // make the loaded value take effect
    }

    protected XmlFile getConfigXml() {
        return new XmlFile(XSTREAM, new File(Hudson.getInstance().getRootDir(),"locale.xml"));
    }

    public String getDescription() {
        return Messages.Description();
    }

    public String getDefaultLanguage() {
        return Messages.Default_Language();
    }

    @Override
    public void configure(StaplerRequest req, JSONObject jsonObject) throws IOException, ServletException, FormException {
        setSystemLocale(jsonObject.getString("systemLocale"));
        ignoreAcceptLanguage = jsonObject.getBoolean("ignoreAcceptLanguage");
        save();
    }

    public boolean isIgnoreAcceptLanguage() {
        return ignoreAcceptLanguage;
    }

    public String getSystemLocale() {
        return systemLocale;
    }

    public void setSystemLocale(String systemLocale) throws IOException {
        systemLocale = Util.fixEmptyAndTrim(systemLocale);
        Locale.setDefault(systemLocale==null ? originalLocale : parse(systemLocale));
        this.systemLocale = systemLocale;
    }

    /**
     * Parses a string like "ja_JP" into a {@link Locale} object.
     */
    public static Locale parse(String s) {
        String[] tokens = s.trim().split("_");
        switch (tokens.length) {
        case 1: return new Locale(tokens[0]);
        case 2: return new Locale(tokens[0],tokens[1]);
        case 3: return new Locale(tokens[0],tokens[1],tokens[2]);
        default:
            throw new IllegalArgumentException(s+" is not a valid locale");
        }
    }

    private static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.alias("locale",PluginImpl.class);
    }
}
