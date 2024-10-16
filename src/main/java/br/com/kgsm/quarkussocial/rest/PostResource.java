package br.com.kgsm.quarkussocial.rest;

import br.com.kgsm.quarkussocial.domain.model.Follower;
import br.com.kgsm.quarkussocial.domain.model.Post;
import br.com.kgsm.quarkussocial.domain.model.User;
import br.com.kgsm.quarkussocial.domain.repository.FollowerRepository;
import br.com.kgsm.quarkussocial.domain.repository.PostRepository;
import br.com.kgsm.quarkussocial.domain.repository.UserRepository;
import br.com.kgsm.quarkussocial.rest.dto.CreatePostRequest;
import br.com.kgsm.quarkussocial.rest.dto.PostResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private FollowerRepository followerRepository;

    @Inject
    public PostResource(UserRepository userRepository, PostRepository postRepository, FollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @GET
    public Response listPosts(@PathParam("userId") Long userId, @HeaderParam("followerId") Long followerId){
        if(followerId == null) return Response.status(Response.Status.BAD_REQUEST).entity("You forgot the header followerId").build();

        User user = userRepository.findById(userId);
        User follower = userRepository.findById(followerId);

        if(user == null) return Response.status(Response.Status.NOT_FOUND).build();
        if(follower == null) return Response.status(Response.Status.BAD_REQUEST).entity("Inexistent followerId").build();

        boolean follows = followerRepository.follows(follower, user);

        if (!follows) return Response.status(Response.Status.FORBIDDEN).entity("You cant see these posts").build();

        PanacheQuery<Post> query = postRepository.find("user", Sort.by("dateTime", Sort.Direction.Descending), user);
        var list = query.list();

        List<PostResponse> postResponseList = list.stream().map(post -> PostResponse.fromEntity(post)).collect(Collectors.toList());

        return Response.ok(postResponseList).build();
    }

    @POST
    @Transactional
    public Response savePost(@PathParam("userId") Long userId, CreatePostRequest postRequest){
        User user = userRepository.findById(userId);

        if(user == null) return Response.status(Response.Status.NOT_FOUND).build();

        Post post = new Post();
        post.setText(postRequest.getText());
        post.setUser(user);

        postRepository.persist(post);

        return Response.status(Response.Status.CREATED).build();
    }

}
