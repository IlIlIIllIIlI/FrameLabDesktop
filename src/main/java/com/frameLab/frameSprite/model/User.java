package com.frameLab.frameSprite.model;

import java.util.List;

public class User {
    protected int id;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected List<Project> Projects;

    public User(){

    }

    public List<Project> getProjects() {
        return Projects;
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
        Projects = projects;
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
