package com.crd.cit.m2521.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeCondition {
    private List<Field> fields;
    private List<ValuesNodes> children;
}
