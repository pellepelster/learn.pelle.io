package io.pelle.todo;

import io.pelle.todo.dto.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoStorage extends JpaRepository<Todo, String > {
}
