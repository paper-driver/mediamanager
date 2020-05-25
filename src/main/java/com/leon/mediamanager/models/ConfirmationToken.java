package com.leon.mediamanager.models;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;

@Entity
@Table(name = "confirmation_token")
public class ConfirmationToken {
    private static final int EXPIRATION = 60 * 24;

    @Id
    private Long id;

    @Column(name = "token")
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @MapsId
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(	name = "token_roles",
            joinColumns = @JoinColumn(name = "confirmation_token_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<Role>();

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "expiry_date")
    private Date expiryDate;;

    public ConfirmationToken(User user){
        Calendar calendar = Calendar.getInstance();
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.createdDate = new Date(calendar.getTime().getTime());
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public ConfirmationToken(){
        Calendar calendar = Calendar.getInstance();
        this.createdDate = new Date(calendar.getTime().getTime());
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

    public void setToken() {
        this.token = UUID.randomUUID().toString();
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        // calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
        // calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(calendar.getTime().getTime());
    }
}
