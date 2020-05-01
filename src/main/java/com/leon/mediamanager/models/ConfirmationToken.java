package com.leon.mediamanager.models;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "confirmation_token")
public class ConfirmationToken {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "roles")
    private Set<Integer> roles = new HashSet<Integer>();

    @Column(name = "token")
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @MapsId
    private User user;

    public ConfirmationToken(User user, List<Role> roles){
        this.user = user;
        setRoles(roles);
        this.token = UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Set<Integer> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles){
        for(Role role : roles){
            this.roles.add(role.getId());
        }
    }
}
