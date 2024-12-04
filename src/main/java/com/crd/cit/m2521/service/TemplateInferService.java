package com.crd.cit.m2521.service;

import com.crd.cit.m2521.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TemplateInferService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DocumentInfer documentInfer;

    public TemplateInferService() {
        this.documentInfer = loadMatchingConfig();
    }

        public DocumentInfer loadMatchingConfig() {
            try {
                return objectMapper.readValue(
                        getClass().getClassLoader().getResourceAsStream("matching-config.json"),
                        DocumentInfer.class
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to load matching config", e);
            }
        }

    public String processDocumentInference(JsonNode crimsJsonNode) {
        // Populate crimsValues in the documentInfer object
        populateCrimsValues(documentInfer.getNode(), crimsJsonNode);

        // Find and return the template
        return findTemplateRecursively(documentInfer.getNode(), crimsJsonNode);
    }

    private void populateCrimsValues(NodeCondition nodeCondition, JsonNode crimsJsonNode) {
        // Populate crimsValues for each field in the node
        for (Field field : nodeCondition.getFields()) {
            String[] pathParts = field.getField().split("\\.");
            JsonNode valueNode = crimsJsonNode;

            for (String part : pathParts) {
                valueNode = valueNode.path(part);
            }

            field.setCrimsValue(valueNode.asText());
        }
    }

    private String findTemplateRecursively(NodeCondition nodeCondition, JsonNode crimsJsonNode) {
        // Check direct children first
        for (ValuesNodes valuesNode : nodeCondition.getChildren()) {
            // Check if all field values match the values in the current node
            if (matchesAllValues(nodeCondition.getFields(), valuesNode.getValues())) {
                // If template exists, return it
                if (valuesNode.getTemplate() != null) {
                    return valuesNode.getTemplate();
                }

                // If no template but a nested node exists, recursively process
                if (valuesNode.getNode() != null) {
                    // populate crims values for this child tree
                    populateCrimsValues(valuesNode.getNode(), crimsJsonNode);
                    return findTemplateRecursively(valuesNode.getNode(), crimsJsonNode);
                }
            }
        }

        Optional<ValuesNodes> wildcardMatch = findSingleWildcardMatch(nodeCondition);
        if (wildcardMatch.isPresent()) {
            if (wildcardMatch.get().getTemplate() != null) {
                return wildcardMatch.get().getTemplate();
            }

            if (wildcardMatch.get().getNode() != null) {
                // Also need to populate crims values for wildcard matches
                populateCrimsValues(wildcardMatch.get().getNode(), crimsJsonNode);
                return findTemplateRecursively(wildcardMatch.get().getNode(), crimsJsonNode);
            }
        }

        return null;
    }

    private boolean matchesAllValues(List<Field> fields, List<Value> values) {
        if (fields.size() != values.size()) return false;

        return fields.stream().allMatch(field ->
                values.stream().anyMatch(value ->
                        field.getName().equals(value.getName()) &&
                                value.getValue().contains(field.getCrimsValue())
                )
        );
    }

    private Optional<ValuesNodes> findSingleWildcardMatch(NodeCondition nodeCondition) {
        // Collect all wildcard matches
        List<ValuesNodes> wildcardMatches = nodeCondition.getChildren().stream()
                .filter(valuesNode -> isWildcardMatch(nodeCondition.getFields(), valuesNode.getValues()))
                .toList();

        // Check if there's more than one match
        if (wildcardMatches.size() > 1) {
            throw new IllegalStateException("Invalid MatchingConfig: More than one wildcard match found.");
        }

        // Return the single match if exactly one match is found
        return wildcardMatches.isEmpty() ? Optional.empty() : Optional.of(wildcardMatches.get(0));
    }

    private boolean isWildcardMatch(List<Field> fields, List<Value> values) {
        if (fields.size() != values.size()) return false;

        return fields.stream().allMatch(field ->
                values.stream().anyMatch(value ->
                        field.getName().equals(value.getName()) &&
                                (value.getValue().contains("*") ||
                                        value.getValue().contains(field.getCrimsValue()))
                )
        );
    }

    public static void main(String... args) throws Exception {
        TemplateInferService templateInferService = new TemplateInferService();
        // Note: You would need to provide the actual crimsJsonNode here
        JsonNode crimsJsonNode = templateInferService.objectMapper.readTree(
                TemplateInferService.class.getClassLoader().getResourceAsStream("crims.json")
        );
        var result = templateInferService.processDocumentInference(crimsJsonNode);
        log.info("Template = " + result);
    }
}