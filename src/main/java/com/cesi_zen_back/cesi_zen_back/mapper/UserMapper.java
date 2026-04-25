package com.cesi_zen_back.cesi_zen_back.mapper;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;

public final class UserMapper {

    private UserMapper() {}

    public static AppUserDto toDto(AppUser user) {
        return new AppUserDto(
                user.getIdUser(),
                user.getMail(),
                user.getPseudo(),
                user.isActive(),
                user.getLastConnexion(),
                user.getRole().getRoleName()
        );
    }
}