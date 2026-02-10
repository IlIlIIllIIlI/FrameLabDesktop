package com.frameLab.frameSprite.model;

import java.util.ArrayList;

public class User {
    protected int id;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected ArrayList<Project> Projects;

    User(){

    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(int id) {
        this.id = id;
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
