package com.frameLab.frameSprite.service;


import atlantafx.base.theme.NordDark;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frameLab.frameSprite.Main;
import com.frameLab.frameSprite.dto.OptionsDTO;
import com.frameLab.frameSprite.utils.Actions;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SettingsService {

    private static SettingsService instance;
    private final EnumMap<Actions,KeyCombination> defaultBinds = new EnumMap<>(Actions.class);
    private final EnumMap<Actions,KeyCombination> userBinds = new EnumMap<>(Actions.class);
    private String currentTheme;

    private static final String OPTION_FILE = "options.json";

    private SettingsService(){
        defaultBinds.put(Actions.SAVE, new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN));
        defaultBinds.put(Actions.EXPORT, new KeyCodeCombination(KeyCode.E,KeyCombination.CONTROL_DOWN));
        defaultBinds.put(Actions.TOOL_ERASER, new KeyCodeCombination(KeyCode.E));
        defaultBinds.put(Actions.UNDO, new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN));
        defaultBinds.put(Actions.REDO, new KeyCodeCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN,KeyCombination.SHIFT_DOWN));
        defaultBinds.put(Actions.TOOL_BRUSH, new KeyCodeCombination(KeyCode.B));
        defaultBinds.put(Actions.EMPTY_LAYER, new KeyCodeCombination(KeyCode.L,KeyCombination.CONTROL_DOWN));
        defaultBinds.put(Actions.DUPLICATE, new KeyCodeCombination(KeyCode.D,KeyCombination.CONTROL_DOWN));

        currentTheme = new NordDark().getUserAgentStylesheet();
        loadUserPreferences();
    }

    public static SettingsService getInstance()  {
        if (instance == null) {
            instance = new SettingsService();
        }
        return instance;
    }

    public void save() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            OptionsDTO options = new OptionsDTO(Main.getTheme());
            for (Map.Entry<Actions, KeyCombination> entry : userBinds.entrySet()) {
                options.addKeybinds(entry.getKey().name(), entry.getValue().getName());
            }
            Files.writeString(Path.of(OPTION_FILE), mapper.writeValueAsString(options));

        } catch (Exception e) {
            System.err.println("Failed to save options: " + e.getMessage());
        }
    }

    private void loadUserPreferences() {
        try {
            Path path = Path.of(OPTION_FILE);
            if (!Files.exists(path)) return;

            ObjectMapper mapper = new ObjectMapper();
            String json = Files.readString(path);

           OptionsDTO options = mapper.readValue(json, OptionsDTO.class);

            if (options.getTheme() != null) {
                this.currentTheme = options.getTheme();
            }

            if (options.getKeybinds() != null) {
                for (Map.Entry<String, String> entry : options.getKeybinds().entrySet()) {
                    try {
                        Actions action = Actions.valueOf(entry.getKey());
                        KeyCombination combo = KeyCombination.valueOf(entry.getValue());
                        userBinds.put(action, combo);
                    } catch (IllegalArgumentException ignored) {

                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load keybinds: " + e.getMessage());
        }
    }
    public String getCurrentTheme() {
        return currentTheme;
    }

    public KeyCombination getBind(Actions action) {
        return userBinds.getOrDefault(action, defaultBinds.get(action));
    }

    public void resetToDefault(Actions action) {
        userBinds.remove(action);

    }

    public void updateBind(Actions action, KeyCombination newCombo) {
        userBinds.put(action, newCombo);
    }

    public void resetAllToDefaults() {
        userBinds.clear();
    }

}
