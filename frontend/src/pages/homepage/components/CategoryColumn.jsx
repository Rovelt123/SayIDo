import styles from "./CategoryColumn.module.css";
import DraggableTask from "./DraggableTask";
import { useDroppable } from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";

function CategoryColumn({
  category,
  onEdit,
  onDelete,
  onAddTask,
  onEditTask,
  onDeleteTask,
  onChangeStatus,
  totalPrice,
}) {
  const isUncategorized = category.title === "Uncategorized";

  const tasks = [...(category.tasks ?? [])].sort(
    (a, b) => a.position - b.position,
  );

  const totalHours = tasks.reduce(
    (sum, task) => sum + (task.estimatedHours ?? 0),
    0,
  );

  const totalTasksPrice = tasks.reduce(
    (sum, task) => sum + (task.price ?? 0),
    0,
  );

  const categoryPercentageBudget =
    totalPrice > 0 ? (totalTasksPrice / totalPrice) * 100 : 0;

  const categoryBudget = category.categoryBudget ?? 0;

  const haveCategoryBudget = categoryBudget > 0;

  const aboveBudget = haveCategoryBudget && totalTasksPrice > categoryBudget;

  const budgetExceedAmont = aboveBudget ? totalTasksPrice - categoryBudget : 0;

  const { setNodeRef, isOver } = useDroppable({ id: category.id });

  return (
    <div
      className={`${styles.container} ${isOver ? styles.dragOver : ""}`}
      ref={setNodeRef}
    >
      <div className={styles.header}>
        <h3 className={styles.smallTitle}>{category.title}</h3>

        <div className={styles.actionsContainer}>
          <button
            className={styles.iconButton}
            onClick={() => onAddTask(category)}
            aria-label={`Add task to ${category.title}`}
          >
            +
          </button>

          {!isUncategorized && (
            <>
              <button
                className={styles.iconButton}
                onClick={() => onEdit(category)}
                aria-label={`Edit ${category.title}`}
              >
                ✎
              </button>

              <button
                className={styles.iconButton}
                onClick={() => onDelete(category)}
                aria-label={`Delete ${category.title}`}
              >
                ×
              </button>
            </>
          )}
        </div>
      </div>

      {tasks.length > 0 && (
        <p className={styles.columnTotals}>
          {tasks.length} {tasks.length === 1 ? "task" : "tasks"},
          {Math.round(totalHours * 10) / 10} h,
          <br />

          {haveCategoryBudget ? (
            <span className={aboveBudget ? styles.aboveBudget : ""}>
              {" "}
              {totalTasksPrice} of {categoryBudget} kr. used currently.
              <br />

              {aboveBudget && (
                <>
                  {budgetExceedAmont} kr. over category budget
                  <br />
                </>
              )}
            </span>
          ) : (
            <>
              {" "}
              total expenses {totalTasksPrice} kr. <br />
            </>
          )}
          
          {Math.round(categoryPercentageBudget)}% of used total budget
        </p>
      )}

      {tasks.length === 0 && <p className={styles.empty}>No tasks yet</p>}

      <SortableContext
        items={tasks.map((task) => task.id)}
        strategy={verticalListSortingStrategy}
      >
        <ul className={styles.taskList}>
          {tasks.map((task) => (
            <DraggableTask
              key={task.id}
              task={task}
              onEditTask={onEditTask}
              onDeleteTask={onDeleteTask}
              onChangeStatus={onChangeStatus}
            />
          ))}
        </ul>
      </SortableContext>
    </div>
  );
}

export default CategoryColumn;
