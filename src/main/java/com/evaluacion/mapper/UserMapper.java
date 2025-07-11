package com.evaluacion.mapper;

import com.evaluacion.dto.PhoneDTO;
import com.evaluacion.dto.UserRequestDTO;
import com.evaluacion.entity.Phone;
import com.evaluacion.entity.User;
import com.evaluacion.usuariosapi.dto.PublicUserResponse;
import com.evaluacion.usuariosapi.dto.UserRequest;
import com.evaluacion.usuariosapi.dto.UserResponse;
import com.evaluacion.util.DateUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {


    public static UserRequestDTO toRequestDTO(UserRequest request){
        return new UserRequestDTO(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhones().stream()
                        .map(t -> new PhoneDTO(t.getNumber(), t.getCityCode(), t.getCountryCode()))
                        .collect(Collectors.toList())
        );
    }

    public static User toEntity (UserRequestDTO dto){
        User usuario = new User();

        usuario.setName(dto.getName());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());

        List<Phone> telefonos = PhoneMapper.mapearDesdeDTOs(dto.getPhones(), usuario);
        usuario.setPhones(telefonos);

        return usuario;
    }

    public static UserResponse toResponse(User usuario) {

        UserResponse response = new UserResponse();

        response.setId(usuario.getId());
        response.setCreated(DateUtils.toOffsetDateTime(usuario.getCreated()));
        response.setModified(DateUtils.toOffsetDateTime(usuario.getModified()));
        response.setLastLogin(DateUtils.toOffsetDateTime(usuario.getLastLogin()));
        response.setToken(usuario.getToken());
        response.setIsActive(usuario.isActive());
        response.setName(usuario.getName());
        response.setEmail(usuario.getEmail());
        response.setPhones(
                usuario.getPhones().stream()
                        .map(t -> {
                            var tel = new com.evaluacion.usuariosapi.dto.Phone();
                            tel.setNumber(t.getNumber());
                            tel.setCityCode(t.getCityCode());
                            tel.setCountryCode(t.getCountryCode());
                            return tel;
                        }).collect(Collectors.toList())
        );
        return response;
    }

    public static PublicUserResponse toPublicResponse(User usuario){
        PublicUserResponse response = new PublicUserResponse();

        response.setId(usuario.getId());
        response.setCreated(DateUtils.toOffsetDateTime(usuario.getCreated()));
        response.setModified(DateUtils.toOffsetDateTime(usuario.getModified()));
        response.setLastLogin(DateUtils.toOffsetDateTime(usuario.getLastLogin()));
        response.setIsActive(usuario.isActive());
        response.setName(usuario.getName());
        response.setEmail(usuario.getEmail());
        response.setPhones(
                usuario.getPhones().stream()
                        .map( t ->{
                            com.evaluacion.usuariosapi.dto.Phone tel = new com.evaluacion.usuariosapi.dto.Phone();
                            tel.setNumber(t.getNumber());
                            tel.setCityCode(t.getCityCode());
                            tel.setCountryCode(t.getCountryCode());
                            return tel;
                        }).collect(Collectors.toList())
        );
        return response;
    }

    public static void updateDtoToEntity(UserRequestDTO dto, User user) {
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
    }

    public static void setDatesMofifiedAndLogin(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setModified(now);
        user.setLastLogin(now);
    }
}
