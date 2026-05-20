package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.dao.ProjectDAO;
import com.frameLab.frameSprite.model.Challenge;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.model.User;
import com.frameLab.frameSprite.utils.SessionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectsServiceTest {

    private ProjectsService projectsService;
    private ProjectDAO mockDao;
    private StorageService mockStorageService;

    private MockedStatic<SessionUtils> mockedSessionUtils;
    private MockedStatic<Main> mockedMain;
    private SessionUtils mockCache;

    @BeforeEach
    void setUp() {
        mockDao = mock(ProjectDAO.class);
        mockStorageService = mock(StorageService.class);

        projectsService = new ProjectsService(mockDao, mockStorageService);

        mockCache = mock(SessionUtils.class);
        mockedSessionUtils = mockStatic(SessionUtils.class);
        mockedSessionUtils.when(SessionUtils::getInstance).thenReturn(mockCache);
    }

    @AfterEach
    void tearDown() {
        if (mockedSessionUtils != null) {
            mockedSessionUtils.close();
        }
    }

    @Test
    void shouldReturnProjectsFromCacheIfAvailable() throws IOException {
        // ARRANGE
        User mockUser = new User();
        List<Project> cachedProjects = new ArrayList<>();
        cachedProjects.add(new Project(1, "Cached Project"));
        mockUser.setProjects(cachedProjects);

        when(mockCache.getUser()).thenReturn(mockUser);

        // ACT
        List<Project> result = projectsService.getProjectsByUserAndChallenge(1, 10);

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("Cached Project", result.getFirst().getTitle());
        verifyNoInteractions(mockDao);
    }

    @Test
    void shouldFetchProjectsFromDaoWhenCacheIsEmpty() throws IOException {
        // ARRANGE
        User mockUser = new User();
        when(mockCache.getUser()).thenReturn(mockUser);

        List<Project> dbProjects = new ArrayList<>();
        dbProjects.add(new Project(1, "DB Project"));

        when(mockDao.getProjectsByChallengeAndUser(1, 10)).thenReturn(dbProjects);
        when(mockStorageService.getPreviewPath(1)).thenReturn("path/to/preview.png");

        // ACT
        List<Project> result = projectsService.getProjectsByUserAndChallenge(1, 10);

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("path/to/preview.png", result.getFirst().getImageUrl());
        assertEquals(result, mockUser.getProjects());
    }

    @Test
    void shouldLoadProjectFiles() throws IOException {
        // ARRANGE
        Project project = new Project(1, "To Load");

        // ACT
        projectsService.loadProject(project);

        // ASSERT
        verify(mockStorageService, times(1)).loadFiles(project);
    }

    @Test
    void shouldSaveNewProjectAndAssignIdsFromSession() throws IOException {
        // ARRANGE
        Project project = new Project(0, "New Project");

        User mockUser = new User();
        mockUser.setId(42);
        when(mockCache.getUser()).thenReturn(mockUser);

        Challenge mockChallenge = new Challenge();
        mockChallenge.setId(10);
        when(mockCache.getChallenge()).thenReturn(mockChallenge);

        // ACT
        projectsService.saveProject(project);

        // ASSERT
        assertEquals(42, project.getUserId());
        assertEquals(10, project.getChallengeId());
        verify(mockDao, times(1)).save(project);
        verify(mockStorageService, times(1)).saveFiles(project);
    }

    @Test
    void shouldSaveExistingProjectWithoutModifyingIds() throws IOException {
        // ARRANGE
        Project project = new Project(5, "Existing Project");
        project.setUserId(99);
        project.setChallengeId(88);

        // ACT
        projectsService.saveProject(project);

        // ASSERT
        assertEquals(99, project.getUserId());
        assertEquals(88, project.getChallengeId());
        verify(mockDao, times(1)).save(project);
        verify(mockStorageService, times(1)).saveFiles(project);
    }

    @Test
    void shouldDeleteProjectAndRemoveFromCache() throws IOException {
        // ARRANGE
        Project project = new Project(1, "To Delete");

        User mockUser = new User();
        List<Project> userProjects = new ArrayList<>();
        userProjects.add(project);
        mockUser.setProjects(userProjects);

        when(mockCache.getUser()).thenReturn(mockUser);

        // ACT
        projectsService.deleteProject(project);

        // ASSERT
        verify(mockDao, times(1)).delete(1);
        verify(mockStorageService, times(1)).deleteProjectFiles(1);
        assertTrue(mockUser.getProjects().isEmpty());
    }
}