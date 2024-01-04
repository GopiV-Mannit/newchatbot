package com.mannit.chatbot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mannit.chatbot.model.Noappointment;

@Repository
public interface Noappointmentrepo extends MongoRepository<Noappointment, String> {

}
