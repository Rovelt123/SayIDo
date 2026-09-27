package app.dtos;

import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryDTO {

    private UUID id;
    private int position;
    private UUID weddingId;
    private String title;
    private int taskCount;
    private long completedTaskCount;
    private double totalEstimatedHours;
    private double categoryBudget;
    private double totalTasksPrice;

    @Builder.Default
    private Set<TaskDTO> tasks = new HashSet<>();
}
