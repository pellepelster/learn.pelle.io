<template>

  <div class="container" style="width: 60%;">
    <div class="row">
      <div class="col-sm">
        <h1>Todos</h1>
      </div>
    </div>
    <div class="row">
      <div class="col-sm">
        <div class="alert alert-secondary" role="alert">
          <input type="text" class="form-control" v-model="newTodo" v-on:keyup.13="addTodo">
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-sm">
        <h1></h1>
      </div>
    </div>
    <div class="row" v-bind:todo="todo" v-for="todo in todos" :key="todo.uuid">
      <div class="col-sm">
        <div class="alert alert-primary clearfix" role="alert">
        <span class="float-left  align-baseline">
          {{ todo.description }}
        </span>
          <button v-on:click="deleteTodo(todo)" type="button" class="float-right btn btn-outline-danger">x</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {HTTP} from './http'

export default {
  name: 'TodoList',
  data () {
    return {
      newTodo: '',
      todos: [],
      errors: []
    }
  },
  methods: {
    updateList () {
      HTTP.get('todos').then(response => {
        this.todos = response.data
      })
      .catch(e => {
        this.errors.push(e)
      })
    },
    addTodo: function () {
      HTTP.post('todos', {
        description: this.newTodo
      })
      .then(response => {
        this.updateList()
      })
      .catch(e => {
        this.errors.push(e)
      })

      this.newTodo = ''
    },
    deleteTodo: function (todo) {
      HTTP.delete('todos/' + todo.uuid).then(response => {
        var index = this.todos.indexOf(todo)
        this.todos.splice(index, 1)
      })
      .catch(e => {
        this.errors.push(e)
      })
    }
  },
  created () {
    this.updateList()
  }
}
</script>

<style scoped>
</style>
