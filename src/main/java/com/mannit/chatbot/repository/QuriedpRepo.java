package com.mannit.chatbot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mannit.chatbot.model.QueriedPatient;


@Repository
public interface QuriedpRepo extends MongoRepository<QueriedPatient, String> {

}
