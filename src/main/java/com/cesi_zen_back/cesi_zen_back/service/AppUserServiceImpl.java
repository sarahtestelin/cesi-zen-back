package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import com.cesi_zen_back.cesi_zen_back.mapper.UserMapper;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;

    @Override
    public AppUserDto getCurrentUser(String mail) {
        AppUser user = appUserRepository.findByMail(mail)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        return UserMapper.toDto(user);
    }

    @Override
    public List<AppUserDto> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public AppUserDto getUserById(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        return UserMapper.toDto(user);
    }

    @Override
    public AppUserDto updateUser(UUID id, AppUserDto appUserDto) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        user.setMail(appUserDto.mail());
        user.setPseudo(appUserDto.pseudo());
        user.setActive(appUserDto.appUserIsActive());

        return UserMapper.toDto(appUserRepository.save(user));
    }

    @Override
    public void deleteUser(UUID id) {
        if (!appUserRepository.existsById(id)) {
            throw new BadRequestException("Utilisateur introuvable.");
        }

        appUserRepository.deleteById(id);
    }

    @Override
    public AppUserDto disableUser(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        user.setActive(false);

        return UserMapper.toDto(appUserRepository.save(user));
    }

    @Override
    public AppUserDto enableUser(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        user.setActive(true);

        return UserMapper.toDto(appUserRepository.save(user));
    }
}