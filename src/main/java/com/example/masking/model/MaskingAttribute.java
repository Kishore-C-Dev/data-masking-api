package com.example.masking.model;

public class MaskingAttribute {

    private String xpath;
    private String jsonpath;
    private Integer start;
    private Integer end;

    public MaskingAttribute() {
    }

    public MaskingAttribute(String xpath, String jsonpath, Integer start, Integer end) {
        this.xpath = xpath;
        this.jsonpath = jsonpath;
        this.start = start;
        this.end = end;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getJsonpath() {
        return jsonpath;
    }

    public void setJsonpath(String jsonpath) {
        this.jsonpath = jsonpath;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}
