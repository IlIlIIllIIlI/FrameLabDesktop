package com.frameLab.frameSprite.dto;

import java.util.HashMap;
import java.util.Map;

public class OptionsDTO {
    protected String theme;
    protected Map<String, String> keybinds;

    public OptionsDTO() {
    }

    public OptionsDTO(String theme){
        this.theme = theme;
        this.keybinds = new HashMap<>();
    }

    public void addKeybinds(String name, String keys) {
        if (this.keybinds == null) {
            this.keybinds = new HashMap<>();
        }
        this.keybinds.put(name,keys);
    }

    public Map<String, String> getKeybinds() {
        if (this.keybinds == null) {
            this.keybinds = new HashMap<>();
        }
        return keybinds;
    }

    public String getTheme() {
        return theme;
    }
}
