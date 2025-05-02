package edu.ban7.springdemo.upload.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;
    private String name;
    private String uri;

    @ManyToOne(optional = false)
    User owner;

}
