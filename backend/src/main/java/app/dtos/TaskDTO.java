package app.dtos;

import app.enums.Priority;
import app.enums.Status;
import lombok.*;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TaskDTO {

    private UUID id;
    private UUID categoryId;
    private int position;
    private String title;
    private Priority priority;
    private String description;
    private String link;
    private float price;
    private String deadline;
    private float estimatedHours;
    private Status status;
}