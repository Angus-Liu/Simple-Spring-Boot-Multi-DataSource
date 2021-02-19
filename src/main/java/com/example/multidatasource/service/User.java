package com.example.multidatasource.service;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String username;

    private String password;

    private Byte sex;

    private Date create_time;

    private Date update_time;

    private Byte deleted;
}