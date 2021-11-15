package com.tesco.pma.notes.model;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class Note {

    private UUID id;
    private UUID ownerColleagueUuid;
    private UUID referenceColleagueUuid;
    private NoteStatus status;
    private Instant updateTime;
    private UUID folderUuid;

    private String title;
    private String content;
}
