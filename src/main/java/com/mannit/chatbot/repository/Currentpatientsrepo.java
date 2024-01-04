package com.mannit.chatbot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mannit.chatbot.model.CurrentPatient;

@Repository
public interface Currentpatientsrepo extends MongoRepository<CurrentPatient, String> {

}
