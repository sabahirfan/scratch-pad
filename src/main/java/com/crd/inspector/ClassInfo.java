package com.crd.inspector;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassInfo {

    private List<FieldInfo> fields = new ArrayList<>();
    private String beanName;

    public ClassInfo(String beanName) {
        this.beanName = beanName;
    }
}

