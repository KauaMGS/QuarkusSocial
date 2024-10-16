package br.com.kgsm.quarkussocial.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "FOLLOWERS")
@Data
public class Follower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;

}
