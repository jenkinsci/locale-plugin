package hudson.plugins.locale;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.User;
import hudson.plugins.locale.user.UserLocaleProperty;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class LocaleFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // nop
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req) {
            PluginImpl plugin = PluginImpl.get();
            final Locale locale;

            if (plugin == null) {
                chain.doFilter(request, response);
                return;
            } else if (plugin.isAllowUserPreferences()) {
                locale = getCurrentUserLocale();
            } else if (plugin.isIgnoreAcceptLanguage()) {
                locale = Locale.getDefault();
            } else {
                locale = null;
            }

            if (locale != null) {
                request = new HttpServletRequestWrapper(req) {
                    @Override
                    public Locale getLocale() {
                        // Force locale to configured default, ignore requests Accept-Language header
                        return locale;
                    }

                    @Override
                    public Enumeration<Locale> getLocales() {
                        // Create a custom Enumeration with only the default locale
                        return new Enumeration<>() {
                            private boolean hasMoreElements = true;

                            @Override
                            public boolean hasMoreElements() {
                                return hasMoreElements;
                            }

                            @Override
                            public Locale nextElement() {
                                if (hasMoreElements) {
                                    hasMoreElements = false;
                                    return getLocale();
                                } else {
                                    throw new NoSuchElementException();
                                }
                            }
                        };
                    }
                };
                ((HttpServletResponse) response).addHeader("X-Jenkins-Language", locale.toString());
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nop
    }

    @CheckForNull
    private Locale getCurrentUserLocale() {
        User user = User.current();
        if (user != null) {
            UserLocaleProperty userLocaleProperty = user.getProperty(UserLocaleProperty.class);
            if (userLocaleProperty == null) {
                return null;
            }
            return userLocaleProperty.getLocale();
        }
        return null;
    }
}
