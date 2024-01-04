package com.mannit.chatbot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "CurrentPatients")
public class CurrentPatient {

	@Id
	private String id;

	private String name;

	private String phone_number;

	private String Doctor_choice;

	private String timestamp;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	public String getDoctor_choice() {
		return Doctor_choice;
	}

	public void setDoctor_choice(String doctor_choice) {
		Doctor_choice = doctor_choice;
	}

}
