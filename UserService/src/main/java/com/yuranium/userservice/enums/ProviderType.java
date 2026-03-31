package com.yuranium.userservice.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum ProviderType
{
    GOOGLE("google")
            {
                @Override
                public String getAvatarUrl(String avatar)
                {
                    return avatar;
                }
            },
    GITHUB("github")
            {
                @Override
                public String getAvatarUrl(String avatar)
                {
                    return avatar;
                }
            },
    YANDEX("yandex")
            {
                @Override
                public String getAvatarUrl(String avatar)
                {
                    return "https://avatars.mds.yandex.net/get-yapic/%s/islands-200".formatted(avatar);
                }
            },
    VK("vk")
            {
                @Override
                public String getAvatarUrl(String avatar)
                {
                    return avatar;
                }
            };

    public abstract String getAvatarUrl(String avatar);

    private final String providerId;

    public static ProviderType fromId(String providerId)
    {
        return Arrays.stream(values())
                .filter(p -> p.providerId.equals(providerId))
                .findFirst()
                .orElse(GOOGLE);
    }
}