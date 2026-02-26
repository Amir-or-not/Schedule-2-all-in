package com.example.demo.controller;

import com.example.demotest.ControllerTestApplication;
import com.example.demo.dto.GroupDTO;
import com.example.demo.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ControllerTestApplication.class)
@AutoConfigureMockMvc
@DisplayName("GroupController Tests")
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupService groupService;

    // AuthController is in the same context; these mocks satisfy its dependencies
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private com.example.demo.repository.UserRepository userRepository;
    @MockBean
    private com.example.demo.repository.GroupRepository groupRepository;
    @MockBean
    private com.example.demo.security.jwt.JwtUtils jwtUtils;

    @Nested
    @DisplayName("GET /api/groups")
    class GetAllGroupsTests {

        @Test
        @WithMockUser
        @DisplayName("should return all groups")
        void shouldReturnAllGroups() throws Exception {
            GroupDTO group1 = new GroupDTO();
            group1.setGroupId("group-1");
            group1.setGroupName("Group 1");
            group1.setScheduleId("schedule-1");
            GroupDTO group2 = new GroupDTO();
            group2.setGroupId("group-2");
            group2.setGroupName("Group 2");
            group2.setScheduleId("schedule-1");

            when(groupService.getAllGroups()).thenReturn(List.of(group1, group2));

            mockMvc.perform(get("/api/groups"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].groupId").value("group-1"))
                    .andExpect(jsonPath("$[0].groupName").value("Group 1"))
                    .andExpect(jsonPath("$[1].groupId").value("group-2"));

            verify(groupService).getAllGroups();
        }
    }

    @Nested
    @DisplayName("GET /api/groups/{id}")
    class GetGroupByIdTests {

        @Test
        @WithMockUser
        @DisplayName("should return group when found")
        void shouldReturnGroupWhenFound() throws Exception {
            GroupDTO group = new GroupDTO();
            group.setGroupId("group-1");
            group.setGroupName("Test Group");
            group.setScheduleId("schedule-1");
            group.setDescription("A test group");

            when(groupService.getGroupById("group-1")).thenReturn(Optional.of(group));

            mockMvc.perform(get("/api/groups/group-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.groupId").value("group-1"))
                    .andExpect(jsonPath("$.groupName").value("Test Group"))
                    .andExpect(jsonPath("$.description").value("A test group"));

            verify(groupService).getGroupById("group-1");
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when group not found")
        void shouldReturn404WhenGroupNotFound() throws Exception {
            when(groupService.getGroupById("non-existent")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/groups/non-existent"))
                    .andExpect(status().isNotFound());

            verify(groupService).getGroupById("non-existent");
        }
    }

    @Nested
    @DisplayName("POST /api/groups")
    class CreateGroupTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should create group when admin")
        void shouldCreateGroupWhenAdmin() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("groupId", "group-new");
            request.put("groupName", "New Group");
            request.put("description", "A new group");
            request.put("scheduleId", "schedule-1");

            GroupDTO createdGroup = new GroupDTO();
            createdGroup.setGroupId("group-new");
            createdGroup.setGroupName("New Group");
            createdGroup.setScheduleId("schedule-1");
            createdGroup.setDescription("A new group");

            when(groupService.createGroup(any(GroupDTO.class))).thenReturn(createdGroup);

            mockMvc.perform(post("/api/groups")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.groupId").value("group-new"))
                    .andExpect(jsonPath("$.groupName").value("New Group"));

            verify(groupService).createGroup(any(GroupDTO.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 403 when non-admin tries to create group")
        void shouldReturn403WhenNonAdminCreatesGroup() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("groupId", "group-new");
            request.put("groupName", "New Group");

            mockMvc.perform(post("/api/groups")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verify(groupService, never()).createGroup(any(GroupDTO.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/groups/{id}")
    class DeleteGroupTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should delete group when admin")
        void shouldDeleteGroupWhenAdmin() throws Exception {
            when(groupService.deleteGroup("group-1")).thenReturn(true);

            mockMvc.perform(delete("/api/groups/group-1")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(groupService).deleteGroup("group-1");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 404 when group not found for delete")
        void shouldReturn404WhenGroupNotFoundForDelete() throws Exception {
            when(groupService.deleteGroup("non-existent")).thenReturn(false);

            mockMvc.perform(delete("/api/groups/non-existent")
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(groupService).deleteGroup("non-existent");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 403 when non-admin tries to delete group")
        void shouldReturn403WhenNonAdminDeletesGroup() throws Exception {
            mockMvc.perform(delete("/api/groups/group-1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(groupService, never()).deleteGroup(any());
        }
    }
}
