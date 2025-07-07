package com.evaluacion.util.testdata;

import com.evaluacion.dto.PhoneDTO;
import com.evaluacion.dto.UserRequestDTO;
import com.evaluacion.entity.Phone;
import com.evaluacion.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    public static UserRequestDTO defaultUserRequest() {
        List<PhoneDTO> phones = new ArrayList<>();
        phones.add(new PhoneDTO("1234567", "1", "57"));
        return new UserRequestDTO(
                "Jorge",
                "jorge@example.com",
                "Password1",
                phones
        );
    }

    public static UserRequestDTO userRequestWithEmail(String email) {
        List<PhoneDTO> phones = new ArrayList<>();
        phones.add(new PhoneDTO("1234567", "1", "57"));
        return new UserRequestDTO(
                "Jorge Jorgepiedra",
                email,
                "Password1",
                phones
        );
    }

    public static User defaultUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName("Jorge");
        user.setEmail("jorge@example.com");
        user.setPassword("Password1");
        user.setToken("mock-token");
        user.setActive(true);
        user.setCreated(LocalDateTime.now());
        user.setModified(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());

        List<Phone> phones = new ArrayList<>();
        phones.add(new Phone("1234567", "1", "57", user));
        user.setPhones(phones);

        return user;
    }
}
