package com.evaluacion.service.impl;

import com.evaluacion.dto.UserRequestDTO;
import com.evaluacion.entity.Phone;
import com.evaluacion.entity.User;
import com.evaluacion.exception.ValidationException;
import com.evaluacion.mapper.PhoneMapper;
import com.evaluacion.mapper.UserMapper;
import com.evaluacion.repository.UserRepository;
import com.evaluacion.config.JwtUtil;
import com.evaluacion.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public User createUser(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("El correo ya esta registrado");
        }

        User user = UserMapper.toEntity(dto);
        // persistir
        User created = userRepository.save(user);

        String token = jwtUtil.generarToken(created.getId());
        LocalDateTime now = LocalDateTime.now();

        created.setToken(token);
        created.setCreated(now);
        created.setLastLogin(now);
        created.setActive(true);

        // persistir con token y timestamps
        return userRepository.save(created);
    }

    @Override
    @Transactional
    public User getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));

        UUID idAutenticado = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        updateLastLogin(idAutenticado);

        return user;
    }

    @Override
    public User updateUser(UUID id, UserRequestDTO dto) {
        validatePropietario(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("El correo ya esta registrado");
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        if (dto.getPhones() != null) {
            List<Phone> updatedPhones = PhoneMapper.mapearDesdeDTOs(dto.getPhones(), user);
            user.getPhones().clear();
            updatedPhones.forEach(phone -> {
                phone.setUser(user);
                user.getPhones().add(phone);
            });
        }

        LocalDateTime time = LocalDateTime.now();
        user.setModified(time);
        user.setLastLogin(time);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User partiallyUpdateUser(UUID id, UserRequestDTO dto) {
        validatePropietario(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            if (userRepository.existsByEmail(dto.getEmail()) && !user.getEmail().equals(dto.getEmail())) {
                throw new ValidationException("El correo ya está registrado por otro usuario");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null) {
            user.setPassword(dto.getPassword());
        }

        if (dto.getPhones() != null) {
            List<Phone> updatedPhones = PhoneMapper.mapearDesdeDTOs(dto.getPhones(), user);
            user.getPhones().clear();
            updatedPhones.forEach(phone -> {
                phone.setUser(user);
                user.getPhones().add(phone);
            });
        }

        LocalDateTime time = LocalDateTime.now();
        user.setModified(time);
        user.setLastLogin(time);

        return userRepository.save(user);
    }


    @Override
    public void deleteUser(UUID id) {
        validatePropietario(id);
        User existente = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));

        userRepository.delete(existente);
    }

    @Override
    public void updateLastLogin(UUID idUser) {
        userRepository.findById(idUser).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    private void validatePropietario(UUID idUserSolicitado) {
        UUID idAutenticado = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        if (!idAutenticado.equals(idUserSolicitado)) {
            throw new ValidationException("No tiene permisos para modificar este recurso");
        }
    }
}