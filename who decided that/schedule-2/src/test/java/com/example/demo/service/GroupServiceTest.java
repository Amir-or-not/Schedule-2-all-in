package com.example.demo.service;

import com.example.demo.dto.GroupDTO;
import com.example.demo.entity.Group;
import com.example.demo.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupService Tests")
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    private Group testGroup;
    private GroupDTO testGroupDTO;

    @BeforeEach
    void setUp() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        testGroup = new Group("group-1", "schedule-1", "Test Group", "A test group", data);

        testGroupDTO = new GroupDTO();
        testGroupDTO.setGroupId("group-1");
        testGroupDTO.setScheduleId("schedule-1");
        testGroupDTO.setGroupName("Test Group");
        testGroupDTO.setDescription("A test group");
        testGroupDTO.setData(data);
    }

    @Nested
    @DisplayName("getAllGroups")
    class GetAllGroupsTests {

        @Test
        @DisplayName("should return all groups")
        void shouldReturnAllGroups() {
            Group group2 = new Group("group-2", "schedule-1", "Another Group", "Description", new HashMap<>());
            when(groupRepository.findAll()).thenReturn(List.of(testGroup, group2));

            List<GroupDTO> result = groupService.getAllGroups();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getGroupId()).isEqualTo("group-1");
            assertThat(result.get(0).getGroupName()).isEqualTo("Test Group");
            assertThat(result.get(0).getScheduleId()).isEqualTo("schedule-1");
            assertThat(result.get(1).getGroupId()).isEqualTo("group-2");
            verify(groupRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no groups exist")
        void shouldReturnEmptyListWhenNoGroups() {
            when(groupRepository.findAll()).thenReturn(List.of());

            List<GroupDTO> result = groupService.getAllGroups();

            assertThat(result).isEmpty();
            verify(groupRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getGroupById")
    class GetGroupByIdTests {

        @Test
        @DisplayName("should return group when found")
        void shouldReturnGroupWhenFound() {
            when(groupRepository.findById("group-1")).thenReturn(Optional.of(testGroup));

            Optional<GroupDTO> result = groupService.getGroupById("group-1");

            assertThat(result).isPresent();
            assertThat(result.get().getGroupId()).isEqualTo("group-1");
            assertThat(result.get().getGroupName()).isEqualTo("Test Group");
            assertThat(result.get().getDescription()).isEqualTo("A test group");
            verify(groupRepository).findById("group-1");
        }

        @Test
        @DisplayName("should return empty when group not found")
        void shouldReturnEmptyWhenGroupNotFound() {
            when(groupRepository.findById("non-existent")).thenReturn(Optional.empty());

            Optional<GroupDTO> result = groupService.getGroupById("non-existent");

            assertThat(result).isEmpty();
            verify(groupRepository).findById("non-existent");
        }
    }

    @Nested
    @DisplayName("createGroup")
    class CreateGroupTests {

        @Test
        @DisplayName("should create group successfully")
        void shouldCreateGroupSuccessfully() {
            GroupDTO createDTO = new GroupDTO();
            createDTO.setGroupId("group-new");
            createDTO.setScheduleId("schedule-1");
            createDTO.setGroupName("New Group");
            createDTO.setDescription("A new group");

            when(groupRepository.existsById("group-new")).thenReturn(false);
            when(groupRepository.save(any(Group.class))).thenAnswer(inv -> {
                Group g = inv.getArgument(0);
                return g;
            });

            GroupDTO result = groupService.createGroup(createDTO);

            assertThat(result).isNotNull();
            assertThat(result.getGroupId()).isEqualTo("group-new");
            assertThat(result.getGroupName()).isEqualTo("New Group");
            assertThat(result.getScheduleId()).isEqualTo("schedule-1");
            verify(groupRepository).save(any(Group.class));
        }

        @Test
        @DisplayName("should set default schedule ID when not provided")
        void shouldSetDefaultScheduleIdWhenNotProvided() {
            GroupDTO createDTO = new GroupDTO();
            createDTO.setGroupId("group-new");
            createDTO.setGroupName("New Group");

            when(groupRepository.existsById("group-new")).thenReturn(false);
            when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

            GroupDTO result = groupService.createGroup(createDTO);

            assertThat(result.getScheduleId()).isEqualTo("default_schedule");
            verify(groupRepository).save(any(Group.class));
        }

        @Test
        @DisplayName("should throw when group ID already exists")
        void shouldThrowWhenGroupIdExists() {
            when(groupRepository.existsById("group-1")).thenReturn(true);

            assertThatThrownBy(() -> groupService.createGroup(testGroupDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Group with ID group-1 already exists");

            verify(groupRepository, never()).save(any(Group.class));
        }
    }

    @Nested
    @DisplayName("deleteGroup")
    class DeleteGroupTests {

        @Test
        @DisplayName("should delete group when exists")
        void shouldDeleteGroupWhenExists() {
            when(groupRepository.findById("group-1")).thenReturn(Optional.of(testGroup));

            boolean result = groupService.deleteGroup("group-1");

            assertThat(result).isTrue();
            verify(groupRepository).deleteById("group-1");
        }

        @Test
        @DisplayName("should return false when group does not exist")
        void shouldReturnFalseWhenGroupDoesNotExist() {
            when(groupRepository.findById("non-existent")).thenReturn(Optional.empty());

            boolean result = groupService.deleteGroup("non-existent");

            assertThat(result).isFalse();
            verify(groupRepository, never()).deleteById(any());
        }
    }
}
