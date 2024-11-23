package com.crd.cit.m2521.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValuesNodes {
    private List<Value> values;
    private NodeCondition node;
    private String template;
}
