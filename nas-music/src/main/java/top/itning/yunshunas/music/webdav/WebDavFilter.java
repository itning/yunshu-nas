package top.itning.yunshunas.music.webdav;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author itning
 * @since 2024/9/24 17:16
 */
public class WebDavFilter extends HttpFilter {
    private static final String HEADER_DAV = "DAV";
    private static final String HEADER_MS_AUTHOR_VIA = "MS-Author-Via";
    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_ALLOW_VALUE = "OPTIONS, HEAD, GET, PROPFIND";
    private static final String WEBDAV_DEFAULT_XML_NAMESPACE = "d";
    private static final String WEBDAV_DEFAULT_XML_NAMESPACE_URI = "DAV:";
    private static final String XML_ALLPROP = "allprop";
    private static final String XML_CREATIONDATE = "creationdate";
    private static final String XML_DISPLAYNAME = "displayname";
    private static final String XML_EXCLUSIVE = "exclusive";
    private static final String XML_GETCONTENTLENGTH = "getcontentlength";
    private static final String XML_GETCONTENTTYPE = "getcontenttype";
    private static final String XML_GETETAG = "getetag";
    private static final String XML_GETLASTMODIFIED = "getlastmodified";
    private static final String XML_HREF = "href";
    private static final String XML_ISARCHIVE = "isarchive";
    private static final String XML_ISCOLLECTION = "iscollection";
    private static final String XML_ISHIDDEN = "ishidden";
    private static final String XML_ISREADONLY = "isreadonly";
    private static final String XML_ISSYSTEM = "issystem";
    private static final String XML_LOCKENTRY = "lockentry";
    private static final String XML_LOCKSCOPE = "lockscope";
    private static final String XML_LOCKTYPE = "locktype";
    private static final String XML_MULTISTATUS = "multistatus";
    private static final String XML_PROP = "prop";
    private static final String XML_PROPNAME = "propname";
    private static final String XML_PROPSTAT = "propstat";
    private static final String XML_RESPONSE = "response";
    private static final String XML_STATUS = "status";
    private static final String XML_SUPPORTEDLOCK = "supportedlock";
    private static final String XML_WIN32FILEATTRIBUTES = "Win32FileAttributes";
    private static final String XML_WRITE = "write";

    private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT");
    private static final String DATETIME_FORMAT_CREATION_DATE = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String DATETIME_FORMAT_LAST_MODIFIED = "E, dd MMM yyyy HH:mm:ss z";

    /**
     * Constant for PROPFIND to display all properties
     */
    private static final int WEBDAV_FIND_ALL_PROP = 1;
    /**
     * Constant for PROPFIND to specify a property mask
     */
    private static final int WEBDAV_FIND_BY_PROPERTY = 0;
    /**
     * Constant for PROPFIND to find property names
     */
    private static final int WEBDAV_FIND_PROPERTY_NAMES = 2;


    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String method = request.getMethod().toUpperCase();
        switch (method) {
            case "OPTIONS" -> {
                response.setHeader(HEADER_DAV, "1, 2");
                response.setHeader(HEADER_MS_AUTHOR_VIA, "DAV");
                response.setHeader(HEADER_ALLOW, HEADER_ALLOW_VALUE);
                response.setStatus(HttpStatus.OK.value());
            }
            case "PROPFIND" -> {
                try {
                    final Document document = this.readXmlRequest(request);
                    try (final ServletOutputStream servletOutputStream = response.getOutputStream();
                         final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                         final XmlWriter xmlWriter = new XmlWriter(buffer)) {
                        xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE_URI, XML_MULTISTATUS, XmlWriter.ElementType.OPENING);
                        if (Objects.nonNull(document)) {
                            final Element root = document.getDocumentElement();
                            final NodeList nodeList = root.getChildNodes();
                            final Set<Node> nodeSet = IntStream.range(0, nodeList.getLength())
                                    .mapToObj(nodeList::item)
                                    .filter(streamNode -> streamNode.getNodeType() == Node.ELEMENT_NODE)
                                    .collect(Collectors.toSet());

                            if (nodeSet.stream().anyMatch(streamNode -> streamNode.getLocalName().equalsIgnoreCase(XML_ALLPROP))) {
                                collectProperties(xmlWriter, WEBDAV_FIND_ALL_PROP, new Properties());

                            } else if (nodeSet.stream().anyMatch(streamNode -> streamNode.getLocalName().equalsIgnoreCase(XML_PROP))) {
                                final Node propNode = nodeSet.stream()
                                        .filter(streamNode -> streamNode.getLocalName().equalsIgnoreCase(XML_PROP))
                                        .findFirst()
                                        .orElse(null);
                                if (null != propNode) {
                                    Properties propertiesFromNode = getPropertiesFromNode(propNode);
                                    collectProperties(xmlWriter, WEBDAV_FIND_BY_PROPERTY, propertiesFromNode);
                                }
                            } else if (nodeSet.stream().anyMatch(streamNode -> streamNode.getLocalName().equalsIgnoreCase(XML_PROPNAME))) {
                                collectProperties(xmlWriter, WEBDAV_FIND_PROPERTY_NAMES, new Properties());

                            } else {
                                collectProperties(xmlWriter, WEBDAV_FIND_ALL_PROP, new Properties());

                            }
                        } else {
                            collectProperties(xmlWriter, WEBDAV_FIND_ALL_PROP, new Properties());
                        }

                        xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_MULTISTATUS, XmlWriter.ElementType.CLOSING);
                        xmlWriter.flush();
                        response.setContentLength(buffer.size());
                        servletOutputStream.write(buffer.toByteArray());
                    }
                    response.setStatus(HttpStatus.MULTI_STATUS.value());
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.setContentType(MediaType.TEXT_XML_VALUE);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
            // HEAD/GET
            default -> chain.doFilter(request, response);
        }
    }

    private Document readXmlRequest(final HttpServletRequest request) throws IOException, SAXException, ParserConfigurationException {
        if (request.getContentLength() <= 0) {
            return null;
        }
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(request.getInputStream());
    }

    private Properties getPropertiesFromNode(final Node node) {
        final Properties properties = new Properties();
        final NodeList nodeList = node.getChildNodes();
        IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item)
                .filter(streamNode -> streamNode.getNodeType() == Node.ELEMENT_NODE)
                .forEach(streamNode -> {
                    String streamNodeValue = streamNode.getNodeValue();
                    if (Objects.isNull(streamNodeValue)) {
                        streamNodeValue = streamNode.getTextContent();
                    }
                    properties.put(streamNode.getLocalName(), streamNodeValue);
                });
        return properties;
    }

    private void collectProperties(XmlWriter xmlWriter, int type, Properties properties) throws IOException {
        xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_RESPONSE, XmlWriter.ElementType.OPENING);
        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_HREF, UriUtils.encodePath("/" + "hello", StandardCharsets.UTF_8.name()));

        final String displayName = UriUtils.encodePath("entry.getName()", StandardCharsets.UTF_8.name());

        final String creationDate = formatDate(new Date(), DATETIME_FORMAT_CREATION_DATE);
        final String lastModified = formatDate(new Date(), DATETIME_FORMAT_LAST_MODIFIED);
        final String contentType = "text/plain";
        final String contentLength = "10240";

        final String isCollection = String.valueOf(false);
        final String isReadOnly = String.valueOf(true);
        final String isHidden = String.valueOf(false);
        final String isSystem = Boolean.FALSE.toString();
        final String isArchive = Boolean.FALSE.toString();

        final String etag = "\"" + (Long.toString(new Date().getTime(), 16)) + "\"";

        // Win32FileAttributes
        // see also https://docs.microsoft.com/de-de/windows/win32/api/fileapi/nf-fileapi-setfileattributesa?redirectedfrom=MSDN
        // readOnly: 0x01, hidden: 0x02, system: 0x04, directory: 0x10, archive: 0x20
        final String win32FileAttributes = Integer.toHexString((0x01) | (0));

        switch (type) {
            case WEBDAV_FIND_ALL_PROP -> {
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.OPENING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.OPENING);

                xmlWriter.writePropertyData(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_DISPLAYNAME, displayName);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISCOLLECTION, isCollection);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_CREATIONDATE, creationDate);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETLASTMODIFIED, lastModified);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISREADONLY, isReadOnly);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISHIDDEN, isHidden);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISSYSTEM, isSystem);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISARCHIVE, isArchive);

                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_WIN32FILEATTRIBUTES, win32FileAttributes);

                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTTYPE, contentType);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTLENGTH, contentLength);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETETAG, etag);

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_SUPPORTEDLOCK, XmlWriter.ElementType.OPENING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, "DAV:", XML_LOCKENTRY, XmlWriter.ElementType.OPENING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_LOCKSCOPE, XmlWriter.ElementType.OPENING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_EXCLUSIVE, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_LOCKSCOPE, XmlWriter.ElementType.CLOSING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_LOCKTYPE, XmlWriter.ElementType.OPENING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_WRITE, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_LOCKTYPE, XmlWriter.ElementType.CLOSING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_LOCKENTRY, XmlWriter.ElementType.CLOSING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_SUPPORTEDLOCK, XmlWriter.ElementType.CLOSING);

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.CLOSING);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_STATUS, "HTTP/1.1 200 Success");
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.CLOSING);
            }
            case WEBDAV_FIND_PROPERTY_NAMES -> {
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.OPENING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.OPENING);

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_DISPLAYNAME, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISCOLLECTION, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_CREATIONDATE, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETLASTMODIFIED, XmlWriter.ElementType.EMPTY);

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISREADONLY, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISHIDDEN, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISSYSTEM, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISARCHIVE, XmlWriter.ElementType.EMPTY);

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_WIN32FILEATTRIBUTES, XmlWriter.ElementType.EMPTY);

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTTYPE, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTLENGTH, XmlWriter.ElementType.EMPTY);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETETAG, XmlWriter.ElementType.EMPTY);

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.CLOSING);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_STATUS, "HTTP/1.1 200 Success");
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.CLOSING);
            }
            case WEBDAV_FIND_BY_PROPERTY -> {
                List<String> list = new ArrayList<>();

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.OPENING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.OPENING);

                for (String property : properties.keySet()) {

                    switch (property) {
                        case XML_DISPLAYNAME ->
                                xmlWriter.writePropertyData(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_DISPLAYNAME, displayName);
                        case XML_ISCOLLECTION ->
                                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISCOLLECTION, isCollection);
                        case XML_CREATIONDATE ->
                                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_CREATIONDATE, creationDate);
                        case XML_GETLASTMODIFIED ->
                                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETLASTMODIFIED, lastModified);
                        case XML_ISREADONLY ->
                                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISREADONLY, isReadOnly);
                        case XML_ISHIDDEN ->
                                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISHIDDEN, isHidden);
                        case XML_ISSYSTEM ->
                                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISSYSTEM, isSystem);
                        case XML_ISARCHIVE ->
                                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISARCHIVE, isArchive);
                        case XML_WIN32FILEATTRIBUTES ->
                                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_WIN32FILEATTRIBUTES, win32FileAttributes);
                        default -> {
                            switch (property) {
                                case XML_GETCONTENTLENGTH ->
                                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTLENGTH, contentLength);
                                case XML_GETCONTENTTYPE ->
                                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTTYPE, contentType);
                                case XML_GETETAG ->
                                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETETAG, etag);
                                default -> list.add(property);
                            }
                        }
                    }
                }

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.CLOSING);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_STATUS, "HTTP/1.1 200 Success");
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.CLOSING);

                Iterator<String> iterator = list.iterator();
                if (iterator.hasNext()) {
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.OPENING);
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.OPENING);

                    while (iterator.hasNext())
                        xmlWriter.writeElement(null, iterator.next(), XmlWriter.ElementType.EMPTY);

                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.CLOSING);
                    xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_STATUS, "HTTP/1.1 200 Success");
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.CLOSING);
                }

            }
        }
        xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_RESPONSE, XmlWriter.ElementType.CLOSING);
    }

    private String formatDate(final Date date, final String format) {
        final SimpleDateFormat pattern = new SimpleDateFormat(format, Locale.US);
        pattern.setTimeZone(DEFAULT_TIME_ZONE);
        return pattern.format(date);
    }
}
