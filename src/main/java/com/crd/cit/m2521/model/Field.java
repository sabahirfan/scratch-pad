package com.crd.cit.m2521.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
    private String name;
    private String field;
    private String crimsValue; // this value will be populated from crims.json based on the field property of this class.
}