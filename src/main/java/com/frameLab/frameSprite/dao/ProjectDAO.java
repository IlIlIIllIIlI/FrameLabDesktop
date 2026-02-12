package com.frameLab.frameSprite.dao;

import com.frameLab.frameSprite.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {
    private final Connection connection;

    public ProjectDAO(Connection connection) {
        this.connection = connection;
        initializeTable();
    }

    private void initializeTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS projects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL UNIQUE,
                user_id INTEGER NOT NULL,
                challenge_id INTEGER NOT NULL,
                project_uri TEXT NOT NULL,
            )
        """;

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create decks table: " + e.getMessage(), e);
        }
    }


    public void save(Project project) {
        String sql = "INSERT INTO projects (title, project_uri) VALUES (?, ?)";

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, project.getTitle());
            pstmt.setString(2, project.getImageUrl());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    project.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save projects: " + e.getMessage(), e);
        }
    }

    public List<Project> getProjectsByChallengeAndUser(int userId, int challengeId){
        ArrayList<Project> projects = new ArrayList<>();
        String sql = "SELECT title,project_uri FROM projects WHERE user_id = ? AND challenge_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2,challengeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                projects.add(
                        new Project(rs.getInt("id"),rs.getString("title"),rs.getString("project_uri"))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find All failed", e);
        }
        return projects;
    }

}
