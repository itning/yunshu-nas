/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * WebDAV mapping for Spring Boot
 * Copyright (C) 2021 Seanox Software Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package top.itning.yunshunas.music.webdav;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Writer for the output of XML data.
 *
 * @author  Seanox Software Solutions
 * @version 1.1.0 20210708
 */
public class XmlWriter implements Closeable {

    /**
     * Output stream
     */
    private final OutputStream output;

    /**
     * Encoding to be used
     */
    private final Charset encoding;

    /**
     * Enumeration of element types
     */
    enum ElementType {

        /**
         * opening element
         */
        OPENING("<", ">"),

        /**
         * closing element
         */
        CLOSING("</", ">"),

        /**
         * Closing element without content
         */
        EMPTY("<", "/>");

        /**
         * Opening character sequence
         */
        private final String open;

        /**
         * Closing character sequence
         */
        private final String close;

        /**
         * Constructor, creates the ElementType.
         *
         * @param open  Opening character sequence
         * @param close Closing character sequence
         */
        ElementType(final String open, final String close) {
            this.open = open;
            this.close = close;
        }
    }

    /**
     * Constructor, creates the XML writer with UTF-8 encoding.
     * The XML header is immediately written to the output.
     *
     * @param output Output stream.
     * @throws IOException In case of faulty access to the data stream
     */
    XmlWriter(final OutputStream output)
            throws IOException {
        this(output, null);
    }

    /**
     * Constructor, creates the XML writer with the specified encoding.
     * The XML header is immediately written to the output.
     *
     * @param output   Output stream.
     * @param encoding Encoding to be used
     * @throws IOException In case of faulty access to the data stream
     */
    XmlWriter(final OutputStream output, final Charset encoding)
            throws IOException {

        this.output = output;
        this.encoding = Objects.isNull(encoding) ? StandardCharsets.UTF_8 : encoding;

        this.writeText(String.format("<?xml version=\"1.0\" encoding=\"%s\"?>", this.encoding.name()));
    }

    /**
     * Writes an XML parameter into the data stream.
     *
     * @param namespace Namespace
     * @param uri       Namespace URI
     * @param name      Element name
     * @param value     Element value
     * @throws IOException In case of faulty access to the data stream
     */
    void writeProperty(final String namespace, final String uri, final String name, final String value)
            throws IOException {

        this.writeElement(namespace, uri, name, ElementType.OPENING);
        this.writeText(value);
        this.writeElement(namespace, uri, name, ElementType.CLOSING);
    }

    /**
     * Writes an XML parameter into the data stream.
     *
     * @param namespace Namespace
     * @param name      Element name
     * @param value     Element value
     * @throws IOException In case of faulty access to the data stream
     */
    void writeProperty(final String namespace, final String name, final String value)
            throws IOException {

        this.writeElement(namespace, name, ElementType.OPENING);
        this.writeText(value);
        this.writeElement(namespace, name, ElementType.CLOSING);
    }

    /**
     * Writes an XML parameter as data segment into the data stream.
     *
     * @param namespace Namespace
     * @param name      Element name
     * @param value     Element value
     * @throws IOException In case of faulty access to the data stream
     */
    void writePropertyData(final String namespace, final String name, final String value)
            throws IOException {

        this.writeElement(namespace, name, ElementType.OPENING);
        this.writeText(String.format("<![CDATA[%s]]>", value));
        this.writeElement(namespace, name, ElementType.CLOSING);
    }

    /**
     * Writes an XML element into the data stream.
     *
     * @param namespace Namespace
     * @param name      Element name
     * @throws IOException In case of faulty access to the data stream
     */
    void writeProperty(final String namespace, final String name)
            throws IOException {
        this.writeElement(namespace, name, ElementType.EMPTY);
    }

    /**
     * Writes an XML element into the data stream.
     *
     * @param namespace Namespace
     * @param name      Element name
     * @param type      Element type
     * @throws IOException In case of faulty access to the data stream
     */
    void writeElement(final String namespace, final String name, final ElementType type)
            throws IOException {
        this.writeElement(namespace, null, name, type);
    }

    /**
     * Writes an XML element into the data stream.
     *
     * @param namespace Namespace
     * @param uri       Namespace URI
     * @param name      Element name
     * @param type      Element type
     * @throws IOException In case of faulty access to the data stream
     */
    void writeElement(final String namespace, final String uri, final String name, final ElementType type)
            throws IOException {

        this.writeText(Objects.nonNull(type) ? type.open : ElementType.EMPTY.open);
        if (namespace != null
                && !namespace.isBlank()) {
            this.writeText(String.format("%s:%s", namespace.trim(), name));
            if (uri != null
                    && !uri.isBlank())
                this.writeText(String.format(" xmlns:%s=\"%s\"", namespace.trim(), uri.trim()));
        } else this.writeText(name);
        this.writeText(Objects.nonNull(type) ? type.close : ElementType.EMPTY.close);
    }

    /**
     * Writes the passed string as text into the data stream.
     *
     * @param text Text
     * @throws IOException In case of faulty access to the data stream
     */
    void writeText(final String text)
            throws IOException {
        this.output.write(String.valueOf(text).getBytes(this.encoding));
    }

    /**
     * Writes the passed string as data segment into the data stream.
     *
     * @param data Content of the data segment
     * @throws IOException In case of faulty access to the data stream
     */
    void writeData(final String data)
            throws IOException {
        this.writeText(String.format("<![CDATA[%s]]>", data));
    }

    /**
     * Writes the data in the output buffer to the data stream.
     *
     * @throws IOException In case of faulty access to the data stream
     */
    void flush()
            throws IOException {
        this.output.flush();
    }

    @Override
    public void close()
            throws IOException {
        this.output.flush();
        this.output.close();
    }
}