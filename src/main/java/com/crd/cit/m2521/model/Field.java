package com.crd.cit.m2521.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@SuppressWarnings("PMD")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {

    private String name;
    private String field;
    private String crimsValue;
    private FieldType type = FieldType.STRING;  // Default to STRING
}