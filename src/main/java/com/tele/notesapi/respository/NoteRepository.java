package com.tele.notesapi.respository;

import com.tele.notesapi.model.Constant;
import com.tele.notesapi.model.store.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Set;
import java.util.UUID;

public interface NoteRepository extends MongoRepository<Note, UUID> {
    Page<Note> findByTagsIn(Set<Constant.Tag> tags, Pageable pageable);

}
