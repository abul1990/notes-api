package com.tele.notesapi.model.store;

import com.tele.notesapi.model.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;
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
    private Set<Constant.Tag> tags;
    private LocalDateTime createdDate;
}
