package top.itning.yunshunas.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpHeaders.*;

/**
 * https://stackoverflow.com/questions/28427339/how-to-implement-http-byte-range-requests-in-spring-mvc
 *
 * @author itning
 * @since 2020/9/13 0:44
 */
@SuppressWarnings("all")
public final class MultipartFileSender {
    private static final Logger logger = LoggerFactory.getLogger(MultipartFileSender.class);

    private static final int DEFAULT_BUFFER_SIZE = 20480; // ..bytes = 20KB.
    private static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1 week.
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    private Path filepath;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String contentType;
    private boolean enable304StatusReturn = true;

    public MultipartFileSender() {
    }

    public static MultipartFileSender fromPath(Path path) {
        return new MultipartFileSender().setFilepath(path);
    }

    public static MultipartFileSender fromFile(File file) {
        return new MultipartFileSender().setFilepath(file.toPath());
    }

    public static MultipartFileSender fromURIString(String uri) {
        return new MultipartFileSender().setFilepath(Paths.get(uri));
    }

    //** internal setter **//
    private MultipartFileSender setFilepath(Path filepath) {
        this.filepath = filepath;
        return this;
    }

    public MultipartFileSender setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public MultipartFileSender with(HttpServletRequest httpRequest) {
        request = httpRequest;
        return this;
    }

    public MultipartFileSender with(HttpServletResponse httpResponse) {
        response = httpResponse;
        return this;
    }

    public MultipartFileSender no304CodeReturn() {
        enable304StatusReturn = false;
        return this;
    }

    public void serveResource() throws Exception {
        if (response == null || request == null) {
            return;
        }

        if (!Files.exists(filepath)) {
            logger.warn("File doesn't exist at URI : {}", filepath.toAbsolutePath().toString());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long length = Files.size(filepath);
        String fileName = filepath.getFileName().toString();
        FileTime lastModifiedObj = Files.getLastModifiedTime(filepath);

        if (fileName == null || "".equals(fileName)) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        long lastModified = LocalDateTime.ofInstant(lastModifiedObj.toInstant(), ZoneId.of(ZoneOffset.systemDefault().getId())).toEpochSecond(ZoneOffset.UTC);

        if (null == contentType) {
            contentType = Files.probeContentType(filepath);
        }

        // Validate request headers for caching ---------------------------------------------------

        if (enable304StatusReturn) {
            // If-None-Match header should contain "*" or ETag. If so, then return 304.
            String ifNoneMatch = request.getHeader(IF_NONE_MATCH);
            if (ifNoneMatch != null && HttpUtils.matches(ifNoneMatch, fileName)) {
                response.setHeader(ETAG, fileName); // Required in 304.
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            // If-Modified-Since header should be greater than LastModified. If so, then return 304.
            // This header is ignored if any If-None-Match header is specified.
            long ifModifiedSince = request.getDateHeader(IF_MODIFIED_SINCE);
            if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
                response.setHeader(ETAG, fileName); // Required in 304.
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            // Validate request headers for resume ----------------------------------------------------

            // If-Match header should contain "*" or ETag. If not, then return 412.
            String ifMatch = request.getHeader(IF_MATCH);
            if (ifMatch != null && !HttpUtils.matches(ifMatch, fileName)) {
                response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }

            // If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
            long ifUnmodifiedSince = request.getDateHeader(IF_UNMODIFIED_SINCE);
            if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
                response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }
        }

        // Validate and process range -------------------------------------------------------------

        // Prepare some variables. The full Range represents the complete file.
        Range full = new Range(0, length - 1, length);
        List<Range> ranges = new ArrayList<>();

        // Validate and process Range and If-Range headers.
        String range = request.getHeader(RANGE);
        if (range != null) {

            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                response.setHeader(CONTENT_RANGE, "bytes */" + length); // Required in 416.
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            String ifRange = request.getHeader(IF_RANGE);
            if (ifRange != null && !ifRange.equals(fileName)) {
                try {
                    long ifRangeTime = request.getDateHeader(IF_RANGE); // Throws IAE if invalid.
                    if (ifRangeTime != -1) {
                        ranges.add(full);
                    }
                } catch (IllegalArgumentException ignore) {
                    ranges.add(full);
                }
            }

            // If any valid If-Range header, then process each part of byte range.
            if (ranges.isEmpty()) {
                for (String part : range.substring(6).split(",")) {
                    // Assuming a file with length of 100, the following examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                    long start = Range.sublong(part, 0, part.indexOf("-"));
                    long end = Range.sublong(part, part.indexOf("-") + 1, part.length());

                    if (start == -1) {
                        start = length - end;
                        end = length - 1;
                    } else if (end == -1 || end > length - 1) {
                        end = length - 1;
                    }

                    // Check if Range is syntactically valid. If not, then return 416.
                    if (start > end) {
                        response.setHeader(CONTENT_RANGE, "bytes */" + length); // Required in 416.
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }

                    // Add range.
                    ranges.add(new Range(start, end, length));
                }
            }
        }

        // Prepare and initialize response --------------------------------------------------------

        // Get content type by file name and set content disposition.
        String disposition = "inline";

        // If content type is unknown, then set the default value.
        // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
        // To add new content types, add new mime-mapping entry in web.xml.
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } else if (!contentType.startsWith("image")) {
            // Else, expect for images, determine content disposition. If content type is supported by
            // the browser, then set to inline, else attachment which will pop a 'save as' dialogue.
            String accept = request.getHeader(ACCEPT);
            disposition = accept != null && HttpUtils.accepts(accept, contentType) ? "inline" : "attachment";
        }
        logger.debug("Content-Type : {}", contentType);
        // Initialize response.
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader(CONTENT_TYPE, contentType);
        response.setHeader(CONTENT_DISPOSITION, disposition + ";filename=\"" + fileName + "\"");
        logger.debug("Content-Disposition : {}", disposition);
        response.setHeader(ACCEPT_RANGES, "bytes");
        if (enable304StatusReturn) {
            response.setHeader(ETAG, fileName);
            response.setDateHeader(LAST_MODIFIED, lastModified);
            response.setDateHeader(EXPIRES, System.currentTimeMillis() + DEFAULT_EXPIRE_TIME);
        }
        String origin = request.getHeader(ORIGIN);
        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, "POST,GET,OPTIONS,DELETE,PUT,PATCH");
        response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS));
        response.setIntHeader(ACCESS_CONTROL_MAX_AGE, 2592000);

        // Send requested file (part(s)) to client ------------------------------------------------

        // Prepare streams.
        try (RandomAccessFile input = new RandomAccessFile(filepath.toFile(), "r");
             ServletOutputStream output = response.getOutputStream()) {

            if (ranges.isEmpty() || ranges.get(0) == full) {

                // Return full file.
                logger.info("Return full file");
                response.setContentType(contentType);
                response.setHeader(CONTENT_RANGE, "bytes " + full.start + "-" + full.end + "/" + full.total);
                response.setHeader(CONTENT_LENGTH, String.valueOf(full.length));
                Range.copy(input, output, length, full.start, full.length);

            } else if (ranges.size() == 1) {

                // Return single part of file.
                Range r = ranges.get(0);
                logger.info("Return 1 part of file : from ({}) to ({}) total ({})", r.start, r.end, length);
                response.setContentType(contentType);
                response.setHeader(CONTENT_RANGE, "bytes " + r.start + "-" + r.end + "/" + r.total);
                response.setHeader(CONTENT_LENGTH, String.valueOf(r.length));
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                // Copy single part range.
                Range.copy(input, output, length, r.start, r.length);

            } else {

                // Return multiple parts of file.
                response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                // Copy multi part range.
                for (Range r : ranges) {
                    logger.info("Return multi part of file : from ({}) to ({}) total ({})", r.start, r.end, length);
                    // Add multipart boundary and header fields for every range.
                    output.println();
                    output.println("--" + MULTIPART_BOUNDARY);
                    output.println(CONTENT_TYPE + ": " + contentType);
                    output.println(CONTENT_RANGE + ": bytes " + r.start + "-" + r.end + "/" + r.total);

                    // Copy single part range of multi part range.
                    Range.copy(input, output, length, r.start, r.length);
                }

                // End with multipart boundary.
                output.println();
                output.println("--" + MULTIPART_BOUNDARY + "--");
            }
        }

    }

    private static class Range {
        long start;
        long end;
        long length;
        long total;

        /**
         * Construct a byte range.
         *
         * @param start Start of the byte range.
         * @param end   End of the byte range.
         * @param total Total length of the byte source.
         */
        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }

        public static long sublong(String value, int beginIndex, int endIndex) {
            String substring = value.substring(beginIndex, endIndex);
            return (substring.length() > 0) ? Long.parseLong(substring) : -1;
        }

        private static void copy(RandomAccessFile input, OutputStream output, long inputSize, long start, long length) throws IOException {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;

            try {
                if (inputSize == length) {
                    // Write full range.
                    while ((read = input.read(buffer)) > 0) {
                        output.write(buffer, 0, read);
                        output.flush();
                    }
                } else {
                    input.seek(start);
                    long toRead = length;

                    while ((read = input.read(buffer)) > 0) {
                        if ((toRead -= read) > 0) {
                            output.write(buffer, 0, read);
                            output.flush();
                        } else {
                            output.write(buffer, 0, (int) toRead + read);
                            output.flush();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                logger.warn("copy failed. start:{} end:{} total:{} {}", start, length, inputSize, e.getMessage());
                throw e;
            }
        }
    }

    private static class HttpUtils {

        /**
         * Returns true if the given accept header accepts the given value.
         *
         * @param acceptHeader The accept header.
         * @param toAccept     The value to be accepted.
         * @return True if the given accept header accepts the given value.
         */
        public static boolean accepts(String acceptHeader, String toAccept) {
            String[] acceptValues = acceptHeader.split("\\s*([,;])\\s*");
            Arrays.sort(acceptValues);

            return Arrays.binarySearch(acceptValues, toAccept) > -1
                    || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
                    || Arrays.binarySearch(acceptValues, "*/*") > -1;
        }

        /**
         * Returns true if the given match header matches the given value.
         *
         * @param matchHeader The match header.
         * @param toMatch     The value to be matched.
         * @return True if the given match header matches the given value.
         */
        public static boolean matches(String matchHeader, String toMatch) {
            String[] matchValues = matchHeader.split("\\s*,\\s*");
            Arrays.sort(matchValues);
            return Arrays.binarySearch(matchValues, toMatch) > -1
                    || Arrays.binarySearch(matchValues, "*") > -1;
        }
    }
}
