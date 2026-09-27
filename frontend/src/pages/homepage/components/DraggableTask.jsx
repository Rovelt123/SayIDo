import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import styles from './CategoryColumn.module.css'

function DraggableTask({ task, onEditTask, onDeleteTask, onChangeStatus }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
  } = useSortable({  
    id: task.id, 
  })
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  return (
    <li ref={setNodeRef} style={style} className={`${styles.task} `} {...listeners} {...attributes}>
      <span className={styles.taskHeader}>

        <span className={styles.taskTitle}>
          {task.title}
        </span>

        
              <select className={styles.statusSelect} id="edit-task-status" name="status" value={task.status} onPointerDown={(e) => e.stopPropagation()} onChange={(e) => onChangeStatus(task, e.target.value)}>
                <option value="TODO">To-do</option>
                <option value="IN_PROGRESS">In progress</option>
                <option value="DONE">Done</option>
              </select>

        <span className={styles.actionsContainer}>
          <button
            className={styles.iconButton}
            onPointerDown={(e) => e.stopPropagation()}
            onClick={() => onEditTask(task)}
            aria-label={`Edit ${task.title}`}
          >
            ✎
          </button>

          <button
            className={styles.iconButton}
            onPointerDown={(e) => e.stopPropagation()}
            onClick={() => onDeleteTask(task)}
            aria-label={`Delete ${task.title}`}
          >
            ×
          </button>
        </span>
      </span>

      <span className={styles.taskMeta}>
        <span className={styles.metaRow}>
          <span className={styles.metaLabel}>Priority:</span>
          <span className={styles.metaValue}>{task.priority}</span>
        </span>

        {task.price > 0 && (
          <span className={styles.metaRow}>
            <span className={styles.metaLabel}>Price:</span>
            <span className={styles.metaValue}>{task.price.toLocaleString('en-US')} kr.</span>
          </span>
        )}

        {task.link && (
          <span className={styles.metaRow}>
            <span className={styles.metaLabel}>Link:</span>
            <a
              className={styles.taskLink}
              href={task.link}
              target="_blank"
              rel="noreferrer"
              onPointerDown={(e) => e.stopPropagation()}
            >
              Open
            </a>
          </span>
        )}
      </span>
    </li>
  )
}
export default DraggableTask
