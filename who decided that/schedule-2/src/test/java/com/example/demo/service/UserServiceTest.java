package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "user-1",
                "John Doe",
                "encodedPassword",
                "john@example.com",
                "group-1",
                "USER"
        );
        testUser.setSubject("Math");
        testUser.setCreatedAt(LocalDateTime.now());

        testUserDTO = new UserDTO(
                "user-1",
                "John Doe",
                "password123",
                "john@example.com",
                "group-1",
                "USER"
        );
        testUserDTO.setSubject("Math");
        testUserDTO.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() {
            User user2 = new User("user-2", "Jane Doe", "pass", "jane@example.com", "group-1", "USER");
            when(userRepository.findAll()).thenReturn(List.of(testUser, user2));

            List<UserDTO> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUserId()).isEqualTo("user-1");
            assertThat(result.get(0).getFullName()).isEqualTo("John Doe");
            assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
            assertThat(result.get(1).getUserId()).isEqualTo("user-2");
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserDTO> result = userService.getAllUsers();

            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            when(userRepository.findByUserId("user-1")).thenReturn(Optional.of(testUser));

            Optional<UserDTO> result = userService.getUserById("user-1");

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo("user-1");
            assertThat(result.get().getFullName()).isEqualTo("John Doe");
            assertThat(result.get().getEmail()).isEqualTo("john@example.com");
            verify(userRepository).findByUserId("user-1");
        }

        @Test
        @DisplayName("should return empty when user not found")
        void shouldReturnEmptyWhenUserNotFound() {
            when(userRepository.findByUserId("non-existent")).thenReturn(Optional.empty());

            Optional<UserDTO> result = userService.getUserById("non-existent");

            assertThat(result).isEmpty();
            verify(userRepository).findByUserId("non-existent");
        }
    }

    @Nested
    @DisplayName("getUserByEmail")
    class GetUserByEmailTests {

        @Test
        @DisplayName("should return user when found by email")
        void shouldReturnUserWhenFoundByEmail() {
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

            Optional<UserDTO> result = userService.getUserByEmail("john@example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("john@example.com");
            assertThat(result.get().getUserId()).isEqualTo("user-1");
            verify(userRepository).findByEmail("john@example.com");
        }

        @Test
        @DisplayName("should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            Optional<UserDTO> result = userService.getUserByEmail("unknown@example.com");

            assertThat(result).isEmpty();
            verify(userRepository).findByEmail("unknown@example.com");
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {

        @Test
        @DisplayName("should create user successfully")
        void shouldCreateUserSuccessfully() {
            when(userRepository.existsById("user-new")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setUserId("user-new");
                return u;
            });

            UserDTO createDTO = new UserDTO("user-new", "New User", "password123", "new@example.com", "group-1", "USER");

            UserDTO result = userService.createUser(createDTO);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo("user-new");
            assertThat(result.getFullName()).isEqualTo("New User");
            assertThat(result.getEmail()).isEqualTo("new@example.com");
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("should throw when user ID already exists")
        void shouldThrowWhenUserIdExists() {
            when(userRepository.existsById("user-1")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(testUserDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User with ID user-1 already exists");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("should throw when email already exists")
        void shouldThrowWhenEmailExists() {
            when(userRepository.existsById("user-new")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            UserDTO createDTO = new UserDTO("user-new", "New User", "password123", "john@example.com", "group-1", "USER");

            assertThatThrownBy(() -> userService.createUser(createDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User with email john@example.com already exists");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("updateUserPartial")
    class UpdateUserPartialTests {

        @Test
        @DisplayName("should update user partial fields")
        void shouldUpdateUserPartialFields() {
            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(groupRepository.existsById("group-2")).thenReturn(true);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            Optional<UserDTO> result = userService.updateUserPartial(
                    "user-1", "John Updated", "john.updated@example.com", "group-2", "TEACHER", "Physics");

            assertThat(result).isPresent();
            assertThat(result.get().getFullName()).isEqualTo("John Updated");
            assertThat(result.get().getEmail()).isEqualTo("john.updated@example.com");
            assertThat(result.get().getGroupId()).isEqualTo("group-2");
            assertThat(result.get().getRole()).isEqualTo("TEACHER");
            assertThat(result.get().getSubject()).isEqualTo("Physics");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should return empty when user not found")
        void shouldReturnEmptyWhenUserNotFoundForPartialUpdate() {
            when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

            Optional<UserDTO> result = userService.updateUserPartial("non-existent", "Name", null, null, null, null);

            assertThat(result).isEmpty();
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("should throw when email already exists for another user")
        void shouldThrowWhenEmailExistsForPartialUpdate() {
            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUserPartial("user-1", null, "taken@example.com", null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User with email taken@example.com already exists");
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete user when exists")
        void shouldDeleteUserWhenExists() {
            when(userRepository.existsById("user-1")).thenReturn(true);

            boolean result = userService.deleteUser("user-1");

            assertThat(result).isTrue();
            verify(userRepository).deleteById("user-1");
        }

        @Test
        @DisplayName("should return false when user does not exist")
        void shouldReturnFalseWhenUserDoesNotExist() {
            when(userRepository.existsById("non-existent")).thenReturn(false);

            boolean result = userService.deleteUser("non-existent");

            assertThat(result).isFalse();
            verify(userRepository, never()).deleteById(anyString());
        }
    }
}
