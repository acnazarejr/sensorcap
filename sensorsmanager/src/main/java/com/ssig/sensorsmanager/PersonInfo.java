package com.ssig.sensorsmanager;

import java.io.Serializable;

/**
 * Created by flabe on 01/06/2018.
 */

public class PersonInfo implements Serializable {
    private String Name;
    private Float Height;
    private Float Weight;
    private Integer Age;
    public enum Gender {
        FEMALE, MALE;
    }

    public PersonInfo(){
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public Float getHeight() {
        return Height;
    }

    public void setHeight(Float height) {
        Height = height;
    }

    public Float getWeight() {
        return Weight;
    }

    public void setWeight(Float weight) {
        this.Weight = weight;
    }

    public Integer getAge() {
        return this.Age;
    }

    public void setAge(Integer age) {
        this.Age = age;
    }
}
