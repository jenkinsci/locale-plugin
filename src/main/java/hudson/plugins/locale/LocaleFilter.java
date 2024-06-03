package hudson.plugins.locale;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.NoSuchElementException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class LocaleFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nop
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            PluginImpl plugin = PluginImpl.get();
            if (plugin != null && plugin.isIgnoreAcceptLanguage()) {
                request = new HttpServletRequestWrapper((HttpServletRequest) request) {
                    @Override
                    public Locale getLocale() {
                        // Force locale to configured default, ignore request' Accept-Language header
                        return Locale.getDefault();
                    }

                    @Override
                    public Enumeration<Locale> getLocales() {
                        // Create a custom Enumeration with only the default locale
                        return new Enumeration<Locale>() {
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
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nop
    }
}
