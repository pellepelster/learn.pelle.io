package io.pelle.todo;

import com.google.common.base.MoreObjects;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

public class NewTodo implements Serializable {

	@NotNull
	@NotEmpty
	private String description;

	public NewTodo() {
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("description", description).toString();
	}
}