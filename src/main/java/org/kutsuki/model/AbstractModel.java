package org.kutsuki.model;

import java.io.Serializable;

import com.google.gson.Gson;

public abstract class AbstractModel implements Serializable {
    private static final long serialVersionUID = 6521330174274172729L;

    private transient static final Gson GSON = new Gson();

    private transient String _id;

    public String toString() {
	return GSON.toJson(this);
    }

    // getId
    public String getId() {
	return _id;
    }

    // setId
    public void setId(String _id) {
	this._id = _id;
    }
}
