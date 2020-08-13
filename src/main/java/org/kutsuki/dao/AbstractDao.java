package org.kutsuki.dao;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.model.AbstractModel;

import com.google.gson.Gson;

public abstract class AbstractDao<T extends AbstractModel> {
    private static final Gson GSON = new Gson();
    private static final String INDEX = "lambert";
    private static final ZoneId MST = ZoneId.of("America/Denver");

    public abstract Class<T> getClazz();

    public abstract String getType();

    public String getIndex() {
	return INDEX;
    }

    // index
    public void index(T model) {
	try {
	    IndexResponse response = null;

	    if (model.getId() != null) {
		response = DaoManager.getClient().prepareIndex(getIndex(), getType(), model.getId())
			.setSource(model.toString()).get();
	    } else {
		response = DaoManager.getClient().prepareIndex(getIndex(), getType()).setSource(model.toString()).get();
	    }

	    if (response.getResult() == Result.CREATED) {
		model.setId(response.getId());
	    }
	} catch (ElasticsearchException e) {
	    String error = "Failed to index: " + model + " into " + getIndex() + ", " + getType();
	    EmailManager.emailException(error, e);
	}
    }

    // delete
    public void delete(T model) {
	try {
	    DeleteResponse response = DaoManager.getClient().prepareDelete(getIndex(), getType(), model.getId()).get();

	    if (response.getResult() != Result.DELETED) {
		String error = "Failed to delete: " + model + " in " + getIndex() + ", " + getType();
		EmailManager.email(EmailManager.HOME, error, model.toString(), null);
	    }
	} catch (ElasticsearchException e) {
	    String error = "Failed to delete: " + model + " in " + getIndex() + ", " + getType();
	    EmailManager.emailException(error, e);
	}
    }

    // getById
    public T getById(String id) {
	T model = null;

	try {
	    GetResponse response = DaoManager.getClient().prepareGet(getIndex(), getType(), id).get();

	    if (response.isExists()) {
		model = GSON.fromJson(response.getSourceAsString(), getClazz());
		model.setId(response.getId());
	    }
	} catch (ElasticsearchException e) {
	    String error = "Failed to get by id: " + id + " in " + getIndex() + ", " + getType();
	    EmailManager.emailException(error, e);
	}

	return model;
    }

    // getAll
    public List<T> getAll() {
	return search(null);
    }

    // search
    public List<T> search(QueryBuilder query) {
	List<T> list = new ArrayList<T>();

	try {
	    SearchRequestBuilder srb = DaoManager.getClient().prepareSearch(getIndex());
	    srb.setTypes(getType());
	    srb.setSize(1000);
	    srb.setScroll(TimeValue.timeValueMinutes(1));
	    if (query != null) {
		srb.setQuery(query);
	    }

	    SearchResponse response = srb.get();

	    while (response.getHits().getHits().length > 0) {
		for (SearchHit hit : response.getHits()) {
		    T model = GSON.fromJson(hit.getSourceAsString(), getClazz());
		    model.setId(hit.getId());
		    list.add(model);
		}

		response = DaoManager.getClient().prepareSearchScroll(response.getScrollId())
			.setScroll(TimeValue.timeValueMinutes(1)).get();
	    }
	} catch (ElasticsearchException e) {
	    String error = null;

	    if (query != null) {
		error = "Failed to search: " + query + " in " + getIndex() + ", " + getType();
	    } else {
		error = "Failed to get all " + getIndex() + ", " + getType();
	    }

	    EmailManager.emailException(error, e);
	}

	return list;
    }

    // count
    public long count() {
	return count(null);
    }

    // count
    public long count(QueryBuilder query) {
	long count = 0;

	try {
	    SearchRequestBuilder srb = DaoManager.getClient().prepareSearch(getIndex());
	    srb.setTypes(getType());

	    if (query != null) {
		srb.setQuery(query);
	    }

	    SearchResponse response = srb.get();

	    count = response.getHits().getTotalHits().value;
	} catch (ElasticsearchException e) {
	    String error = null;

	    if (query != null) {
		error = "Failed to count: " + query + " in " + getIndex() + ", " + getType();
	    } else {
		error = "Failed to count all " + getIndex() + ", " + getType();
	    }

	    EmailManager.emailException(error, e);
	}

	return count;
    }

    // getMST
    public ZoneId getMST() {
	return MST;
    }

    // now
    public ZonedDateTime now() {
	return ZonedDateTime.now(getMST());
    }
}
