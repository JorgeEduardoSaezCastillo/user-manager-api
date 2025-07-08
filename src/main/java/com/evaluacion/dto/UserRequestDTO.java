package com.evaluacion.dto;

import com.evaluacion.validation.ValidEmail;
import com.evaluacion.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @ValidEmail
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @ValidPassword
    private String password;

    @NotEmpty(message = "Debe incluir al menos un teléfono")
    private List<PhoneDTO> phones;
}