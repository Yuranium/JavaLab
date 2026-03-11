package com.yuranium.userservice.service;

import com.yuranium.userservice.config.KeycloakConfig;
import com.yuranium.userservice.enums.RoleType;
import com.yuranium.userservice.models.dto.UserRequestDto;
import com.yuranium.userservice.util.exception.ResourceNotCreatedException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

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

        try (Response response = usersResource.create(userRep))
        {
            if (response.getStatus() != 201)
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
        usersResource.delete(keycloakUserId.toString());
    }

    public void verifyUser(UUID keycloakUserId)
    {
        UserRepresentation user = keycloak.realm(keycloakConfig.getCurrentRealm())
                .users()
                .get(String.valueOf(keycloakUserId))
                .toRepresentation();
        user.setEmailVerified(true);
        keycloak.realm(keycloakConfig.getCurrentRealm())
                .users()
                .get(String.valueOf(keycloakUserId))
                .update(user);
    }
}