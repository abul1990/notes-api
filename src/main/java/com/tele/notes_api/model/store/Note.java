package com.tele.notes_api.model.store;

import com.tele.notes_api.model.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "notes")
@AllArgsConstructor
@NoArgsConstructor
public class Note {

    @Id
    private UUID id;
    private String title;
    private String text;
    private Constant.Tag tag;
    private LocalDateTime createdDate;
}
