package com.example.masking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaskingRule {

    private String service;
    private String type;
    private List<MaskingAttribute> attributes;
}
