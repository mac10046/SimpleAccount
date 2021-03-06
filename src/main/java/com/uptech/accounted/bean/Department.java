package com.uptech.accounted.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "Department")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Department implements Master {
  @Id
  @Column(name = "code", updatable = false, nullable = false)
  @NonNull
  private String code;
  @Column(name = "name", updatable = true, nullable = false)
  @NonNull
  private String name;

  public Department(String code, String name) {
    if(code.isEmpty() || name.isEmpty())
      throw new IllegalArgumentException("Fields can't be left blank");
    this.code = code;
    this.name = name;
  }

}
