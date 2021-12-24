package com.tesco.pma.notes.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Folder {

    private UUID id;
    private UUID ownerColleagueUuid;
    private UUID parentFolderUuid;
    private String title;

}
