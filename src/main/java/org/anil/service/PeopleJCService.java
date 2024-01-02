package org.anil.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.anil.entity.People;

import java.io.IOException;
import java.util.List;

import static org.anil.resource.PeopleLLRCResource.INDEX_NAME;

@RequiredArgsConstructor
@ApplicationScoped
public class PeopleJCService {


    private final ElasticsearchClient client;

    public void index(People people) throws IOException {
        IndexRequest<People> request = IndexRequest.of(
                b -> b.index(INDEX_NAME)
                        .id(String.valueOf(people.getId()))
                        .document(people));
        client.index(request);
    }

    public People getById(int id) throws IOException {
        GetRequest getRequest = GetRequest.of(
                b -> b.index(INDEX_NAME)
                        .id(String.valueOf(id)));
        GetResponse<People> getResponse = client.get(getRequest, People.class);
        if (getResponse.found()) {
            return getResponse.source();
        }
        return null;
    }

    public List<People> searchByJob(String job) throws IOException {
        return search("job", job);
    }

    public List<People> searchByName(String name) throws IOException {
        return search("name", name);
    }

    public List<People> searchAll() throws IOException {
        SearchRequest searchRequest = SearchRequest.of(
                b -> b.index(INDEX_NAME)
                        .query(QueryBuilders.matchAll().build()._toQuery()));

        return executeQuery(searchRequest);
    }

    private List<People> search(String term, String match) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(
                b -> b.index(INDEX_NAME)
                        .query(QueryBuilders.match().field(term).query(FieldValue.of(match)).build()._toQuery()));

        return executeQuery(searchRequest);
    }

    private List<People> executeQuery(SearchRequest searchRequest) throws IOException {
        SearchResponse<People> searchResponse = client.search(searchRequest, People.class);
        HitsMetadata<People> hits = searchResponse.hits();
        return hits.hits().stream().map(Hit::source).toList();
    }


}
