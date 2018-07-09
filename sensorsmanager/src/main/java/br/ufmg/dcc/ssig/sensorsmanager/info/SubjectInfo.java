package br.ufmg.dcc.ssig.sensorsmanager.info;

import java.io.Serializable;

@SuppressWarnings("ALL")
public class SubjectInfo implements Serializable {

    static final long serialVersionUID = 123789145623456789L;

    private String name;
    private Integer height;
    private Integer weight;
    private Integer age;
    private Gender gender;

    public enum Gender {
        FEMALE, MALE
    }

    public SubjectInfo(String name){
        this.name = name;
        this.height = null;
        this.weight = null;
        this.age = null;
        this.gender = null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

}
