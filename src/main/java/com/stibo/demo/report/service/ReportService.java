package com.stibo.demo.report.service;

import com.stibo.demo.report.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class ReportService {
    
    private SearchService searchService;
    
    @Autowired
    public ReportService(SearchService searchService) {
        this.searchService = searchService;
    }
    
    @NotNull
    public Stream<Stream<String>> report(@NotNull Datastandard datastandard, @NotNull String categoryId) {
        
        Category category = findCategoryById(datastandard, categoryId);
        
        return getReportForCategoryAndItsParents(datastandard, category);
    }
    
    @NotNull
    private Category findCategoryById(@NotNull Datastandard datastandard, @NotNull String categoryId) {
        List<Category> categories = datastandard.getCategories().stream()
                .filter(category -> category.getId().equals(categoryId))
                .collect(Collectors.toList());
        
        if (categories.size() > 1)
            log.warn("Inconsistency in Categories: categoryId: {} matches {} categories.", categoryId, categories.size());
        
        return categories.get(0);
    }
    
    @NotNull
    private Stream<Stream<String>> getReportForCategoryAndItsParents(@NotNull Datastandard datastandard,
                                                                     @NotNull Category category) {
        
        Map<String, AttributeLink> attrIdAttrLink = searchService.getAttributeIdsWithAttributeLink(category.getAttributeLinks());
        
        List<Attribute> attributes = searchService.getAllAttributesByLinks(datastandard, attrIdAttrLink);
        
        List<List<String>> report = attributes.stream()
                .map(attribute -> {
                    
                    List<String> row = new ArrayList<>(5);
                    row.add(category.getName());
                    row.add(getAttributeName(attribute, attrIdAttrLink));
                    row.add(getAttributeDescription(attribute));
                    row.add(getAttributeTypeId(datastandard, attribute));
                    row.add(getAttributeGroupsNames(datastandard, attribute));
                    
                    return row;
                })
                .collect(Collectors.toList());
        
        return prepareResultsAndCheckParent(datastandard, category, report);
        
    }
    
    @NotNull
    private Stream<Stream<String>> prepareResultsAndCheckParent(@NotNull Datastandard datastandard,
                                                                @NotNull Category category,
                                                                @NotNull List<List<String>> report) {
        
        Stream<Stream<String>> results = report.stream().map(Collection::stream);
        
        if (category.getParentId() != null) {
            Category parent = findCategoryById(datastandard, category.getParentId());
            
            // recursive call, going up in hierarchy to parent category
            return Stream.concat(results, getReportForCategoryAndItsParents(datastandard, parent));
            
        } else
            return results;
    }
    
    @NotNull
    private String getAttributeName(@NotNull Attribute attribute, @NotNull Map<String, AttributeLink> attrLinksById) {
        
        return attrLinksById.get(attribute.getId()).getOptional()
                ? attribute.getName()
                : attribute.getName() + "*";
    }
    
    @NotNull
    private String getAttributeDescription(@NotNull Attribute attribute) {
        
        return attribute.getDescription() != null
                ? attribute.getDescription()
                : "";
    }
    
    @NotNull
    private String getAttributeTypeId(@NotNull Datastandard datastandard, @NotNull Attribute attribute) {
        
        return attribute.getType().getMultiValue()
                ? getAttributeTypes(datastandard, attribute) + "[]"
                : getAttributeTypes(datastandard, attribute);
    }
    
    @NotNull
    private String getAttributeTypes(@NotNull Datastandard datastandard, @NotNull Attribute attribute) {
        
        List<AttributeLink> attributeLinks = attribute.getAttributeLinks();
        
        if (attributeLinks == null || attributeLinks.size() == 0)
            return attribute.getType().getId();
        else {
            Map<String, AttributeLink> attrIdAttrLink = searchService.getAttributeIdsWithAttributeLink(attributeLinks);
            
            List<Attribute> attributes = searchService.getAllLinkedAttributes(datastandard, attribute);
            
            String nestedTypes = attributes.stream()
                    
                    // recursive call, looking for attributes details of linked attributes
                    .map(att -> "\t" + getAttributeName(att, attrIdAttrLink) + ": " + getAttributeTypeId(datastandard, att))
                    .collect(Collectors.joining("\n"));
            
            return attribute.getType().getId() + "{\n" + nestedTypes + "\n}";
        }
        
    }
    
    @NotNull
    private String getAttributeGroupsNames(@NotNull Datastandard datastandard, @NotNull Attribute attribute) {
        
        return searchService.getAllAttributesGroupsNames(datastandard, attribute);
        
    }
    
}