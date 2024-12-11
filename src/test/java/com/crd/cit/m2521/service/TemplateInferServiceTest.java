package com.crd.cit.m2521.service;

import com.crd.cit.m2521.model.DocumentInfer;
import com.crd.cit.m2521.model.Field;
import com.crd.cit.m2521.model.FieldType;
import com.crd.cit.m2521.model.NodeCondition;
import com.crd.cit.m2521.model.Value;
import com.crd.cit.m2521.model.ValuesNodes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class TemplateInferServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TemplateInferService templateInferService = new CustomTemplateInferService();

    private static class CustomTemplateInferService extends TemplateInferService {
        @Override
        public DocumentInfer loadMatchingConfig() {
            return createMatchingConfig();
        }
    }

    // Original test scenarios
    @Test
    void processDocumentInference_Scenario1_SabahGBP() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Sabah",
                    "localCurrency": "GBP"  
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertEquals("TEMPLATE_A", result);
    }

    @Test
    void processDocumentInference_Scenario2_SabahUSD() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Sabah",
                    "localCurrency": "USD"  
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertEquals("TEMPLATE_B", result);
    }

    @Test
    void processDocumentInference_Scenario3_ManjitWeekly() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Manjit",
                    "localCurrency": "GBP",
                    "index": "Manjit",
                    "paymentFreq": "Weekly"
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertEquals("TEMPLATE_C", result);
    }

    @Test
    void processDocumentInference_Scenario4_ManjitMonthly() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Manjit",
                    "localCurrency": "GBP",
                    "index": "Manjit",
                    "paymentFreq": "Monthly"
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertEquals("TEMPLATE_D", result);
    }

    @Test
    void processDocumentInference_Scenario5_ManjitWildcard() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Manjit",
                    "localCurrency": "GBP",
                    "index": "Manjit",
                    "paymentFreq": "Unknown"
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertEquals("TEMPLATE_E", result);
    }

    @Test
    void processDocumentInference_NoMatchFound() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Unknown",
                    "localCurrency": "EUR"
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertNull(result);
    }

    // New test scenarios for array matching
    @Test
    void processDocumentInference_Scenario7_MultiCurrencyMatch() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Bond",
                    "localCurrency": ["USD", "EUR"],
                    "markets": ["US", "EU"]
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertEquals("TEMPLATE_MULTI_CURRENCY", result);
    }

    @Test
    void processDocumentInference_Scenario8_SingleValueInArray() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Bond",
                    "localCurrency": ["USD"],
                    "markets": ["US"]
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertEquals("TEMPLATE_MULTI_CURRENCY", result);
    }

    // New test scenarios for regex matching
    @Test
    void processDocumentInference_Scenario9_RegexPaymentFreq() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Manjit",
                    "localCurrency": "GBP",
                    "index": "Manjit",
                    "paymentFreq": "Weekly12"
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertEquals("TEMPLATE_WEEKLY_NUMBERED", result);
    }

    @Test
    void processDocumentInference_Scenario10_RegexSecurityCode() throws Exception {
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "SAB2023",
                    "localCurrency": "USD"
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);
        String result = templateInferService.processDocumentInference(crimsJsonNode);
        assertEquals("TEMPLATE_SAB_PATTERN", result);
    }

    private static DocumentInfer createMatchingConfig() {
        DocumentInfer documentInfer = new DocumentInfer();
        NodeCondition rootNode = new NodeCondition();

        // Set up fields for root node
        List<Field> rootFields = Arrays.asList(
                createField("secTypCd", "security.secTypCd", FieldType.REGEX),
                createField("currency", "security.localCurrency", FieldType.ARRAY)
        );
        rootNode.setFields(rootFields);

        // Create all nodes
        List<ValuesNodes> allNodes = Arrays.asList(
                // Original Sabah nodes
                createValuesNode(
                        Arrays.asList(
                                createValue("secTypCd", List.of("Sabah")),
                                createValue("currency", List.of("GBP"))
                        ),
                        "TEMPLATE_A"
                ),
                createValuesNode(
                        Arrays.asList(
                                createValue("secTypCd", List.of("Sabah")),
                                createValue("currency", List.of("USD"))
                        ),
                        "TEMPLATE_B"
                ),
                // Original Manjit node with nested conditions
                createManjitNode(),
                // New array matching node
                createValuesNode(
                        Arrays.asList(
                                createValue("secTypCd", List.of("Bond")),
                                createValue("currency", Arrays.asList("USD", "EUR"))
                        ),
                        "TEMPLATE_MULTI_CURRENCY"
                ),
                // New regex matching node for SAB pattern
                createValuesNode(
                        Arrays.asList(
                                createValue("secTypCd", List.of("^SAB\\d{4}$")),
                                createValue("currency", List.of("USD"))
                        ),
                        "TEMPLATE_SAB_PATTERN"
                )
        );

        rootNode.setChildren(allNodes);
        documentInfer.setNode(rootNode);

        return documentInfer;
    }

    private static ValuesNodes createManjitNode() {
        // Create Manjit's nested node structure
        NodeCondition manjitNode = new NodeCondition();
        manjitNode.setFields(Arrays.asList(
                createField("index", "security.index", FieldType.STRING),
                createField("paymentFreq", "security.paymentFreq", FieldType.REGEX)
        ));

        // Create Manjit's children nodes
        List<ValuesNodes> manjitChildren = Arrays.asList(
                // Original Weekly node
                createValuesNode(
                        Arrays.asList(
                                createValue("index", List.of("Manjit")),
                                createValue("paymentFreq", List.of("Weekly"))
                        ),
                        "TEMPLATE_C"
                ),
                // Original Monthly node
                createValuesNode(
                        Arrays.asList(
                                createValue("index", List.of("Manjit")),
                                createValue("paymentFreq", List.of("Monthly"))
                        ),
                        "TEMPLATE_D"
                ),
                // New Weekly with number pattern
                createValuesNode(
                        Arrays.asList(
                                createValue("index", List.of("Manjit")),
                                createValue("paymentFreq", List.of("Weekly\\d+"))
                        ),
                        "TEMPLATE_WEEKLY_NUMBERED"
                ),
                // Original wildcard node
                createValuesNode(
                        Arrays.asList(
                                createValue("index", List.of("Manjit")),
                                createValue("paymentFreq", List.of("*"))
                        ),
                        "TEMPLATE_E"
                )
        );

        manjitNode.setChildren(manjitChildren);

        // Create and return the parent Manjit node
        ValuesNodes manjitParentNode = createValuesNode(
                Arrays.asList(
                        createValue("secTypCd", List.of("Manjit")),
                        createValue("currency", List.of("GBP"))
                ),
                null
        );
        manjitParentNode.setNode(manjitNode);

        return manjitParentNode;
    }

    private static Field createField(String name, String field, FieldType type) {
        Field f = new Field();
        f.setName(name);
        f.setField(field);
        f.setType(type);
        return f;
    }

    private static Field createField(String name, String field) {
        return createField(name, field, FieldType.STRING);
    }

    private static Value createValue(String name, List<String> value) {
        Value v = new Value();
        v.setName(name);
        v.setValue(value);
        return v;
    }

    private static ValuesNodes createValuesNode(List<Value> values, String template) {
        ValuesNodes node = new ValuesNodes();
        node.setValues(values);
        node.setTemplate(template);
        return node;
    }
}