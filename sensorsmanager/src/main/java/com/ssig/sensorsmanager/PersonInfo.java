package com.ssig.sensorsmanager;

import java.io.Serializable;

/**
 * Created by flabe on 01/06/2018.
 */

public class PersonInfo implements Serializable {
    private String nome;
    private Float Height;
    private Float Weight;
    private Integer age;
    public enum Gender {
        FEMALE, MALE;
    }

    public PersonInfo(){
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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
        Weight = weight;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
