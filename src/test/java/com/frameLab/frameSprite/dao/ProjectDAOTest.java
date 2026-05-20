package com.frameLab.frameSprite.dao;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class ProjectDAOTest {
    private Connection connection;
    private ProjectDAO projectDAO;
    private MockedStatic<Main> mockedMain;

    @BeforeEach
    void setUp() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        this.mockedMain = mockStatic(Main.class);
        this.mockedMain.when(Main::getConnection).thenReturn(this.connection);

        this.projectDAO = new ProjectDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        this.mockedMain.close();
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
        }
    }

    @Test
    void shouldInsertNewProject() {
        // ARRANGE
        Project project = new Project(0, "New Project", 1, 10, 800, 600);

        // ACT
        projectDAO.save(project);

        // ASSERT
        assertNotEquals(0, project.getId());
        Optional<Project> savedProject = projectDAO.findById(project.getId());
        assertTrue(savedProject.isPresent());
        assertEquals("New Project", savedProject.get().getTitle());
    }

    @Test
    void shouldUpdateExistingProject() {
        // ARRANGE
        Project project = new Project(0, "Old Title", 1, 10, 800, 600);
        projectDAO.save(project);

        project.setTitle("Updated Title");
        project.setWidth(1024);

        // ACT
        projectDAO.save(project);

        // ASSERT
        Optional<Project> updatedProject = projectDAO.findById(project.getId());
        assertTrue(updatedProject.isPresent());
        assertEquals("Updated Title", updatedProject.get().getTitle());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProject() {
        // ARRANGE
        Project fakeProject = new Project(999, "Project", 1, 10, 800, 600);

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectDAO.update(fakeProject)
        );
        assertEquals("Project not found", exception.getMessage());
    }

    @Test
    void shouldGetProjectsByChallengeAndUser() {
        // ARRANGE
        projectDAO.save(new Project(0, "Project 1", 1, 5, 800, 600));
        projectDAO.save(new Project(0, "Project 2", 1, 5, 800, 600));
        projectDAO.save(new Project(0, "Project 3", 2, 5, 800, 600));
        projectDAO.save(new Project(0, "Project 4", 1, 9, 800, 600));

        // ACT
        List<Project> user1Challenge5Projects = projectDAO.getProjectsByChallengeAndUser(1, 5);

        // ASSERT
        assertEquals(2, user1Challenge5Projects.size());
        assertEquals("Project 1", user1Challenge5Projects.get(0).getTitle());
        assertEquals("Project 2", user1Challenge5Projects.get(1).getTitle());
    }

    @Test
    void shouldFindById() {
        // ARRANGE
        Project project = new Project(0, "Test", 1, 10, 800, 600);
        projectDAO.save(project);

        // ACT
        Optional<Project> foundProject = projectDAO.findById(project.getId());

        // ASSERT
        assertTrue(foundProject.isPresent());
        assertEquals(project.getId(), foundProject.get().getId());
        assertEquals("Test", foundProject.get().getTitle());
    }

    @Test
    void shouldReturnEmptyWhenFindByIdFails() {
        // ACT
        Optional<Project> notFound = projectDAO.findById(999);

        // ASSERT
        assertTrue(notFound.isEmpty());
    }

    @Test
    void shouldDeleteProject() {
        // ARRANGE
        Project project = new Project(0, "Deleted", 1, 10, 800, 600);
        projectDAO.save(project);
        int savedId = project.getId();

        // ACT
        projectDAO.delete(savedId);

        // ASSERT
        Optional<Project> deletedProject = projectDAO.findById(savedId);
        assertTrue(deletedProject.isEmpty());
    }
}