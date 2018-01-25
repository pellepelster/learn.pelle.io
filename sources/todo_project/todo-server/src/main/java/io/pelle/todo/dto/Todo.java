package io.pelle.todo.dto;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

@Entity
public class Todo implements Serializable {

	@Id()
	@Column()
	private String id;

	@Column()
	private String description;

	@Column()
	private boolean complete;

	public Todo() {
	}

	public Todo(String id, String description, boolean complete) {
		this.id = id;
		this.description = description;
		this.complete = complete;
	}

	public Todo(String description)
	{
		this(UUID.randomUUID().toString(), description, false);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	@Override
	public String toString() {
		return "[ id=" + this.id + ", description=" + this.description + ", complete=" + this.complete + " ]";
	}

}