package com.evaluacion.service.impl;

import com.evaluacion.dto.UserRequestDTO;
import com.evaluacion.entity.User;
import com.evaluacion.exception.ValidationException;
import com.evaluacion.mapper.PhoneMapper;
import com.evaluacion.mapper.UserMapper;
import com.evaluacion.repository.UserRepository;
import com.evaluacion.config.JwtUtil;
import com.evaluacion.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static com.evaluacion.mapper.UserMapper.setDatesMofifiedAndLogin;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final Validator validator;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, Validator validator) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.validator = validator;
    }

    @Override
    @Transactional
    public User createUser(UserRequestDTO dto) {
        // 1) Validación de anotaciones JSR-380 en UserRequestDTO
        Set<ConstraintViolation<UserRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String mensaje = violations.iterator().next().getMessage();
            throw new ValidationException(mensaje);
        }

        validateEmailNotRegistered(dto.getEmail());

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
        User user = findUserById(id);

        UUID idAutenticado = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        updateLastLogin(idAutenticado);

        return user;
    }

    @Override
    public User updateUser(UUID id, UserRequestDTO dto) {
        validateOwnership(id);

        User user = findUserById(id);

        validateEmailNotUsedByAnotherUser(dto.getEmail(), user.getEmail());

        UserMapper.updateDtoToEntity(dto, user);

        setDatesMofifiedAndLogin(user);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User partiallyUpdateUser(UUID id, UserRequestDTO dto) {
        validateOwnership(id);

        User user = findUserById(id);

        if (dto.getName() != null) user.setName(dto.getName());


        if (dto.getEmail() != null) {
            validateEmailNotUsedByAnotherUser(dto.getEmail(), user.getEmail());
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null) user.setPassword(dto.getPassword());


        if (dto.getPhones() != null) PhoneMapper.actualizarTelefonosDesdeDTOs(dto.getPhones(), user);


        setDatesMofifiedAndLogin(user);

        return userRepository.save(user);
    }


    @Override
    public void deleteUser(UUID id) {
        validateOwnership(id);
        User user = findUserById(id);

        userRepository.delete(user);
    }

    @Override
    public void updateLastLogin(UUID idUser) {
        userRepository.findById(idUser).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    private void validateOwnership(UUID idUserSolicitado) {
        UUID idAutenticado = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        if (!idAutenticado.equals(idUserSolicitado)) {
            throw new ValidationException("No tiene permisos para modificar este recurso");
        }
    }

    private void validateEmailNotRegistered(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("El correo ya se encuentra registrado");
        }
    }

    private void validateEmailNotUsedByAnotherUser(String nuevoEmail, String emailActual) {
        if (userRepository.existsByEmail(nuevoEmail) && !nuevoEmail.equals(emailActual)) {
            throw new ValidationException("El correo ya está registrado por otro usuario");
        }
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));
    }
}