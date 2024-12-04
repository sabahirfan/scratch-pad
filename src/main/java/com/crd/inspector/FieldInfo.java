package com.crd.inspector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldInfo {

    private String name;
    private String type;
    private List<String> enumValues;
}