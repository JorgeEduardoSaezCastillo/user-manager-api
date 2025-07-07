// UserControllerTest.java
package com.evaluacion.controller;

import com.evaluacion.dto.UserRequestDTO;
import com.evaluacion.entity.User;
import com.evaluacion.exception.GlobalExceptionHandler;
import com.evaluacion.service.UserService;
import com.evaluacion.util.testdata.TestDataFactory;
import com.evaluacion.validation.ValidationGetUser;
import com.evaluacion.validation.ValidationUser;
import com.evaluacion.validation.ValidationUserDelete;
import com.evaluacion.validation.ValidationUserPatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @Mock
    private ValidationUser validationUser;

    @Mock
    private ValidationUserPatch validationUserPatch;

    @Mock
    private ValidationUserDelete validationUserDelete;

    @Mock
    private ValidationGetUser validationGetUser;

    @InjectMocks
    private UserController userController;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testCrearUsuario_Success() throws Exception {
        UserRequestDTO requestDTO = TestDataFactory.defaultUserRequest();
        String requestJson = objectMapper.writeValueAsString(requestDTO);

        User usuario = TestDataFactory.defaultUser(userId);

        when(userService.createUser(any())).thenReturn(usuario);

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value(usuario.getName()))
                .andExpect(jsonPath("$.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.token").value(usuario.getToken()));
    }

    @Test
    public void testObtenerUsuarioPorId_Success() throws Exception {
        User usuario = TestDataFactory.defaultUser(userId);

        when(userService.getUser(eq(userId))).thenReturn(usuario);

        mockMvc.perform(get("/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value(usuario.getName()))
                .andExpect(jsonPath("$.email").value(usuario.getEmail()));
    }

    @Test
    public void testReemplazarUsuario_Success() throws Exception {
        UserRequestDTO requestDTO = TestDataFactory.userRequestWithEmail("jorge.updated@saez.org");
        String requestJson = objectMapper.writeValueAsString(requestDTO);

        User usuario = TestDataFactory.defaultUser(userId);
        usuario.setName("Jorge Jorgesaez Updated");
        usuario.setEmail("jorge.updated@saez.org");

        when(userService.updateUser(eq(userId), any())).thenReturn(usuario);

        mockMvc.perform(put("/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Jorge Jorgesaez Updated"))
                .andExpect(jsonPath("$.email").value("jorge.updated@saez.org"));
    }

    @Test
    public void testActualizarParcialmenteUsuario_Success() throws Exception {

        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setName("Jorge Jorgesaez Partial");
        requestDTO.setPhones(Collections.emptyList());

        String requestJson = objectMapper.writeValueAsString(requestDTO);

        User usuario = TestDataFactory.defaultUser(userId);
        usuario.setName("Jorge Jorgesaez Partial");

        when(userService.partiallyUpdateUser(eq(userId), any())).thenReturn(usuario);

        mockMvc.perform(patch("/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Jorge Jorgesaez Partial"));
    }

    @Test
    public void testEliminarUsuario_Success() throws Exception {
        mockMvc.perform(delete("/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
