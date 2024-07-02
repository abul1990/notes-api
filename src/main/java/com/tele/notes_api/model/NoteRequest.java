package com.tele.notes_api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NoteRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Text cannot be blank")
    private String text;

    private Constant.Tag tag;

    @NotNull(message = "Created Date cannot be blank")
    private LocalDateTime createdDate;
}
