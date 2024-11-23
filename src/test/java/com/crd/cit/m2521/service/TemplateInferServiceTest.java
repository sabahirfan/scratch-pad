package com.crd.cit.m2521.service;

import com.crd.cit.m2521.model.DocumentInfer;
import com.crd.cit.m2521.model.Field;
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

    // Custom class that overrides loadMatchingConfig to return our test configuration
    private static class CustomTemplateInferService extends TemplateInferService {
        @Override
        public DocumentInfer loadMatchingConfig() {
            return createMatchingConfig();
        }
    }

    @Test
    void processDocumentInference_Scenario1_SabahGBP() throws Exception {
        // Arrange
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Sabah",
                    "localCurrency": "GBP"  
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);

        // Act
        String result = templateInferService.processDocumentInference(crimsJsonNode);

        // Assert
        assertEquals("TEMPLATE_A", result);
    }

    @Test
    void processDocumentInference_Scenario2_SabahUSD() throws Exception {
        // Arrange
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Sabah",
                    "localCurrency": "USD"  
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);

        // Act
        String result = templateInferService.processDocumentInference(crimsJsonNode);

        // Assert
        assertEquals("TEMPLATE_B", result);
    }

    @Test
    void processDocumentInference_Scenario3_ManjitWeekly() throws Exception {
        // Arrange
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

        // Act
        String result = templateInferService.processDocumentInference(crimsJsonNode);

        // Assert
        assertEquals("TEMPLATE_C", result);
    }

    @Test
    void processDocumentInference_Scenario4_ManjitMonthly() throws Exception {
        // Arrange
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

        // Act
        String result = templateInferService.processDocumentInference(crimsJsonNode);

        // Assert
        assertEquals("TEMPLATE_D", result);
    }

    @Test
    void processDocumentInference_Scenario5_ManjitWildcard() throws Exception {
        // Arrange
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

        // Act
        String result = templateInferService.processDocumentInference(crimsJsonNode);

        // Assert
        assertEquals("TEMPLATE_E", result);
    }

    @Test
    void processDocumentInference_NoMatchFound() throws Exception {
        // Arrange
        String crimsJson = """
                {
                  "security": {
                    "secTypCd": "Unknown",
                    "localCurrency": "EUR"
                  }
                }
                """;
        JsonNode crimsJsonNode = objectMapper.readTree(crimsJson);

        // Act
        String result = templateInferService.processDocumentInference(crimsJsonNode);

        // Assert
        assertNull(result);
    }

    private static DocumentInfer createMatchingConfig() {
        DocumentInfer documentInfer = new DocumentInfer();
        NodeCondition rootNode = new NodeCondition();

        // Set up fields for root node
        List<Field> rootFields = Arrays.asList(
                createField("secTypCd", "security.secTypCd"),
                createField("currency", "security.localCurrency")
        );
        rootNode.setFields(rootFields);

        // Create Sabah nodes (Scenarios 1 and 2)
        ValuesNodes sabahGbpNode = createValuesNode(
                Arrays.asList(
                        createValue("secTypCd", List.of("Sabah")),
                        createValue("currency", List.of("GBP"))
                ),
                "TEMPLATE_A"
        );

        ValuesNodes sabahUsdNode = createValuesNode(
                Arrays.asList(
                        createValue("secTypCd", List.of("Sabah")),
                        createValue("currency", List.of("USD"))
                ),
                "TEMPLATE_B"
        );

        // Create Manjit node with nested conditions
        NodeCondition manjitNode = new NodeCondition();
        manjitNode.setFields(Arrays.asList(
                createField("index", "security.index"),
                createField("paymentFreq", "security.paymentFreq")
        ));

        // Create Manjit's children (Scenarios 3, 4, and 5)
        ValuesNodes manjitWeeklyNode = createValuesNode(
                Arrays.asList(
                        createValue("index", List.of("Manjit")),
                        createValue("paymentFreq", List.of("Weekly"))
                ),
                "TEMPLATE_C"
        );

        ValuesNodes manjitMonthlyNode = createValuesNode(
                Arrays.asList(
                        createValue("index", List.of("Manjit")),
                        createValue("paymentFreq", List.of("Monthly"))
                ),
                "TEMPLATE_D"
        );

        ValuesNodes manjitWildcardNode = createValuesNode(
                Arrays.asList(
                        createValue("index", List.of("Manjit")),
                        createValue("paymentFreq", List.of("*"))
                ),
                "TEMPLATE_E"
        );

        manjitNode.setChildren(Arrays.asList(manjitWeeklyNode, manjitMonthlyNode, manjitWildcardNode));

        ValuesNodes manjitParentNode = createValuesNode(
                Arrays.asList(
                        createValue("secTypCd", List.of("Manjit")),
                        createValue("currency", List.of("GBP"))
                ),
                null
        );
        manjitParentNode.setNode(manjitNode);

        // Set up root node's children
        rootNode.setChildren(Arrays.asList(sabahGbpNode, sabahUsdNode, manjitParentNode));
        documentInfer.setNode(rootNode);

        return documentInfer;
    }

    private static Field createField(String name, String field) {
        Field f = new Field();
        f.setName(name);
        f.setField(field);
        return f;
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