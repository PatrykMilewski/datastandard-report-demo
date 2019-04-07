package com.stibo.demo.report.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Attribute {
  
  private String id;
  private String name;
  private String description;
  private AttributeType type;
  private List<AttributeLink> attributeLinks;
  private List<String> groupIds;
  
}

