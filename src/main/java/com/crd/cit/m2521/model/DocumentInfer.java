package com.crd.cit.m2521.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentInfer {
    private NodeCondition node;
}
