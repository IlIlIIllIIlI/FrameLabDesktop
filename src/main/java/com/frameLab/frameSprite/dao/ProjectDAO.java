package com.frameLab.frameSprite.dao;

import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectDAO {
    private final Connection connection;

    public ProjectDAO() {
        this.connection = Main.getConnection();
        initializeTable();
    }

    private void initializeTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS projects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                user_id INTEGER NOT NULL,
                challenge_id INTEGER NOT NULL,
                height INTEGER DEFAULT 600,
                width INTEGER DEFAULT 800,
                last_modified DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create projects table: " + e.getMessage(), e);
        }
    }

    public void save(Project project){
        if (exists(project)){
            update(project);
        } else{
            insert(project);
        }
    }

    private void insert(Project project) {
        String sql = "INSERT INTO projects (title,user_id,challenge_id,height,width) VALUES (?, ?,?,?,?)";

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, project.getTitle());
            pstmt.setInt(2, project.getUserId());
            pstmt.setInt(3,project.getChallengeId());
            pstmt.setInt(4,project.getHeight());
            pstmt.setInt(5,project.getWidth());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    project.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert projects: " + e.getMessage(), e);
        }
    }

    public List<Project> getProjectsByChallengeAndUser(int userId, int challengeId){
        ArrayList<Project> projects = new ArrayList<>();
        String sql = "SELECT id, title,height,width FROM projects WHERE user_id = ? AND challenge_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2,challengeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                projects.add(
                        new Project(rs.getInt("id"),rs.getString("title"),userId,challengeId,
                                rs.getInt("width"),rs.getInt("height"))
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find All failed", e);
        }
        return projects;
    }

    public void update(Project project) {
        String sql = "UPDATE projects SET title = ?, height= ?,width = ?,last_modified = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, project.getTitle());
            pstmt.setInt(2, project.getHeight());
            pstmt.setInt(3, project.getWidth());
            pstmt.setInt(4, project.getId());
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Project not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Update failed", e);
        }
    }

    public Optional<Project> findById(int id) {
        String sql = "SELECT id, title FROM projects WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Project(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Find failed", e);
        }
    }

    private boolean exists(Project project){
        return project.getId() != 0;
    }

}
