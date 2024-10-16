package br.com.kgsm.quarkussocial.domain.repository;

import br.com.kgsm.quarkussocial.domain.model.Follower;
import br.com.kgsm.quarkussocial.domain.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FollowerRepository implements PanacheRepository<Follower> {
    public boolean follows(User follower, User user){
        var params = Parameters.with("user", user).and("follower", follower);
        var query = find("follower =:follower and user =:user", params);

        Optional<Follower> follows = query.firstResultOptional();

        return follows.isPresent();
    }

    public List<Follower> findByUser(Long userId){
        var query = find("user.id", userId);

        return query.list();
    }

    public void unfollowUser(Long userId, Long followerId) {
        var params = Parameters.with("userId", userId).and("followerId", followerId).map();

        delete("follower.id =:followerId and user.id =:userId", params);
    }

}
