package org.anil.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.anil.entity.People;
import org.anil.service.PeopleJCService;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.anil.resource.PeopleLLRCResource.INDEX_NAME;

@RequiredArgsConstructor
@Path("/" + INDEX_NAME + "/jc")
public class PeopleJCResource {

    private final PeopleJCService peopleJCService;

    @POST
    public Response index(People people) throws IOException {
        peopleJCService.index(people);
        return Response.created(URI.create("/" + INDEX_NAME + "/" + people.getId())).build();
    }

    @GET
    @Path("/{id}")
    public People get(@QueryParam int id) throws IOException {
        return peopleJCService.getById(id);
    }

    @GET
    @Path("/search")
    public List<People> search(@QueryParam String name, @QueryParam String job) throws IOException {
        if (name != null) {
            return peopleJCService.searchByName(name);
        } else if (job != null) {
            return peopleJCService.searchByJob(job);
        } else {
            return peopleJCService.searchAll();
        }
    }
}
