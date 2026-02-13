package com.frameLab.frameSprite.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class User {
    protected int id;
    protected String firstName;
    protected String lastName;
    protected String email;
    @JsonIgnore
    protected List<Project> projects;

    public User(){

    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public void setFirst_name(String firstName) {
        this.firstName = firstName;
    }

    public void setLast_name(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

}
