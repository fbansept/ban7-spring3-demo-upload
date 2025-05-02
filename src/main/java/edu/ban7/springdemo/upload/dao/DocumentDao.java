package edu.ban7.springdemo.upload.dao;

import edu.ban7.springdemo.upload.model.Document;
import edu.ban7.springdemo.upload.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentDao extends JpaRepository<Document, Integer> {

}
