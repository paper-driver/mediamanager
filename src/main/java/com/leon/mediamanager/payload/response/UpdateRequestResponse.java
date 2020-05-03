package com.leon.mediamanager.payload.response;

import com.leon.mediamanager.models.ConfirmationToken;
import com.leon.mediamanager.models.ERole;
import com.leon.mediamanager.models.Role;
import com.leon.mediamanager.models.User;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UpdateRequestResponse {

    private String token;
    private String username;
    private String email;
    private Date expiryDate;;
    private List<ERole> pendingRoles = new ArrayList<>();

    public UpdateRequestResponse(String token, User user, Date expiryDate,  Set<Role> roles){
        setToken(token);
        setUserInfo(user);
        setExpiryDate(expiryDate);
        setPendingRoles(roles);
    }

    public UpdateRequestResponse() {}

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setPendingRoles(Set<Role> roles) {
        for(Role role : roles){
            this.pendingRoles.add(role.getName());
        }
    }

    public void setUserInfo(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
    }

    public void setObjectFromTokenObject(ConfirmationToken token) {
        this.username = token.getUser().getUsername();
        this.email = token.getUser().getEmail();
        this.expiryDate = token.getExpiryDate();
        this.token = token.getToken();
        setPendingRoles(token.getRoles());
    }

    public String getToken() {
        return this.token;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public List<ERole> getPendingRoles() {
        return this.pendingRoles;
    }

}
