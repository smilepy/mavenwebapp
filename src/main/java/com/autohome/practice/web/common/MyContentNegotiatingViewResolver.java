package com.autohome.practice.web.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created with IntelliJ IDEA.
 * User: shizhizhong
 * Date: 14-1-21
 * Time: 上午11:41
 * To change this template use File | Settings | File Templates.
 */
public class MyContentNegotiatingViewResolver extends WebApplicationObjectSupport implements ViewResolver, Ordered {

    private static final Log logger = LogFactory.getLog(MyContentNegotiatingViewResolver.class);

    private static final String ACCEPT_HEADER = "Accept";

    private static final boolean jafPresent =
            ClassUtils.isPresent("javax.activation.FileTypeMap", MyContentNegotiatingViewResolver.class.getClassLoader());

    private static final UrlPathHelper urlPathHelper = new UrlPathHelper();


    private int order = Ordered.HIGHEST_PRECEDENCE;

    private boolean favorPathExtension = true;

    private boolean favorParameter = false;

    private String parameterName = "format";

    private boolean useNotAcceptableStatusCode = false;

    private boolean ignoreAcceptHeader = false;

    private boolean useJaf = true;

    private ConcurrentMap<String, MediaType> mediaTypes = new ConcurrentHashMap<String, MediaType>();

    private List<View> defaultViews;

    private MediaType defaultContentType;

    private List<ViewResolver> viewResolvers;

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    /**
     * Indicates whether the extension of the request path should be used to determine the requested media type,
     * in favor of looking at the {@code Accept} header. The default value is {@code true}.
     * <p>For instance, when this flag is <code>true</code> (the default), a request for {@code /hotels.pdf}
     * will result in an {@code AbstractPdfView} being resolved, while the {@code Accept} header can be the
     * browser-defined {@code text/html,application/xhtml+xml}.
     */
    public void setFavorPathExtension(boolean favorPathExtension) {
        this.favorPathExtension = favorPathExtension;
    }

    /**
     * Indicates whether a request parameter should be used to determine the requested media type,
     * in favor of looking at the {@code Accept} header. The default value is {@code false}.
     * <p>For instance, when this flag is <code>true</code>, a request for {@code /hotels?format=pdf} will result
     * in an {@code AbstractPdfView} being resolved, while the {@code Accept} header can be the browser-defined
     * {@code text/html,application/xhtml+xml}.
     */
    public void setFavorParameter(boolean favorParameter) {
        this.favorParameter = favorParameter;
    }

    /**
     * Sets the parameter name that can be used to determine the requested media type if the {@link
     * #setFavorParameter(boolean)} property is {@code true}. The default parameter name is {@code format}.
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Indicates whether the HTTP {@code Accept} header should be ignored. Default is {@code false}.
     * If set to {@code true}, this view resolver will only refer to the file extension and/or paramter,
     * as indicated by the {@link #setFavorPathExtension(boolean) favorPathExtension} and
     * {@link #setFavorParameter(boolean) favorParameter} properties.
     */
    public void setIgnoreAcceptHeader(boolean ignoreAcceptHeader) {
        this.ignoreAcceptHeader = ignoreAcceptHeader;
    }

    /**
     * Indicates whether a {@link javax.servlet.http.HttpServletResponse#SC_NOT_ACCEPTABLE 406 Not Acceptable} status code should be
     * returned if no suitable view can be found.
     *
     * <p>Default is {@code false}, meaning that this view resolver returns {@code null} for
     * {@link #resolveViewName(String, Locale)} when an acceptable view cannot be found. This will allow for view
     * resolvers chaining. When this property is set to {@code true},
     * {@link #resolveViewName(String, Locale)} will respond with a view that sets the response status to
     * {@code 406 Not Acceptable} instead.
     */
    public void setUseNotAcceptableStatusCode(boolean useNotAcceptableStatusCode) {
        this.useNotAcceptableStatusCode = useNotAcceptableStatusCode;
    }

    /**
     * Sets the mapping from file extensions to media types.
     * <p>When this mapping is not set or when an extension is not present, this view resolver
     * will fall back to using a {@link FileTypeMap} when the Java Action Framework is available.
     */
    public void setMediaTypes(Map<String, String> mediaTypes) {
        Assert.notNull(mediaTypes, "'mediaTypes' must not be null");
        for (Map.Entry<String, String> entry : mediaTypes.entrySet()) {
            String extension = entry.getKey().toLowerCase(Locale.ENGLISH);
            MediaType mediaType = MediaType.parseMediaType(entry.getValue());
            this.mediaTypes.put(extension, mediaType);
        }
    }

    /**
     * Sets the default views to use when a more specific view can not be obtained
     * from the {@link org.springframework.web.servlet.ViewResolver} chain.
     */
    public void setDefaultViews(List<View> defaultViews) {
        this.defaultViews = defaultViews;
    }

    /**
     * Sets the default content type.
     * <p>This content type will be used when file extension, parameter, nor {@code Accept}
     * header define a content-type, either through being disabled or empty.
     */
    public void setDefaultContentType(MediaType defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    /**
     * Indicates whether to use the Java Activation Framework to map from file extensions to media types.
     * <p>Default is {@code true}, i.e. the Java Activation Framework is used (if available).
     */
    public void setUseJaf(boolean useJaf) {
        this.useJaf = useJaf;
    }

    /**
     * Sets the view resolvers to be wrapped by this view resolver.
     * <p>If this property is not set, view resolvers will be detected automatically.
     */
    public void setViewResolvers(List<ViewResolver> viewResolvers) {
        this.viewResolvers = viewResolvers;
    }


    @Override
    protected void initServletContext(ServletContext servletContext) {
        if (this.viewResolvers == null) {
            Map<String, ViewResolver> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), ViewResolver.class);
            this.viewResolvers = new ArrayList<ViewResolver>(matchingBeans.size());
            for (ViewResolver viewResolver : matchingBeans.values()) {
                if (this != viewResolver) {
                    this.viewResolvers.add(viewResolver);
                }
            }
        }
        if (this.viewResolvers.isEmpty()) {
            logger.warn("Did not find any ViewResolvers to delegate to; please configure them using the " +
                    "'viewResolvers' property on the MyContentNegotiatingViewResolver");
        }
        OrderComparator.sort(this.viewResolvers);
    }

    /**
     * Determines the list of {@link org.springframework.http.MediaType} for the given {@link javax.servlet.http.HttpServletRequest}.
     * <p>The default implementation invokes {@link #getMediaTypeFromFilename(String)} if {@linkplain
     * #setFavorPathExtension(boolean) favorPathExtension} property is <code>true</code>. If the property is
     * <code>false</code>, or when a media type cannot be determined from the request path, this method will
     * inspect the {@code Accept} header of the request.
     * <p>This method can be overriden to provide a different algorithm.
     * @param request the current servlet request
     * @return the list of media types requested, if any
     */
    protected List<MediaType> getMediaTypes(HttpServletRequest request) {
        if (this.favorPathExtension) {
            String requestUri = urlPathHelper.getRequestUri(request);
            String filename = WebUtils.extractFullFilenameFromUrlPath(requestUri);
            MediaType mediaType = getMediaTypeFromFilename(filename);
            if (mediaType != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Requested media type is '" + mediaType + "' (based on filename '" + filename + "')");
                }
                return Collections.singletonList(mediaType);
            }
        }
        if (this.favorParameter) {
            if (request.getParameter(this.parameterName) != null) {
                String parameterValue = request.getParameter(this.parameterName);
                MediaType mediaType = getMediaTypeFromParameter(parameterValue);
                if (mediaType != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Requested media type is '" + mediaType + "' (based on parameter '" +
                                this.parameterName + "'='" + parameterValue + "')");
                    }
                    return Collections.singletonList(mediaType);
                }
            }
        }
        if (!this.ignoreAcceptHeader) {
            String acceptHeader = request.getHeader(ACCEPT_HEADER);
            if (StringUtils.hasText(acceptHeader)) {
                List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader);
                MediaType.sortByQualityValue(mediaTypes);
                if (logger.isDebugEnabled()) {
                    logger.debug("Requested media types are " + mediaTypes + " (based on Accept header)");
                }
                return mediaTypes;
            }
        }
        if (this.defaultContentType != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Requested media types is " + defaultContentType + " (based on defaultContentType property)");
            }
            return Collections.singletonList(this.defaultContentType);
        }
        else {
            return Collections.emptyList();
        }
    }

    /**
     * Determines the {@link org.springframework.http.MediaType} for the given filename.
     * <p>The default implementation will check the {@linkplain #setMediaTypes(Map) media types} property first for a
     * defined mapping. If not present, and if the Java Activation Framework can be found on the class path, it will call
     * {@link FileTypeMap#getContentType(String)}
     * <p>This method can be overriden to provide a different algorithm.
     * @param filename the current request file name (i.e. {@code hotels.html})
     * @return the media type, if any
     */
    protected MediaType getMediaTypeFromFilename(String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        if (!StringUtils.hasText(extension)) {
            return null;
        }
        extension = extension.toLowerCase(Locale.ENGLISH);
        MediaType mediaType = this.mediaTypes.get(extension);
        if (mediaType == null && useJaf && jafPresent) {
            mediaType = ActivationMediaTypeFactory.getMediaType(filename);
            if (mediaType != null) {
                this.mediaTypes.putIfAbsent(extension, mediaType);
            }
        }
        return mediaType;
    }

    /**
     * Determines the {@link org.springframework.http.MediaType} for the given parameter value.
     * <p>The default implementation will check the {@linkplain #setMediaTypes(Map) media types}
     * property for a defined mapping.
     * <p>This method can be overriden to provide a different algorithm.
     * @param parameterValue the parameter value (i.e. {@code pdf}).
     * @return the media type, if any
     */
    protected MediaType getMediaTypeFromParameter(String parameterValue) {
        return this.mediaTypes.get(parameterValue.toLowerCase(Locale.ENGLISH));
    }

    public View resolveViewName(String viewName, Locale locale) throws Exception {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        Assert.isInstanceOf(ServletRequestAttributes.class, attrs);

        List<MediaType> requestedMediaTypes = getMediaTypes(((ServletRequestAttributes) attrs).getRequest());

        List<View> candidateViews = getCandidateViews(viewName, locale, requestedMediaTypes);

        View bestView = getBestView(candidateViews, requestedMediaTypes);

        if (bestView != null) {
            return bestView;
        }
        else {
            if (useNotAcceptableStatusCode) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No acceptable view found; returning 406 (Not Acceptable) status code");
                }
                return NOT_ACCEPTABLE_VIEW;
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No acceptable view found; returning null");
                }
                return null;
            }
        }
    }

    private List<View> getCandidateViews(String viewName, Locale locale, List<MediaType> requestedMediaTypes)
            throws Exception {
        List<View> candidateViews = new ArrayList<View>();

        //如是json请求则跳过vm resolver
        if( requestedMediaTypes != null && requestedMediaTypes.size() == 1
                &&requestedMediaTypes.get(0).getSubtype()!=null
                && "json".equals(requestedMediaTypes.get(0).getSubtype().toLowerCase()) ){

        }else{
            for (ViewResolver viewResolver : this.viewResolvers) {
                View view = viewResolver.resolveViewName(viewName, locale);
                if (view != null) {
                    candidateViews.add(view);
                }
    //            for (MediaType requestedMediaType : requestedMediaTypes) {
    //                List<String> extensions = getExtensionsForMediaType(requestedMediaType);
    //                for (String extension : extensions) {
    //                    String viewNameWithExtension = viewName + "." + extension;
    //                    view = viewResolver.resolveViewName(viewNameWithExtension, locale);
    //                    if (view != null) {
    //                        candidateViews.add(view);
    //                    }
    //                }
    //
    //            }
            }
        }

        if (!CollectionUtils.isEmpty(this.defaultViews)) {
            candidateViews.addAll(this.defaultViews);
        }
        return candidateViews;
    }

    private List<String> getExtensionsForMediaType(MediaType requestedMediaType) {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, MediaType> entry : mediaTypes.entrySet()) {
            if (requestedMediaType.includes(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private View getBestView(List<View> candidateViews, List<MediaType> requestedMediaTypes) {
        MediaType bestRequestedMediaType = null;
        View bestView = null;
        for (MediaType requestedMediaType : requestedMediaTypes) {
            for (View candidateView : candidateViews) {
                if (StringUtils.hasText(candidateView.getContentType())) {
                    MediaType candidateContentType = MediaType.parseMediaType(candidateView.getContentType());
                    if (requestedMediaType.includes(candidateContentType)) {
                        bestRequestedMediaType = requestedMediaType;
                        bestView = candidateView;
                        break;
                    }
                }
            }
            if (bestView != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Returning [" + bestView + "] based on requested media type '" + bestRequestedMediaType +
                                    "'");
                }
                break;
            }
        }
        return bestView;

    }

    /**
     * Inner class to avoid hard-coded JAF dependency.
     */
    private static class ActivationMediaTypeFactory {

        private static final FileTypeMap fileTypeMap;

        static {
            fileTypeMap = loadFileTypeMapFromContextSupportModule();
        }

        private static FileTypeMap loadFileTypeMapFromContextSupportModule() {
            // see if we can find the extended mime.types from the context-support module
            Resource mappingLocation = new ClassPathResource("org/springframework/mail/javamail/mime.types");
            if (mappingLocation.exists()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Loading Java Activation Framework FileTypeMap from " + mappingLocation);
                }
                InputStream inputStream = null;
                try {
                    inputStream = mappingLocation.getInputStream();
                    return new MimetypesFileTypeMap(inputStream);
                }
                catch (IOException ex) {
                    // ignore
                }
                finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (IOException ex) {
                            // ignore
                        }
                    }
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Loading default Java Activation Framework FileTypeMap");
            }
            return FileTypeMap.getDefaultFileTypeMap();
        }

        public static MediaType getMediaType(String fileName) {
            String mediaType = fileTypeMap.getContentType(fileName);
            return StringUtils.hasText(mediaType) ? MediaType.parseMediaType(mediaType) : null;
        }
    }

    private static final View NOT_ACCEPTABLE_VIEW = new View() {

        public String getContentType() {
            return null;
        }

        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
                throws Exception {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
    };
}
