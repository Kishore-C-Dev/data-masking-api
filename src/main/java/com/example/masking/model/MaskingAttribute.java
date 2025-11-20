package com.example.masking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaskingAttribute {

    private String xpath;
    private String jsonpath;
    private Integer start;
    private Integer end;
}
