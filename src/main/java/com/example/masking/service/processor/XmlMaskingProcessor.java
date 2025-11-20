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

    @Override
    public String mask(String payload, List<MaskingAttribute> attributes) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(payload.getBytes()));

            XPath xpath = XPathFactory.newInstance().newXPath();

            for (MaskingAttribute attribute : attributes) {
                if (attribute.getXpath() != null) {
                    NodeList nodes = (NodeList) xpath.evaluate(attribute.getXpath(), document, XPathConstants.NODESET);

                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node node = nodes.item(i);
                        String value = node.getTextContent();
                        node.setTextContent(maskValue(value));
                    }
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error masking XML payload: " + e.getMessage(), e);
        }
    }
}
