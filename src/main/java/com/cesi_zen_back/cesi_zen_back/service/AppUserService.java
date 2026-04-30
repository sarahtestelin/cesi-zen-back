package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UpdateCurrentUserDto;

import java.util.List;
import java.util.UUID;

public interface AppUserService {

    AppUserDto getCurrentUser(String mail);

    AppUserDto updateCurrentUser(String currentMail, UpdateCurrentUserDto dto);

    void anonymizeCurrentUser(String currentMail);

    List<AppUserDto> getAllUsers();

    AppUserDto getUserById(UUID id);

    AppUserDto updateUser(UUID id, AppUserDto appUserDto);

    void deleteUser(UUID id);

    AppUserDto disableUser(UUID id);

    AppUserDto enableUser(UUID id);
}