package com.example.masking.model;

import java.util.HashMap;
import java.util.Map;

public class XmlSubtypeInfo {
    private String subtype;           // e.g., "pain.013", "camt.054", "custom-banking"
    private String namespace;         // Full xmlns URI
    private Map<String, String> namespacePrefixes; // For XPath namespace resolution

    public XmlSubtypeInfo() {
        this.namespacePrefixes = new HashMap<>();
    }

    public XmlSubtypeInfo(String subtype, String namespace) {
        this.subtype = subtype;
        this.namespace = namespace;
        this.namespacePrefixes = new HashMap<>();
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, String> getNamespacePrefixes() {
        return namespacePrefixes;
    }

    public void setNamespacePrefixes(Map<String, String> namespacePrefixes) {
        this.namespacePrefixes = namespacePrefixes;
    }

    public void addNamespacePrefix(String prefix, String uri) {
        this.namespacePrefixes.put(prefix, uri);
    }

    @Override
    public String toString() {
        return "XmlSubtypeInfo{" +
                "subtype='" + subtype + '\'' +
                ", namespace='" + namespace + '\'' +
                ", namespacePrefixes=" + namespacePrefixes +
                '}';
    }
}
