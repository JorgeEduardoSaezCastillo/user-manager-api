package com.evaluacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneDTO {

    @NotBlank(message = "El número es obligatorio")
    private String number;

    @NotBlank(message = "El código de ciudad es obligatorio")
    private String cityCode;

    @NotBlank(message = "El código de país es obligatorio")
    private String countryCode;
}
