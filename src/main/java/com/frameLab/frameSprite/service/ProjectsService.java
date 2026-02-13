package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.dao.ProjectDAO;
import com.frameLab.frameSprite.model.Project;
import com.frameLab.frameSprite.model.User;
import com.frameLab.frameSprite.utils.SessionUtils;

import java.io.IOException;
import java.util.List;

public class ProjectsService {
    ProjectDAO dao;
    StorageService storageService;

    public ProjectsService() {
        dao = new ProjectDAO();
        storageService = new StorageService();
    }

    public List<Project> getProjectsByUserAndChallenge(int userId, int challengeId) throws IOException {
        User userCache = SessionUtils.getInstance().getUser();
        if (userCache == null || userCache.getProjects() == null) {
            List<Project> projects = dao.getProjectsByChallengeAndUser(userId,challengeId);

            for (Project project : projects){
                project.setImageUrl(storageService.getPreviewPath(project.getId()));
            }
            if (userCache != null) {
                userCache.setProjects(projects);
            }
            return  projects;
        }

        return userCache.getProjects();
    }

    public void loadProject(Project project) throws IOException {
        storageService.loadFiles(project);
    }

    public void saveProject(Project project) throws IOException {
        if (project.getId() == 0) {
            project.setUserId(SessionUtils.getInstance().getUser().getId());
            project.setChallengeId(SessionUtils.getInstance().getChallenge().getId());
        }

        dao.save(project);

        storageService.saveFiles(project);
    }

}
