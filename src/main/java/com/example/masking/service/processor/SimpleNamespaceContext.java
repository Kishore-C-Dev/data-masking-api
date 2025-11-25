package com.example.masking.service.processor;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleNamespaceContext implements NamespaceContext {
    private final Map<String, String> prefixToUri = new HashMap<>();
    private final Map<String, String> uriToPrefix = new HashMap<>();

    public void bindNamespaceUri(String prefix, String namespaceURI) {
        prefixToUri.put(prefix, namespaceURI);
        uriToPrefix.put(namespaceURI, prefix);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        }

        // Handle predefined XML prefixes
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        }
        if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }

        // Return mapped URI or NULL_NS_URI if not found
        String uri = prefixToUri.get(prefix);
        return uri != null ? uri : XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return uriToPrefix.get(namespaceURI);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        String prefix = getPrefix(namespaceURI);
        return prefix != null ?
            java.util.Collections.singletonList(prefix).iterator() :
            java.util.Collections.emptyIterator();
    }
}
