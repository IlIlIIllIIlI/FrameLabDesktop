package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.dao.ProjectDAO;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.model.User;
import com.frameLab.frameSprite.utils.SessionUtils;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class ProjectsService {
    ProjectDAO dao;

    public ProjectsService() throws SQLException {
        dao = new ProjectDAO(DriverManager.getConnection("jdbc:sqlite:frameSprite.db"));
    }

    public List<Project> getProjectsByUserAndChallenge(int userId, int challengeId) throws IOException {
        User userCache = SessionUtils.getInstance().getUser();
        if (userCache == null || userCache.getProjects() == null) {
            List<Project> projects = dao.getProjectsByChallengeAndUser(userId,challengeId);
            if (userCache != null) {
                userCache.setProjects(projects);
            }
            return  projects;
        }

        return userCache.getProjects();
    }
}
