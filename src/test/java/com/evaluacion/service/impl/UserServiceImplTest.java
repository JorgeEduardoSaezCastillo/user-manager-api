package com.evaluacion.service.impl;

import com.evaluacion.config.JwtUtil;
import com.evaluacion.dto.PhoneDTO;
import com.evaluacion.dto.UserRequestDTO;
import com.evaluacion.entity.User;
import com.evaluacion.exception.ValidationException;
import com.evaluacion.repository.UserRepository;
import com.evaluacion.util.testdata.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl usuarioService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreatedUser_Success() {
        UserRequestDTO dto = TestDataFactory.defaultUserRequest();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);

        User usuarioSinToken = TestDataFactory.defaultUser(userId);
        usuarioSinToken.setId(userId);
        usuarioSinToken.setCreated(LocalDateTime.now());
        usuarioSinToken.setModified(LocalDateTime.now());
        usuarioSinToken.setLastLogin(LocalDateTime.now());

        when(jwtUtil.generarToken(eq(userId))).thenReturn("token-value");

        when(userRepository.save(any(User.class)))
                .thenReturn(usuarioSinToken)
                .thenAnswer(invocation -> invocation.getArgument(0));

        User resultado = usuarioService.createUser(dto);

        verify(userRepository).existsByEmail(dto.getEmail());
        verify(userRepository, times(2)).save(any(User.class));
        verify(jwtUtil).generarToken(eq(userId));

        assertEquals("Jorge", resultado.getName());
        assertEquals("jorge@example.com", resultado.getEmail());
        assertEquals("token-value", resultado.getToken());
        assertTrue(resultado.isActive());
        assertNotNull(resultado.getModified());
        assertNotNull(resultado.getLastLogin());
    }

    @Test
    void testCreatedUser_DuplicateCorreo() {
        UserRequestDTO dto = TestDataFactory.defaultUserRequest();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            usuarioService.createUser(dto);
        });
        assertEquals("El correo ya esta registrado", exception.getMessage());
    }

    @Test
    void testGetUser_Success() {
        User usuario = TestDataFactory.defaultUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(userRepository.save(any(User.class))).thenReturn(usuario);

        User resultado = usuarioService.getUser(userId);

        verify(userRepository, atLeastOnce()).findById(userId);
        assertEquals("Jorge", resultado.getName());
        assertEquals("jorge@example.com", resultado.getEmail());
    }

    @Test
    void testUpdateUser_Success() {
        User existente = TestDataFactory.defaultUser(userId);
        existente.setEmail("viejo@ejemplo.com");

        UserRequestDTO dto = new UserRequestDTO(
                "Jorge Jorgesaez Updated",
                "jorge.updated@saez.org",
                "Password1",
                new ArrayList<>(List.of(new PhoneDTO("123123123", "3", "57")))
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existente));
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User resultado = usuarioService.updateUser(userId, dto);

        assertEquals("Jorge Jorgesaez Updated", resultado.getName());
        assertEquals("jorge.updated@saez.org", resultado.getEmail());
        assertEquals(1, resultado.getPhones().size());
        assertEquals("123123123", resultado.getPhones().get(0).getNumber());
    }

    @Test
    void testUpdateUser_DuplicateCorreo() {
        User existente = TestDataFactory.defaultUser(userId);
        existente.setEmail("original@mail.com");

        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("otro@mail.com");
        dto.setName("Nuevo");
        dto.setPassword("NuevaPass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existente));
        when(userRepository.existsByEmail("otro@mail.com")).thenReturn(true);

        assertThrows(ValidationException.class, () -> usuarioService.updateUser(userId, dto));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Nombre");
        dto.setEmail("email@dominio.com");
        dto.setPassword("pass");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> usuarioService.updateUser(userId, dto));
    }

    @Test
    void testUpdateUser_PhonesNull() {
        User existente = TestDataFactory.defaultUser(userId);

        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Nombre");
        dto.setEmail("nuevo@mail.com");
        dto.setPassword("pass");
        dto.setPhones(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existente));
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User resultado = usuarioService.updateUser(userId, dto);

        assertEquals("nuevo@mail.com", resultado.getEmail());
        assertEquals("Nombre", resultado.getName());
    }

    @Test
    void testDeleteUser_Success() {
        User existente = TestDataFactory.defaultUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existente));

        usuarioService.deleteUser(userId);

        verify(userRepository).delete(existente);
    }
}
