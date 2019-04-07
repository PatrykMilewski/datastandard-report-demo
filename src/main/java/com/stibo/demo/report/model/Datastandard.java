package com.stibo.demo.report.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Datastandard {

  private String id;
  private String name;
  private List<Category> categories;
  private List<Attribute> attributes;
  private List<AttributeGroup> attributeGroups;

}

