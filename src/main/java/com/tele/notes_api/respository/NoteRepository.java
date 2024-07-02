package com.tele.notes_api.respository;

import com.tele.notes_api.model.Constant;
import com.tele.notes_api.model.store.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface NoteRepository extends MongoRepository<Note, UUID> {
    Page<Note> findByTagIn(Set<Constant.Tag> tags, Pageable pageable);

}
