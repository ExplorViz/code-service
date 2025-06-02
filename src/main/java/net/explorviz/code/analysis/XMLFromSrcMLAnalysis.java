package net.explorviz.code.analysis;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@ApplicationScoped
public class XMLFromSrcMLAnalysis {
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLFromSrcMLAnalysis.class);

    public NodeList retrieveNodeListFromXMLFile(InputStream xmlFileName) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(xmlFileName);
            XPath xPath = XPathFactory.newInstance().newXPath();

            xPath.setNamespaceContext(new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    switch (prefix) {
                        case "src": return "http://www.srcML.org/srcML/src";
                        case "cpp": return "http://www.srcML.org/srcML/cpp";
                        default: return XMLConstants.NULL_NS_URI;
                    }
                }

                public String getPrefix(String uri) { return null; }
                public Iterator<String> getPrefixes(String uri) { return null; }
            });

            String expression = "/src:unit/src:unit";
            XPathExpression xPathExpression = xPath.compile(expression);

            Object result = xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;

            return nodes;
        } catch (Exception e) {
            LOGGER.error("Error converting XML file to package structure", e);
            return null;
        }
    }

}