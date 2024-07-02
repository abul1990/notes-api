package com.tele.notes_api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class NoteResponse {
    private UUID id;
    private String title;
    private String text;
    private Constant.Tag tag;
    private LocalDateTime createdDate;
}
