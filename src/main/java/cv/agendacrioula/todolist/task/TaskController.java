package cv.agendacrioula.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cv.agendacrioula.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity  create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    // aqui eu recupero o atributo que foi setado no filtro
    // System.out.println("Chegou no controller " + request.getAttribute("idUser"));
    
    var idUser = request.getAttribute("idUser");

    taskModel.setIdUser((UUID) idUser); 
    
    var currentDate = LocalDateTime.now();

    // 14/10/2023 - Current
    // 15/10/2023 - StartAt >(maior)
    if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início  e a data de término devem ser maior que a data atual");
    }

    // A data de início deve ser menor que a data de término
    if(taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser menor que a data de término");
    }
    
    var task = this.taskRepository.save(taskModel);

    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var idUser = request.getAttribute("idUser");
    
    var tasks = this.taskRepository.findByIdUser((UUID) idUser);

    return tasks;
  }

  // http://localhost:8080/tasks/89e2b3e0-8b1a-4b0e-8b0a-9b0b9b0b9b0b
  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
    var task = this.taskRepository.findById(id).orElse(null);

    if(task == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada");
    }
    
    var idUser = request.getAttribute("idUser");

    if(!task.getIdUser().equals(idUser)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não tem permissão para alterar a tarefa");
    }

    Utils.copyNonNullProperties(taskModel, task);
    
    var taskUpdated = this.taskRepository.save(task);

    return ResponseEntity.ok().body(taskUpdated);
  }

}
