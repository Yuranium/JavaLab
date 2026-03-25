package com.yuranium.userservice.service;

import com.javalab.core.exception.ResourceNotCreatedException;
import com.yuranium.userservice.config.KeycloakConfig;
import com.yuranium.userservice.enums.RoleType;
import com.yuranium.userservice.models.dto.UserRequestDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

import static jakarta.ws.rs.core.Response.Status.Family.SUCCESSFUL;

@Service
@RequiredArgsConstructor
public class KeycloakService
{
    private final Keycloak keycloak;

    private final KeycloakConfig keycloakConfig;

    public UUID createUser(UserRequestDto userDto)
    {
        UsersResource usersResource = keycloak.realm(keycloakConfig.getCurrentRealm()).users();

        UserRepresentation userRep = getUserRepresentation(
                userDto.email(), userDto.username(), userDto.password()
        );

        RoleRepresentation role = keycloak.realm(keycloakConfig.getCurrentRealm())
                .roles().get(RoleType.ROLE_USER.name()).toRepresentation();

        try (var response = usersResource.create(userRep))
        {
            if (response.getStatusInfo().getFamily() != SUCCESSFUL)
                throw new ResourceNotCreatedException("Failed to create user in Keycloak");
            String location = response.getLocation().getPath();
            String userId = location.substring(location.lastIndexOf("/") + 1);
            usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(role));
            return UUID.fromString(userId);
        }
    }

    private UserRepresentation getUserRepresentation(String email, String username, String password)
    {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setEmail(email);
        userRep.setUsername(username);
        userRep.setEnabled(true);
        userRep.setEmailVerified(false);
        userRep.setRequiredActions(Collections.emptyList());

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        passwordCred.setTemporary(false);
        userRep.setCredentials(Collections.singletonList(passwordCred));
        return userRep;
    }

    public void deleteUser(UUID keycloakUserId)
    {
        UsersResource usersResource = keycloak.realm(keycloakConfig.getCurrentRealm()).users();
        try (var response = usersResource.delete(keycloakUserId.toString()))
        {
            if (response.getStatusInfo().getFamily() != SUCCESSFUL)
                throw new ResourceNotCreatedException("Failed to delete user in Keycloak");
        }
    }

    public void changeUserActivity(UUID userId, boolean activityState)
    {
        var user = getUserRepresentation(userId);
        if (user.isEnabled() == activityState)
            return;

        user.setEnabled(activityState);
        saveChanges(user);
    }

    public void changeEmailStatus(UUID userId, boolean emailStatus)
    {
        var user = getUserRepresentation(userId);
        if (user.isEmailVerified() == emailStatus)
            return;

        user.setEmailVerified(emailStatus);
        saveChanges(user);
    }

    private UserRepresentation getUserRepresentation(UUID userId)
    {
        try
        {
            return keycloak.realm(keycloakConfig.getCurrentRealm())
                    .users()
                    .get(String.valueOf(userId))
                    .toRepresentation();
        } catch (Exception e)
        {
            throw new ResourceNotFoundException(
                    "Failed to fetch user with k-id=%s".formatted(userId), e
            );
        }
    }

    private void saveChanges(UserRepresentation user)
    {
        try
        {
            keycloak.realm(keycloakConfig.getCurrentRealm())
                    .users()
                    .get(user.getId())
                    .update(user);
        } catch (Exception e)
        {
            throw new ResourceNotFoundException(
                    "Failed to update user with k-id=%s".formatted(user.getId()), e
            );
        }
    }
}