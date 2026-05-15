package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UpdateCurrentUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UserDataExportDto;

import java.util.List;
import java.util.UUID;

public interface AppUserService {

    AppUserDto getCurrentUser(String mail);

    UserDataExportDto exportCurrentUserData(String mail);

    AppUserDto updateCurrentUser(String currentMail, UpdateCurrentUserDto dto);

    void anonymizeCurrentUser(String currentMail);

    List<AppUserDto> getAllUsers();

    AppUserDto getUserById(UUID id);

    AppUserDto updateUser(UUID id, AppUserDto appUserDto, String adminMail);

    void deleteUser(UUID id, String adminMail);

    AppUserDto disableUser(UUID id, String adminMail);

    AppUserDto enableUser(UUID id, String adminMail);

    AppUserDto promoteUser(UUID id, String adminMail);

    AppUserDto demoteUser(UUID id, String adminMail);
}