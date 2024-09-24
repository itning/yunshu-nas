package top.itning.yunshunas.music.webdav;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import top.itning.yunshunas.common.util.FileNameValidator;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.repository.MusicRepository;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author itning
 * @since 2024/9/24 17:16
 */
@Slf4j
public class WebDavFilter extends HttpFilter {
    private static final String HEADER_DAV = "DAV";
    private static final String HEADER_MS_AUTHOR_VIA = "MS-Author-Via";
    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_ALLOW_VALUE = "OPTIONS, HEAD, GET, PROPFIND";
    private static final String HEADER_DEPTH = "Depth";
    private static final String WEBDAV_DEFAULT_XML_NAMESPACE = "d";
    private static final String WEBDAV_DEFAULT_XML_NAMESPACE_URI = "DAV:";
    /**
     * Constant for max. depth with infinite processing depth
     * Caution: More than 1 works, but Windows Explorer seems to have problems
     * and then displays everything deeper 1 in the same folder level.
     */
    private static final int WEBDAV_INFINITY = 1;
    private static final String XML_ALLPROP = "allprop";
    private static final String XML_COLLECTION = "collection";
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
    private static final String XML_RESOURCETYPE = "resourcetype";
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


    private final MusicRepository musicRepository;
    private final MusicDataSource musicDataSource;
    private final PathPatternParser pathPatternParser;
    private final List<String> filterUrlPatternMappings = new ArrayList<>();

    public WebDavFilter(MusicRepository musicRepository, MusicDataSource musicDataSource, PathPatternParser pathPatternParser) {
        this.musicRepository = musicRepository;
        this.musicDataSource = musicDataSource;
        this.pathPatternParser = pathPatternParser;
    }

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
                Optional.ofNullable(getFilterConfig().getServletContext().getFilterRegistration(getFilterConfig().getFilterName()))
                        .ifPresent(filterRegistration -> filterUrlPatternMappings.addAll(filterRegistration.getUrlPatternMappings()));
                if (filterUrlPatternMappings.stream().anyMatch(urlPatternMapping -> !urlPatternMapping.matches("^/(\\w+([\\w\\-]+\\w){0,1}/){0,}(\\*){0,}$"))) {
                    throw new ServletException("Invalid URL pattern mapping for filter: " + this.getClass().getName());
                }
                final String contextPath = locateRequestContextPath(request);
                Entry entry;
                String requestURI = request.getRequestURI();
                if (requestURI.endsWith("/")) {
                    requestURI = requestURI.substring(0, requestURI.length() - 1);
                }
                if (!contextPath.equalsIgnoreCase(requestURI)) {
                    PathPattern.PathMatchInfo matchInfo = getPathMatchInfo(request);
                    if (null == matchInfo) {
                        response.setStatus(HttpStatus.NOT_FOUND.value());
                        return;
                    }
                    Map<String, String> uriVariables = matchInfo.getUriVariables();
                    String name = uriVariables.get("name");
                    String single = uriVariables.get("single");
                    String ext = uriVariables.get("ext");
                    String id = uriVariables.get("id");
                    log.debug("matchInfo from url: {}", uriVariables);
                    if (null != id) {
                        Optional<Music> musicOptional = musicRepository.findByMusicId(id);
                        if (musicOptional.isEmpty()) {
                            response.setStatus(HttpStatus.NOT_FOUND.value());
                            return;
                        }
                        Music music = musicOptional.get();
                        entry = getMusicFile(music);
                    } else {
                        Optional<MusicType> musicTypeOptional = MusicType.getFromExt(ext);
                        if (musicTypeOptional.isEmpty()) {
                            response.setStatus(HttpStatus.NOT_FOUND.value());
                            return;
                        }
                        Optional<Music> musicOptional = musicRepository.findByNameAndSingerAndType(name, single, musicTypeOptional.get().getType());
                        if (musicOptional.isEmpty()) {
                            response.setStatus(HttpStatus.NOT_FOUND.value());
                            return;
                        }
                        Music music = musicOptional.get();
                        entry = getMusicFile(music);
                    }
                } else {
                    entry = getMusicEntry();
                }


                try {
                    final Document document = this.readXmlRequest(request);
                    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    try (final XmlWriter xmlWriter = new XmlWriter(buffer)) {
                        xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE_URI, XML_MULTISTATUS, XmlWriter.ElementType.OPENING);
                        final int depth = getDepth(request);

                        if (Objects.nonNull(document)) {
                            final Element root = document.getDocumentElement();
                            final NodeList nodeList = root.getChildNodes();
                            final Set<Node> nodeSet = IntStream.range(0, nodeList.getLength())
                                    .mapToObj(nodeList::item)
                                    .filter(streamNode -> streamNode.getNodeType() == Node.ELEMENT_NODE)
                                    .collect(Collectors.toSet());

                            if (nodeSet.stream().anyMatch(streamNode -> streamNode.getLocalName().equalsIgnoreCase(XML_ALLPROP))) {
                                collectProperties(xmlWriter, contextPath, entry, WEBDAV_FIND_ALL_PROP, new Properties(), depth);

                            } else if (nodeSet.stream().anyMatch(streamNode -> streamNode.getLocalName().equalsIgnoreCase(XML_PROP))) {
                                final Node propNode = nodeSet.stream()
                                        .filter(streamNode -> streamNode.getLocalName().equalsIgnoreCase(XML_PROP))
                                        .findFirst()
                                        .orElse(null);
                                if (null != propNode) {
                                    Properties propertiesFromNode = getPropertiesFromNode(propNode);
                                    collectProperties(xmlWriter, contextPath, entry, WEBDAV_FIND_BY_PROPERTY, propertiesFromNode, depth);
                                }
                            } else if (nodeSet.stream().anyMatch(streamNode -> streamNode.getLocalName().equalsIgnoreCase(XML_PROPNAME))) {
                                collectProperties(xmlWriter, contextPath, entry, WEBDAV_FIND_PROPERTY_NAMES, new Properties(), depth);

                            } else {
                                collectProperties(xmlWriter, contextPath, entry, WEBDAV_FIND_ALL_PROP, new Properties(), depth);

                            }
                        } else {
                            collectProperties(xmlWriter, contextPath, entry, WEBDAV_FIND_ALL_PROP, new Properties(), depth);
                        }

                        xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_MULTISTATUS, XmlWriter.ElementType.CLOSING);
                        xmlWriter.flush();
                    }
                    response.setStatus(HttpStatus.MULTI_STATUS.value());
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.setContentType(MediaType.TEXT_XML_VALUE);
                    response.setContentLength(buffer.size());
                    response.getOutputStream().write(buffer.toByteArray());
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
            case "LOCK", "PUT", "UNLOCK", "PROPPATCH", "MKCOL", "COPY", "MOVE", "DELETE" -> {
                response.setStatus(HttpStatus.FORBIDDEN.value());
            }
            // HEAD/GET
            default -> chain.doFilter(request, response);
        }
    }

    private String locateRequestPath(final HttpServletRequest request) {
        final String contextPath = request.getContextPath();
        final String requestURI = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        if (requestURI.startsWith(contextPath + "/"))
            return requestURI.substring(contextPath.length());
        if (requestURI.equals(contextPath))
            return "/";
        return requestURI;
    }

    private String locateRequestContextPath(final HttpServletRequest request) {
        final String requestURI = this.locateRequestPath(request);
        for (String urlPatternMapping : this.filterUrlPatternMappings) {
            urlPatternMapping = urlPatternMapping.replaceAll("/+\\**$", "");
            if (requestURI.startsWith(urlPatternMapping + "/")
                    || requestURI.equals(urlPatternMapping))
                return urlPatternMapping;
        }
        return "";
    }

    private PathPattern.PathMatchInfo getPathMatchInfo(HttpServletRequest request) {
        final PathContainer pathContainer = PathContainer.parsePath(request.getRequestURI());
        PathPattern parse1 = pathPatternParser.parse(WebDavMusicController.NAME_SINGLE_EXT);
        PathPattern.PathMatchInfo matchInfo1 = parse1.matchAndExtract(pathContainer);
        if (null != matchInfo1) {
            return matchInfo1;
        }
        PathPattern parse2 = pathPatternParser.parse(WebDavMusicController.ID_ID_EXT);
        return parse2.matchAndExtract(pathContainer);
    }

    private Entry.File getMusicFile(Music music) {
        Entry.File file = new Entry.File();
        file.setContentLength(musicDataSource.getFileSize(music.getMusicId()));
        MusicType musicType = MusicType.getMediaTypeEnum(music.getType()).orElse(MusicType.MP3);
        file.setContentType(musicType.getMediaType());
        file.setLastModified(music.getGmtModified());
        file.setCreationDate(music.getGmtCreate());
        file.setReadOnly(true);
        file.setHidden(false);
        file.setPermitted(true);
        file.setName(music.getName() + " - " + music.getSinger() + "." + musicType.getExt());
        String fileName = StringEscapeUtils.escapeXml10(music.getName() + " - " + music.getSinger());
        if (FileNameValidator.isValidFileName(fileName)) {
            file.setPath("/" + fileName + "." + musicType.getExt());
        } else {
            log.warn("The song name or artist name does not comply with the file system name specification: [{}] music id: {}", fileName, music.getMusicId());
            file.setPath("/id_" + music.getMusicId() + "." + musicType.getExt());
        }
        file.setParent("/");
        return file;
    }

    private Entry.Folder getMusicEntry() {
        Entry.Folder folder = new Entry.Folder();
        folder.setParent("/");
        folder.setPath("/");
        folder.setName("");

        for (Music music : musicRepository.findAll()) {
            folder.getCollection().add(getMusicFile(music));
        }
        return folder;
    }

    private int getDepth(final HttpServletRequest request) {
        final String depth = request.getHeader(HEADER_DEPTH);
        if (Objects.isNull(depth) || !depth.matches("[01]")) {
            return WEBDAV_INFINITY;
        }
        return request.getIntHeader(HEADER_DEPTH);
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

    private void collectProperties(final XmlWriter xmlWriter, final String contextUrl, final Entry entry,
                                   final int type, final Properties properties, final int depth) throws IOException {

        collectProperties(xmlWriter, contextUrl, entry, type, properties);
        if (entry.isFile() || depth <= 0) {
            return;
        }
        for (Entry folderEntry : ((Entry.Folder) entry).getCollection()) {
            if (folderEntry.isHidden()) {
                continue;
            }
            collectProperties(xmlWriter, contextUrl, folderEntry, type, properties, depth - 1);
        }
    }

    private void collectProperties(final XmlWriter xmlWriter, final String contextUrl, final Entry entry,
                                   final int type, final Properties properties)
            throws IOException {

        xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_RESPONSE, XmlWriter.ElementType.OPENING);
        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_HREF, UriUtils.encodePath(contextUrl + entry.getPath(), StandardCharsets.UTF_8.name()));

        final String displayName = entry.getName();

        final String creationDate = Objects.nonNull(entry.getCreationDate()) ? formatDate(entry.getCreationDate(), DATETIME_FORMAT_CREATION_DATE) : null;
        final String lastModified = Objects.nonNull(entry.getLastModified()) ? formatDate(entry.getLastModified(), DATETIME_FORMAT_LAST_MODIFIED) : null;
        final String contentType = entry.isFile() && Objects.nonNull(((Entry.File) entry).getContentType()) ? ((Entry.File) entry).getContentType() : null;
        final String contentLength = entry.isFile() && Objects.nonNull(((Entry.File) entry).getContentLength()) ? ((Entry.File) entry).getContentLength().toString() : null;

        final String isCollection = String.valueOf(entry.isFolder());
        final String isReadOnly = String.valueOf(entry.isReadOnly());
        final String isHidden = String.valueOf(entry.isHidden());
        final String isSystem = Boolean.FALSE.toString();
        final String isArchive = Boolean.FALSE.toString();

        final String etag = Objects.nonNull(entry.getIdentifier()) ? "\"" + entry.getIdentifier() + "\"" : "";

        // Win32FileAttributes
        // see also https://docs.microsoft.com/de-de/windows/win32/api/fileapi/nf-fileapi-setfileattributesa?redirectedfrom=MSDN
        // readOnly: 0x01, hidden: 0x02, system: 0x04, directory: 0x10, archive: 0x20
        final String win32FileAttributes = Integer.toHexString(
                (entry.isReadOnly() ? 0x01 : 0)
                        | (entry.isHidden() ? 0x02 : 0)
                        | (entry.isFolder() ? 0x10 : 0));

        switch (type) {
            case WEBDAV_FIND_ALL_PROP:
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.OPENING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.OPENING);

                xmlWriter.writePropertyData(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_DISPLAYNAME, displayName);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISCOLLECTION, isCollection);
                if (Objects.nonNull(creationDate))
                    xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_CREATIONDATE, creationDate);
                if (Objects.nonNull(lastModified))
                    xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETLASTMODIFIED, lastModified);

                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISREADONLY, isReadOnly);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISHIDDEN, isHidden);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISSYSTEM, isSystem);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISARCHIVE, isArchive);

                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_WIN32FILEATTRIBUTES, win32FileAttributes);

                if (entry.isFolder()) {
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_RESOURCETYPE, XmlWriter.ElementType.OPENING);
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_COLLECTION, XmlWriter.ElementType.EMPTY);
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_RESOURCETYPE, XmlWriter.ElementType.CLOSING);
                } else {
                    if (Objects.nonNull(contentType))
                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTTYPE, contentType);
                    if (Objects.nonNull(contentLength))
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
                }

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.CLOSING);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_STATUS, "HTTP/1.1 200 Success");
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.CLOSING);

                break;

            case WEBDAV_FIND_PROPERTY_NAMES:

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

                if (entry.isFolder()) {
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_RESOURCETYPE, XmlWriter.ElementType.EMPTY);
                } else {
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTTYPE, XmlWriter.ElementType.EMPTY);
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTLENGTH, XmlWriter.ElementType.EMPTY);
                    xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETETAG, XmlWriter.ElementType.EMPTY);
                }

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.CLOSING);
                xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_STATUS, "HTTP/1.1 200 Success");
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.CLOSING);

                break;

            case WEBDAV_FIND_BY_PROPERTY:

                List<String> list = new ArrayList<>();

                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROPSTAT, XmlWriter.ElementType.OPENING);
                xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_PROP, XmlWriter.ElementType.OPENING);

                for (String property : properties.keySet()) {

                    if (property.equals(XML_DISPLAYNAME)) {
                        xmlWriter.writePropertyData(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_DISPLAYNAME, displayName);
                    } else if (property.equals(XML_ISCOLLECTION)) {
                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISCOLLECTION, isCollection);
                    } else if (property.equals(XML_CREATIONDATE)
                            && Objects.nonNull(creationDate)) {
                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_CREATIONDATE, creationDate);
                    } else if (property.equals(XML_GETLASTMODIFIED)
                            && Objects.nonNull(lastModified)) {
                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETLASTMODIFIED, lastModified);

                    } else if (property.equals(XML_ISREADONLY)) {
                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISREADONLY, isReadOnly);
                    } else if (property.equals(XML_ISHIDDEN)) {
                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISHIDDEN, isHidden);
                    } else if (property.equals(XML_ISSYSTEM)) {
                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISSYSTEM, isSystem);
                    } else if (property.equals(XML_ISARCHIVE)) {
                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_ISARCHIVE, isArchive);

                    } else if (property.equals(XML_WIN32FILEATTRIBUTES)) {
                        xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_WIN32FILEATTRIBUTES, win32FileAttributes);
                    } else {
                        if (entry.isFolder()
                                && property.equals(XML_RESOURCETYPE)) {
                            xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_RESOURCETYPE, XmlWriter.ElementType.OPENING);
                            xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_COLLECTION, XmlWriter.ElementType.EMPTY);
                            xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_RESOURCETYPE, XmlWriter.ElementType.CLOSING);
                        } else if (!entry.isFolder()
                                && property.equals(XML_GETCONTENTLENGTH)
                                && Objects.nonNull(contentLength)) {
                            xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTLENGTH, contentLength);
                        } else if (!entry.isFolder()
                                && property.equals(XML_GETCONTENTTYPE)
                                && Objects.nonNull(contentType)) {
                            xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETCONTENTTYPE, contentType);
                        } else if (!entry.isFolder()
                                && property.equals(XML_GETETAG)) {
                            xmlWriter.writeProperty(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_GETETAG, etag);
                        } else list.add(property);
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

                break;

            default:
        }

        xmlWriter.writeElement(WebDavFilter.WEBDAV_DEFAULT_XML_NAMESPACE, XML_RESPONSE, XmlWriter.ElementType.CLOSING);
    }

    private String formatDate(final Date date, final String format) {
        final SimpleDateFormat pattern = new SimpleDateFormat(format, Locale.US);
        pattern.setTimeZone(DEFAULT_TIME_ZONE);
        return pattern.format(date);
    }
}
