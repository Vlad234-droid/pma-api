package com.tesco.pma.notes.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class Note {

    private UUID id;
    private UUID ownerColleagueUuid;
    private UUID referenceColleagueUuid;
    private NoteStatus status;
    private OffsetDateTime updateDate;
    private UUID folderUuid;

    private String title;
    private String content;
}
