package com.zenika.users.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "USERS")
@Data
public class Users {
  @Id
  private String id;
  @Column(unique = true)
  private String login;
  private String name;
  private double salary;
  private Date startDate;
}
