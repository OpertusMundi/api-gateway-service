package eu.opertusmundi.web.zuul.filter;

import static com.netflix.zuul.context.RequestContext.getCurrentContext;
import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerMapping;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

// For examples see:
//
// https://github.com/spring-cloud-samples/sample-zuul-filters/tree/master/src/main/java/org/springframework/cloud/samplezuulfilters

public class CatalogueResponseBodyFilter extends ZuulFilter
{
    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 999;
    }

    @Override
    public boolean shouldFilter() {
        final RequestContext context = getCurrentContext();
        return context.getRequest().getRequestURI().startsWith("/action/catalogue");
    }

    @Override
    public Object run() {
        try {
            final RequestContext context = getCurrentContext();
            final InputStream    stream  = context.getResponseDataStream();
            final String         body    = StreamUtils.copyToString(stream, Charset.forName("UTF-8"));

            final String match = (String) context.getRequest().getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

            // TODO: Rewrite response.

            switch (match) {
                default :
                    break;
            }

            context.setResponseBody(body);
        } catch (final IOException e) {
            rethrowRuntimeException(e);
        }
        return null;
    }

}