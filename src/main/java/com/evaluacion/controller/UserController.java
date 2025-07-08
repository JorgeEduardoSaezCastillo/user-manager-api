package com.evaluacion.controller;


import com.evaluacion.dto.UserRequestDTO;
import com.evaluacion.entity.User;
import com.evaluacion.mapper.UserMapper;
import com.evaluacion.service.UserService;
import com.evaluacion.usuariosapi.api.UserApi;
import com.evaluacion.usuariosapi.dto.PublicUserResponse;
import com.evaluacion.usuariosapi.dto.UserRequest;
import com.evaluacion.usuariosapi.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador para gestionar las operaciones relacionadas con los usuarios.
 * Implementa los endpoints definidos en la interfaz generada por OpenAPI.
 */
@RestController
public class UserController implements UserApi {

    private final UserService userService;



    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param userRequest Objeto con los datos del usuario a crear.
     * @return ResponseEntity con la información del usuario creado.
     */
    @Override
    public ResponseEntity<UserResponse> createUser(@Validated UserRequest userRequest) {
        UserRequestDTO userDTO = UserMapper.toRequestDTO(userRequest);
        User usuarioCreado = userService.createUser(userDTO);
        UserResponse response = UserMapper.toResponse(usuarioCreado);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Elimina un usuario existente del sistema.
     *
     * @param id UUID del usuario a eliminar.
     * @return ResponseEntity sin contenido si se elimina correctamente.
     */
    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id UUID del usuario a obtener.
     * @return ResponseEntity con la información del usuario encontrado.
     */
    @Override
    public ResponseEntity<PublicUserResponse> getUser(UUID id) {
        User usuario = userService.getUser(id);
        PublicUserResponse response = UserMapper.toPublicResponse(usuario);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza parcialmente los datos de un usuario existente.
     *
     * @param id          UUID del usuario a actualizar parcialmente.
     * @param userRequest Objeto con los datos a modificar.
     * @return ResponseEntity con la información del usuario actualizado.
     */
    @Override
    public ResponseEntity<PublicUserResponse> partiallyUpdateUser(UUID id, @Validated UserRequest userRequest) {
        UserRequestDTO dto = UserMapper.toRequestDTO(userRequest);
        User actualizado = userService.partiallyUpdateUser(id, dto);
        PublicUserResponse response = UserMapper.toPublicResponse(actualizado);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza completamente los datos de un usuario existente.
     *
     * @param id          UUID del usuario a actualizar.
     * @param userRequest Objeto con los nuevos datos del usuario.
     * @return ResponseEntity con la información del usuario actualizado.
     */
    @Override
    public ResponseEntity<PublicUserResponse> updateUser(UUID id, @Validated UserRequest userRequest) {
        UserRequestDTO dto = UserMapper.toRequestDTO(userRequest);
        User actualizado = userService.updateUser(id,dto);
        PublicUserResponse response = UserMapper.toPublicResponse(actualizado);
        return ResponseEntity.ok(response);
    }
}
