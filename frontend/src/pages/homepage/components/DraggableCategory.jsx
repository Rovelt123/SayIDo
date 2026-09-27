import { useDraggable } from '@dnd-kit/core'
import CategoryColumn from './CategoryColumn'
import styles from './CategoryColumn.module.css'

function DraggableCategory({ category, onEdit, onDelete, onAddTask, onEditTask, onDeleteTask, onChangeStatus, totalPrice }) {
    const { attributes, listeners, setNodeRef, transform } = useDraggable({
        id: category.id,
    })

    const style = {
        transform: transform
            ? `translate3d(${transform.x}px, ${transform.y}px, 0)`
            : undefined,
    }

    return (
        <div ref={setNodeRef} style={style} className={styles.draggableCategory}>
            <button
                className={styles.moveHandle}
                aria-label={`Move ${category.title}`}
                {...listeners}
                {...attributes}
            >
                ↔
            </button>

            <CategoryColumn
                category={category}
                onEdit={onEdit}
                onDelete={onDelete}
                onAddTask={onAddTask}
                onEditTask={onEditTask}
                onDeleteTask={onDeleteTask}
                onChangeStatus={onChangeStatus}
                totalPrice={totalPrice}
            />
        </div>
    )
}

export default DraggableCategory
