package com.stibo.demo.report.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Category   {

  private String id;
  private String name;
  private String description;
  private String parentId;
  private List<AttributeLink> attributeLinks;

}

