package com.yuranium.userservice.models;

import com.yuranium.userservice.models.entity.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails
{
    private UserEntity user;

    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return List.of(user.getRole());
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername()
    {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled()
    {
        return user.getActivity();
    }
}