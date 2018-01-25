package io.pelle.todo.controller;

import io.pelle.todo.TodoStorage;
import io.pelle.todo.dto.NewTodo;
import io.pelle.todo.dto.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

	@Autowired
	private TodoStorage todoStorage;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Iterable<Todo>> list() {
		return new ResponseEntity<>(todoStorage.findAll(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Todo> create(@Valid @RequestBody NewTodo newTodo) {
		Todo todo = todoStorage.save(new Todo(newTodo.getDescription()));
    return new ResponseEntity<>(todo, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") String id) {
		todoStorage.delete(id);
	}

	@RequestMapping(value = "/{id}/markCompleted", method = RequestMethod.PUT)
	public ResponseEntity<Todo> markCompleted(@PathVariable("id") String id) {

		Todo example = new Todo();
		example.setId(id);
		Todo todo = todoStorage.findOne(Example.of(example));

		if (todo != null) {
			return new ResponseEntity(todo, HttpStatus.OK);
		}else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@RequestMapping(value = "/{uuid}/updateDescription", method = RequestMethod.PUT)
	public ResponseEntity<Todo> updateDescription(@PathVariable("id") String id, @RequestBody String description) {

		Todo example = new Todo();
		example.setId(id);
		Todo todo = todoStorage.findOne(Example.of(example));

		if (todo != null) {
      todo.setDescription(description);
			return new ResponseEntity<>(todoStorage.save(todo), HttpStatus.OK);
		}else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

}