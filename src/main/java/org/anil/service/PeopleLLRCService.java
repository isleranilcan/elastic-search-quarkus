package org.anil.service;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.anil.entity.People;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.anil.resource.PeopleLLRCResource.INDEX_NAME;

@RequiredArgsConstructor
@ApplicationScoped
public class PeopleLLRCService {

    private final RestClient restClient;

    public void index(People people) throws IOException {
        Request request = new Request(
                "PUT",
                "/" + INDEX_NAME + "/_doc/" + people.getId());
        request.setJsonEntity(JsonObject.mapFrom(people).toString());
        restClient.performRequest(request);
    }

    public People getById(int id) throws IOException {
        Request request = new Request(
                "GET",
                "/" + INDEX_NAME + "/_doc/" + id);
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject json = new JsonObject(responseBody);
        return json.getJsonObject("_source").mapTo(People.class);
    }

    public List<People> searchByJob(String job) throws IOException {
        return search("job", job);
    }

    public List<People> searchByName(String name) throws IOException {
        return search("name", name);
    }

    public List<People> searchAll() throws IOException {
        Request request = new Request(
                "GET",
                "/" + INDEX_NAME + "/_search");
        //construct a JSON query like {"query": {"match_all": {}}
        JsonObject matchJson = new JsonObject().put("match_all", new JsonObject());
        JsonObject queryJson = new JsonObject().put("query", matchJson);
        request.setJsonEntity(queryJson.encode());

        return executeQuery(request);
    }

    private List<People> search(String term, String match) throws IOException {
        Request request = new Request(
                "GET",
                "/" + INDEX_NAME + "/_search");
        //construct a JSON query like {"query": {"match": {"<term>": "<match"}}
        JsonObject termJson = new JsonObject().put(term, match);
        JsonObject matchJson = new JsonObject().put("match", termJson);
        JsonObject queryJson = new JsonObject().put("query", matchJson);
        request.setJsonEntity(queryJson.encode());

        return executeQuery(request);
    }

    private List<People> executeQuery(Request request) throws IOException {
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject json = new JsonObject(responseBody);
        JsonArray hits = json.getJsonObject("hits").getJsonArray("hits");
        List<People> results = new ArrayList<>(hits.size());
        for (int i = 0; i < hits.size(); i++) {
            JsonObject hit = hits.getJsonObject(i);
            People people = hit.getJsonObject("_source").mapTo(People.class);
            results.add(people);
        }
        return results;
    }
}
