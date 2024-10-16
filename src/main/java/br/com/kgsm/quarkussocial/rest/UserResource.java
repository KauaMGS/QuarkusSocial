package br.com.kgsm.quarkussocial.rest;

import br.com.kgsm.quarkussocial.domain.model.User;
import br.com.kgsm.quarkussocial.domain.repository.UserRepository;
import br.com.kgsm.quarkussocial.rest.dto.CreateUserRequest;
import br.com.kgsm.quarkussocial.rest.dto.ResponseError;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Set;

@Path("users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private UserRepository repository;
    private Validator validator;

    @Inject
    public UserResource(UserRepository repository, Validator validator){
        this.repository = repository;
        this.validator = validator;
    }

    @GET
    public Response listAllUsers(){
        PanacheQuery<User> query = repository.findAll();

        return Response.ok(query.list()).build();
    }

    @POST
    @Transactional
    public Response createUser(CreateUserRequest userRequest){
        User user = new User();

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);
        if(!violations.isEmpty()){
            return ResponseError.createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        user.setName(userRequest.getName());
        user.setAge(userRequest.getAge());
        repository.persist(user);

        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, CreateUserRequest userNewData){
        User user = repository.findById(id);

        if (user == null) return Response.status(Response.Status.NOT_FOUND).build();

        user.setName(userNewData.getName());
        user.setAge(userNewData.getAge());

        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id){
        User user = repository.findById(id);

        if (user == null) return Response.status(Response.Status.NOT_FOUND).build();

        repository.delete(user);

        return Response.noContent().build();
    }

}
