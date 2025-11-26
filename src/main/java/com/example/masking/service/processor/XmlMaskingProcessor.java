package com.example.masking.service.processor;

import com.example.masking.model.MaskingAttribute;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

@Component
public class XmlMaskingProcessor implements MaskingProcessor {

    // Cache expensive factories as static final (thread-safe singletons)
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
    private static final TransformerFactory TRANSFORMER_FACTORY;

    static {
        DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);

        TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    }

    // Cache XPathFactory instance (thread-safe)
    private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

    @Override
    public String mask(String payload, List<MaskingAttribute> attributes) {
        return maskWithNamespace(payload, attributes, null);
    }

    /**
     * Masks XML payload with namespace-aware processing.
     *
     * @param payload XML payload string
     * @param attributes List of masking attributes (XPath expressions)
     * @param namespaceUri The xmlns namespace URI
     * @return Masked XML payload
     */
    public String maskWithNamespace(String payload, List<MaskingAttribute> attributes, String namespaceUri) {
        try {
            // Use cached DocumentBuilderFactory
            DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(payload.getBytes()));

            // Create XPath instance for this request (XPath.setNamespaceContext is not thread-safe)
            XPath xpath = XPATH_FACTORY.newXPath();

            // Set up namespace context if namespace URI is provided
            if (namespaceUri != null && !namespaceUri.isEmpty()) {
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                nsContext.bindNamespaceUri("ns", namespaceUri);
                xpath.setNamespaceContext(nsContext);
            }

            for (MaskingAttribute attribute : attributes) {
                if (attribute.getXpath() != null) {
                    NodeList nodes = (NodeList) xpath.evaluate(
                            attribute.getXpath(),
                            document,
                            XPathConstants.NODESET
                    );

                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node node = nodes.item(i);
                        String value = node.getTextContent();
                        node.setTextContent(maskValue(value));
                    }
                }
            }

            // Use cached TransformerFactory
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error masking XML payload: " + e.getMessage(), e);
        }
    }
}
