package app.mappers;

import app.dtos.WeddingDTO;
import app.entities.Category;
import app.entities.Wedding;
import app.entities.Task;
import app.enums.Status;
import app.mappers.generic.IMapper;
import app.services.BudgetService;
import app.utils.ErrorHandler;
import app.enums.Notifications;

import java.util.Set;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class WeddingMapper implements IMapper<Wedding, WeddingDTO> {

    private final CategoryMapper categoryMapper = new CategoryMapper();

    @Override
    public Wedding toEntity(WeddingDTO dto) {

        Wedding wedding = Wedding.builder()
            .id(dto.getId())
            .title(dto.getTitle())
            .date(ErrorHandler.tryParseLocalDate(
                dto.getDate(),
                Notifications.WEDDING_DATE_INVALID.getDisplayName()
            ))
            .location(dto.getLocation())
            .budget(dto.getBudget())
            .description(dto.getDescription())
            .build();

        if (dto.getCategories() != null) {
            Set<Category> categories = dto.getCategories().stream()
                .map(categoryMapper::toEntity)
                .collect(Collectors.toCollection(LinkedHashSet::new));

            categories.forEach(category -> category.setWedding(wedding));

            wedding.setCategories(categories);
        }

        return wedding;
    }

    // ________________________________________________________

    @Override
    public WeddingDTO toDTO(Wedding entity) {
        return WeddingDTO.builder()
            .id(entity.getId())
            .ownerId(
                entity.getOwner() != null
                ? entity.getOwner().getId()
                : null
            )
            .title(entity.getTitle())
            .date(
                entity.getDate() != null
                ? entity.getDate().toString()
                : null
            )
            .location(entity.getLocation())
            .budget(entity.getBudget())
            .description(entity.getDescription())
            .taskCount(entity.getCategories().stream().mapToInt(category -> category.getTasks().size()).sum())
            .completedTaskCount(entity.getCategories().stream().flatMap(category -> category.getTasks().stream()).filter(task -> task.getStatus() == Status.DONE).count())
            .totalEstimatedHours(entity.getCategories().stream().flatMap(category -> category.getTasks().stream()).mapToDouble(Task::getEstimatedHours).sum())
            .totalSpent(BudgetService.totalSpent(entity))
            .remainingBudget(BudgetService.remainingBudget(entity))
            .budgetUsedPercent(BudgetService.budgetUsedPercent(entity))
            .budgetOverrun(BudgetService.budgetOverrun(entity))
            .categories(
                entity.getCategories().stream()
                .sorted(Comparator.comparingInt(Category::getPosition).thenComparing(Category::getId))
                .map(categoryMapper::toDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new))
            )
            .build();
    }
}
