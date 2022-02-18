/*
 * Copyright (C) 2021 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.knaw.dans.filemigration.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

public class AmdHandler extends DefaultHandler {

    private static final Logger log = LoggerFactory.getLogger(AmdHandler.class);

    private static final SAXParserFactory parserFactory = configureFactory();
    private StringBuilder chars; // collected since the last startElement
    private String changeDate;
    private boolean published;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        chars = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {

        if ("changeDate".equalsIgnoreCase(localName)) {
            if (published && changeDate == null)
                changeDate = chars.toString().toUpperCase();
            published = false;
        } else if ("toState".equalsIgnoreCase(localName)) {
            published = "PUBLISHED".equals(chars.toString());
        } else published = false;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        chars.append(new String(ch, start, length));
    }

    private String get() {
        return changeDate;
    }

    static public SAXParserFactory configureFactory() {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        return saxParserFactory;
    }

    /**
     * @return content of the first <changeDate></changeDate> immediately following a <toState>PUBLISHED</toState>
     */
    static public String parseAmd(InputStream xml) {
        AmdHandler handler = new AmdHandler();
        try {
            parserFactory.newSAXParser().parse(xml, handler);
            return handler.get();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}