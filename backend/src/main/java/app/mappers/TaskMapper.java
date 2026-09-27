package app.mappers;

import app.dtos.TaskDTO;
import app.entities.Task;
import app.mappers.generic.IMapper;
import app.utils.ErrorHandler;
import app.enums.Notifications;

public class TaskMapper implements IMapper<Task, TaskDTO> {

    @Override
    public Task toEntity(TaskDTO dto) {
        return Task.builder()
            .id(dto.getId())
            .position(dto.getPosition())
            .title(dto.getTitle())
            .priority(dto.getPriority())
            .description(dto.getDescription())
            .link(dto.getLink())
            .deadline(dto.getDeadline() == null ? null : ErrorHandler.tryParseLocalDate(dto.getDeadline(), Notifications.TASK_DEADLINE_INVALID.getDisplayName()))
            .estimatedHours(dto.getEstimatedHours())
            .status(dto.getStatus())
            .price(dto.getPrice())
            .build();
    }

    // ________________________________________________________

    @Override
    public TaskDTO toDTO(Task entity) {
        return TaskDTO.builder()
            .id(entity.getId())
            .categoryId(
                entity.getCategory() != null
                ? entity.getCategory().getId()
                : null
            )
            .position(entity.getPosition())
            .title(entity.getTitle())
            .priority(entity.getPriority())
            .description(entity.getDescription())
            .link(entity.getLink())
            .deadline(entity.getDeadline() == null ? null : entity.getDeadline().toString())
            .estimatedHours(entity.getEstimatedHours())
            .status(entity.getStatus())
            .price(entity.getPrice())
            .build();
    }
}
