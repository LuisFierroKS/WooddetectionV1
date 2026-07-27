package com.example.wooddetectionv1.woodtypes.model;

public class WoodType {
    private final String name;
    private final String assetPath;

    public WoodType(String name, String assetPath) {
        this.name = name;
        this.assetPath = assetPath;
    }

    public String getName() {
        return name;
    }

    public String getAssetPath() {
        return assetPath;
    }
}
