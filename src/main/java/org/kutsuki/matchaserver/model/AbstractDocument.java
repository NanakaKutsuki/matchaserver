package org.kutsuki.matchaserver.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Document
public abstract class AbstractDocument {
    @Id
    private String id;

    @Override
    public String toString() {
	String json = null;

	try {
	    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
	    json = ow.writeValueAsString(this);
	} catch (JsonProcessingException e) {
	    e.printStackTrace();
	}

	return json;
    }

    // getId
    public String getId() {
	return id;
    }

    // setId
    public void setId(String id) {
	this.id = id;
    }
}
