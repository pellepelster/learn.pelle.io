package io.pelle.todo.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pelle.todo.TodoApplication;
import io.pelle.todo.TodoStorage;
import io.pelle.todo.controller.TodoController;
import io.pelle.todo.dto.Todo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TodoApplication.class)
public class TodoControllerTest {

  @MockBean
  private TodoStorage todoStorage;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private TodoController todoController;

  private MockMvc mvc;

  @Before
  public void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(todoController).build();
  }

  @Test
  public void testListTodos() throws Exception {
    UUID uuid = UUID.randomUUID();
		when(todoStorage.findAll()).thenReturn(Arrays.asList(new Todo(uuid.toString(), "todo 1", false)));

    mvc.perform(get("/api/todos")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].uuid", is(uuid.toString())))
				.andExpect(jsonPath("$[0].description", is("todo 1")))
				.andExpect(jsonPath("$[0].complete", is(false)));

		 verify(todoStorage, times(1)).findAll();
		 verifyNoMoreInteractions(todoStorage);
  }

  @Test
  public void testCreateTodo() throws Exception {
    final Todo todo = new Todo("todo 2");
		ObjectMapper objectMapper = new ObjectMapper();
		final byte[] bytes = objectMapper.writeValueAsBytes(todo);

		when(todoStorage.save(any(Todo.class))).thenReturn(todo);

		mvc.perform(post("/api/todos")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(bytes))
          .andDo(MockMvcResultHandlers.print())
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.uuid", not(isEmptyOrNullString())))
          .andExpect(jsonPath("$.description", is("todo 2")))
          .andExpect(jsonPath("$.complete", is(false)));

		verify(todoStorage, times(1)).save(Mockito.any(Todo.class));

    ArgumentCaptor<Todo> argument = ArgumentCaptor.forClass(Todo.class);
    verify(todoStorage).save(argument.capture());
    assertThat("todo 2", is(argument.getValue().getDescription()));

		verifyNoMoreInteractions(todoStorage);
	}

  @Test
  public void testCreateTodoWithEmptyDescription() throws Exception {
    final Todo todo = new Todo();
    ObjectMapper objectMapper = new ObjectMapper();
    final byte[] bytes = objectMapper.writeValueAsBytes(todo);

    mvc.perform(post("/api/todos")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(bytes))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest());

    verifyZeroInteractions(todoStorage);
  }

  @Test
	public void testDelete() throws Exception {
    UUID uuid = UUID.randomUUID();

		mvc.perform(delete("/api/todos/{uuid}", uuid.toString()))
				.andExpect(status().isNoContent());

    verify(todoStorage, times(1)).delete(eq(uuid.toString()));
    verifyNoMoreInteractions(todoStorage);
  }

  @Test
  public void testMarkCompleted() throws Exception {
    UUID uuid = UUID.randomUUID();

    when(todoStorage.findOne(any(Example.class))).thenReturn(Optional.of(new Todo(uuid.toString(), "xxx", false)));
    when(todoStorage.save(any(Todo.class))).thenReturn(new Todo(uuid.toString(), "xxx", true));

    mvc.perform(put("/api/todos/{uuid}/markCompleted", uuid.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(true)));

    verify(todoStorage, times(1)).save(any(Todo.class));
    verifyNoMoreInteractions(todoStorage);
  }

  @Test
  public void testMarkCompletedWithInvalidUUID() throws Exception {
    UUID uuid = UUID.randomUUID();

    when(todoStorage.findOne(any(Example.class))).thenReturn(null);

    mvc.perform(put("/api/todos/{uuid}/markCompleted", uuid.toString()))
        .andExpect(status().isNotFound());
    verify(todoStorage, times(1)).findOne(any(Example.class));
  }

  @Test
  public void testUpdateDescription() throws Exception {
    UUID uuid = UUID.randomUUID();

    when(todoStorage.findOne(any(Example.class))).thenReturn(new Todo(uuid.toString(), "xxx", false));
    when(todoStorage.save(any(Todo.class))).thenReturn(new Todo(uuid.toString(), "todo 3", true));

    mvc.perform(put("/api/todos/{id}/updateDescription", uuid.toString())
        .content("todo 3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is("todo 3")));


    verify(todoStorage, times(1)).save(any(Todo.class));
    verifyNoMoreInteractions(todoStorage);
  }

  @Test
  public void testUpdateDescriptionWithInvalidUUID() throws Exception {
    UUID uuid = UUID.randomUUID();

    when(todoStorage.findOne(any(Example.class))).thenReturn(new Todo(uuid.toString(), "xxx", false));

    mvc.perform(put("/api/todos/{id}/updateDescription", uuid.toString()).content("todo 3"))
        .andExpect(status().isNotFound());

    verifyZeroInteractions(todoStorage);
  }

}