package com.stibo.demo.report.service;

import com.google.common.collect.ImmutableMap;
import com.stibo.demo.report.model.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Service
public class SearchService {
    
    @NotNull
    @Cacheable("attributeGroup.getAllAttributeGroupNames")
    public String getAllAttributesGroupsNames(@NotNull Datastandard datastandard,
                                              @NotNull Attribute attribute) {
    
        // to avoid O(n) complexity on List::contains, will be slower for small sets
        Set<String> groupIds = new HashSet<>(attribute.getGroupIds());
        
        return datastandard.getAttributeGroups().stream()
                .filter(ag -> groupIds.contains(ag.getId()))
                .map(AttributeGroup::getName)
                .collect(joining("\n"));
    }
    
    @NotNull
    @Cacheable("attributes.getAllAttributesByLinks")
    public List<Attribute> getAllAttributesByLinks(@NotNull Datastandard datastandard,
                                                   @NotNull Map<String, AttributeLink> attrLinksById) {
        
        return datastandard.getAttributes().stream()
                .filter(attribute -> attrLinksById.containsKey(attribute.getId()))
                .collect(Collectors.toList());
        
    }
    
    @NotNull
    @Cacheable("attributeIdAttributeLink.getAttributeIdsWithAttributeLinkByAttrLinksList")
    public ImmutableMap<String, AttributeLink> getAttributeIdsWithAttributeLink(@NotNull List<AttributeLink> attributeLinks) {
        
        // ImmutableMap from Guava, to avoid collection modification, since result is sometimes passed as argument
        return attributeLinks.stream()
                // Since this Map should be used more likely as index, then filtering
                // duplicates is possible (they are present in acme data).
                .filter(distinctByKey(AttributeLink::getId))
                .collect(ImmutableMap.toImmutableMap(AttributeLink::getId, attributeLink -> attributeLink));
        
    }
    
    // https://stackoverflow.com/a/27872086/1441122
    private static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
    
    @NotNull
    @Cacheable("attributes.getAttributeIdsWithAttributeLink")
    public List<Attribute> getAllLinkedAttributes(@NotNull Datastandard datastandard,
                                                  @NotNull Attribute attribute) {
    
        // to avoid O(n) complexity on List::contains, will be slower for small sets
        Set<String> links = attribute.getAttributeLinks().stream()
                .map(AttributeLink::getId)
                .collect(Collectors.toSet());
        
        return datastandard.getAttributes().stream()
                .filter(attr -> links.contains(attr.getId()))
                .collect(Collectors.toList());
    }
}
