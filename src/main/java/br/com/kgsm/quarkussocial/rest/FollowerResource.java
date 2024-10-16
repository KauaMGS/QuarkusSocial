package br.com.kgsm.quarkussocial.rest;

import br.com.kgsm.quarkussocial.domain.model.Follower;
import br.com.kgsm.quarkussocial.domain.model.User;
import br.com.kgsm.quarkussocial.domain.repository.FollowerRepository;
import br.com.kgsm.quarkussocial.domain.repository.UserRepository;
import br.com.kgsm.quarkussocial.rest.dto.FollowerRequest;
import br.com.kgsm.quarkussocial.rest.dto.FollowerResponse;
import br.com.kgsm.quarkussocial.rest.dto.FollowersPerUserResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private FollowerRepository followerRepository;
    private UserRepository userRepository;

    @Inject
    public FollowerResource(FollowerRepository followerRepository, UserRepository userRepository) {
        this.followerRepository = followerRepository;
        this.userRepository = userRepository;
    }

    @GET
    public Response getFollowers(@PathParam("userId") Long userId){
        var list = followerRepository.findByUser(userId);
        var user = userRepository.findById(userId);

        if(user == null) return Response.status(Response.Status.NOT_FOUND).build();

        FollowersPerUserResponse followers = new FollowersPerUserResponse();
        followers.setFollowersCount(list.size());

        var followersList = list.stream().map(FollowerResponse::new).collect(Collectors.toList());
        followers.setContent(followersList);

        return Response.ok(followers).build();
    }

    @PUT
    @Transactional
    public Response followUser(@PathParam("userId") Long userId, FollowerRequest followerRequest){
        if(userId.equals(followerRequest.getFollowerId())) return Response.status(Response.Status.CONFLICT).entity("You can't follow yourself!").build();

        var user = userRepository.findById(userId);
        if(user == null) return Response.status(Response.Status.NOT_FOUND).build();

        var follower = userRepository.findById(followerRequest.getFollowerId());
        if(follower == null) return Response.status(Response.Status.NOT_FOUND).build();

        boolean follows = followerRepository.follows(follower, user);
        if(!follows) {
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);

            followerRepository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Transactional
    public Response unfollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId){
        var user = userRepository.findById(userId);
        if(user == null) return Response.status(Response.Status.NOT_FOUND).build();

        followerRepository.unfollowUser(userId, followerId);

        return Response.noContent().build();
    }

}
