import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import styles from "./HomePage.module.css";
import CategoryColumn from "./components/CategoryColumn.jsx";
import DraggableCategory from "./components/DraggableCategory.jsx";
import { getToken, getUser, clearSession } from "../../utils/storage";
import { DndContext } from "@dnd-kit/core";
const API_BASE = 'https://sys2.roneu.dk/api'

// ________________________________________________________

function displayName(user) {
  if (!user) {
    return "";
  }

  if (user.first_name) {
    return user.first_name;
  }

  if (user.name) {
    return user.name;
  }

  if (user.email) {
    return user.email.split("@")[0];
  }

  return "";
}

// ________________________________________________________

function daysUntil(date) {
  const diff = new Date(date) - new Date();
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

// ________________________________________________________

function parseErrorMessage(text) {
  try {
    const parsed = JSON.parse(text);

    if (typeof parsed === "string") {
      return parsed;
    }

    return parsed.message || text;
  } catch {
    return text;
  }
}

// ________________________________________________________

function HomePage() {
  const user = getUser();
  const name = displayName(user);

  const [wedding, setWedding] = useState(null);
  const [loadingWedding, setLoadingWedding] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [deleteError, setDeleteError] = useState("");

  const [showDeleteWarning, setShowDeleteWarning] = useState(false);

  const navigate = useNavigate();

  const [showEditWedding, setShowEditWedding] = useState(false);
  const [editForm, setEditForm] = useState({
    title: "",
    date: "",
    location: "",
    budget: "",
    description: "",
  });

  const [editError, setEditError] = useState("");
  const [editingWedding, setEditingWedding] = useState(false);

  const [categories, setCategories] = useState([]);

  const [editFormCategory, setEditFormCategory] = useState({
    title: "",
  });
  const [showEditCategory, setShowEditCategory] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [categoryToDelete, setCategoryToDelete] = useState(null);
  const [showDeleteCategoryWarning, setShowDeleteCategoryWarning] =
    useState(false);

  const [showCreateCategory, setShowCreateCategory] = useState(false);
  const [createCategoryForm, setCreateCategoryForm] = useState({
    title: "",
    categoryBudget: "",
  });

  const [taskToEdit, setTaskToEdit] = useState(null);
  const [showEditTask, setShowEditTask] = useState(false);
  const [editingTask, setEditingTask] = useState(false);
  const [editTaskForm, setEditTaskForm] = useState({
    title: "",
    deadline: "",
    price: "",
    estimatedHours: "",
    priority: "LOW",
    description: "",
    link: "",
  });
  const [taskToDelete, setTaskToDelete] = useState(null);
  const [showDeleteTaskWarning, setShowDeleteTaskWarning] = useState(false);
  const [showCreateTask, setShowCreateTask] = useState(false);
  const [createTaskCategory, setCreateTaskCategory] = useState(null);
  const [creatingTask, setCreatingTask] = useState(false);
  const [createTaskForm, setCreateTaskForm] = useState({
    title: "",
    deadline: "",
    price: "",
    estimatedHours: "",
    priority: "LOW",
    description: "",
  });

  const [showBudgetOverview, setShowBudgetOverview] = useState(false);

  const taskCount = categories.reduce(
    (total, category) => total + (category.tasks?.length ?? 0),
    0,
  );

  const completedTaskCount = categories.reduce(
    (total, category) =>
      total +
      (category.tasks ?? []).filter((task) => task.status === "DONE").length,
    0,
  );

  const totalHours = categories.reduce(
    (total, category) =>
      total +
      (category.tasks ?? []).reduce(
        (sum, task) => sum + (task.estimatedHours ?? 0),
        0,
      ),
    0,
  );

  const totalPrice = categories.reduce(
    (total, category) =>
      total +
      (category.tasks ?? []).reduce((sum, task) => sum + (task.price ?? 0), 0),
    0,
  );

  const days = wedding ? daysUntil(wedding.date) : 0;
  const today = new Date().toISOString().slice(0, 10);
  // ________________________________________________________

  useEffect(() => {
    const loadWedding = async () => {
      try {
        const response = await fetch(`${API_BASE}/weddings`, {
          headers: {
            Authorization: `Bearer ${getToken()}`,
          },
        });

        if (response.status === 204) {
          setWedding(null);
          return;
        }

        if (!response.ok) {
          throw new Error("Could not load wedding");
        }

        const result = await response.json();

        const weddings = result.data.data;

        if (weddings.length > 0) {
          setWedding(weddings[0]);
        } else {
          setWedding(null);
        }
      } catch (error) {
        console.error("Could not load wedding:", error);
        setWedding(null);
        setLoadError("We could not load your wedding. Please try again.");
      } finally {
        setLoadingWedding(false);
      }
    };

    loadWedding();
  }, []);

  useEffect(() => {
    if (!wedding) {
      return;
    }

    const loadCategories = async () => {
      try {
        const response = await fetch(
          `${API_BASE}/weddings/${wedding.id}/categories`,
          {
            headers: {
              Authorization: `Bearer ${getToken()}`,
            },
          },
        );

        if (!response.ok) {
          throw new Error("Could not load categories");
        }

        const result = await response.json();
        const categories = result.data.data;

        console.log("Category response:", result);
        console.log("Categories:", categories);

        setCategories(categories);
      } catch (error) {
        console.error("Could not load categories:", error);
        setCategories([]);
      }
    };

    loadCategories();
  }, [wedding]);

  // ________________________________________________________
  // ________________________________________________________

  const handleLogout = () => {
    clearSession();
    navigate("/login");
  };

  // ________________________________________________________

  const handleCreateWedding = () => {
    navigate("/create");
  };

  // ________________________________________________________

  const handleDeleteWedding = async () => {
    try {
      const response = await fetch(`${API_BASE}/weddings/${wedding.id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${getToken()}`,
        },
      });

      if (!response.ok) {
        throw new Error("Could not delete wedding");
      }

      setWedding(null);
      setCategories([]);
      setShowDeleteWarning(false);
    } catch (error) {
      console.error("Could not delete wedding:", error);
    }
  };
  // ________________________________________________________

  const handleEditWedding = async (e) => {
    e.preventDefault();

    setEditError("");
    setEditingWedding(true);

    const body = {
      title: editForm.title,
      date: editForm.date,
      location: editForm.location,
      description: editForm.description,
    };

    body.budget = editForm.budget === "" ? "0" : editForm.budget;

    try {
      const response = await fetch(`${API_BASE}/weddings/${wedding.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${getToken()}`,
        },
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        const text = await response.text();
        setEditError(parseErrorMessage(text) || "Could not update wedding");
        return;
      }

      setWedding({
        ...wedding,
        ...body,
      });

      setShowEditWedding(false);
    } catch {
      setEditError("Could not connect to the server");
    } finally {
      setEditingWedding(false);
    }
  };

  // ________________________________________________________
  const handleOpenEdit = () => {
    setEditForm({
      title: wedding.title ?? "",
      date: wedding.date ?? "",
      location: wedding.location ?? "",
      budget: wedding.budget != null ? String(wedding.budget) : "",
      description: wedding.description ?? "",
    });

    setEditError("");
    setShowEditWedding(true);
  };

  // ________________________________________________________

  const handleEditChange = (e) => {
    setEditForm({ ...editForm, [e.target.name]: e.target.value });
  };

  // ________________________________________________________

  const handleEditChangeCategory = (e) => {
    setEditFormCategory({
      ...editFormCategory,
      [e.target.name]: e.target.value,
    });
  };

  // ________________________________________________________

  const handleOpenEditCategory = (category) => {
    setEditingCategory(category);
    setEditFormCategory({
      title: category.title ?? "",
      categoryBudget:
        category.categoryBudget != null ? String(category.categoryBudget) : "",
    });
    setEditError("");
    setShowEditCategory(true);
  };

  // ________________________________________________________

  const handleOpenDeleteCategory = (category) => {
    setCategoryToDelete(category);
    setShowDeleteCategoryWarning(true);
  };

  // ________________________________________________________

  const handleEditCategory = async (e) => {
    e.preventDefault();

    setEditError("");

    const body = {
      title: editFormCategory.title,
    };

    if (editFormCategory.categoryBudget !== "") {
      body.categoryBudget = editFormCategory.categoryBudget;
    }

    try {
      const response = await fetch(
        `${API_BASE}/categories/${editingCategory.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${getToken()}`,
          },
          body: JSON.stringify(body),
        },
      );

      if (!response.ok) {
        const text = await response.text();
        setEditError(parseErrorMessage(text) || "Could not update category");
        return;
      }

      setCategories(
        categories.map((category) =>
          category.id === editingCategory.id
            ? { ...category, ...body }
            : category,
        ),
      );

      setShowEditCategory(false);
    } catch {
      setEditError("Could not connect to the server");
    } finally {
      setEditingCategory(false);
    }
  };

  // ________________________________________________________

  const handleDeleteCategory = async () => {
    try {
      const response = await fetch(
        `${API_BASE}/categories/${categoryToDelete.id}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${getToken()}`,
          },
        },
      );

      if (!response.ok) {
        const text = await response.text();
        console.log("DELETE error response:", text);
        throw new Error("Could not delete category");
      }

      const refreshed = await fetch(
        `${API_BASE}/weddings/${wedding.id}/categories`,
        {
          headers: {
            Authorization: `Bearer ${getToken()}`,
          },
        },
      );

      if (!refreshed.ok) {
        throw new Error("Could not load categories");
      }

      const refreshedResult = await refreshed.json();

      setCategories(refreshedResult.data.data);

      setCategoryToDelete(null);
      setShowDeleteCategoryWarning(false);
    } catch (error) {
      console.error("Could not delete category:", error);
    }
  };

  // ________________________________________________________

  const handleCreateCategory = async (e) => {
    e.preventDefault();

    const body = {
      title: createCategoryForm.title,
    };

    if (createCategoryForm.categoryBudget !== "") {
      body.categoryBudget = createCategoryForm.categoryBudget;
    }

    try {
      const response = await fetch(
        `${API_BASE}/weddings/${wedding.id}/categories`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${getToken()}`,
          },
          body: JSON.stringify(body),
        },
      );

      if (!response.ok) {
        const text = await response.text();
        setEditError(parseErrorMessage(text) || "Could not create the wedding");
        return;
      }

      const result = await response.json();

      const createdCategory = result.data.data;

      setCategories([...categories, createdCategory]);

      setCreateCategoryForm({
        title: "",
        categoryBudget: "",
      });

      setShowCreateCategory(false);
    } catch {
      setEditError("Could not connect to the server");
    }
  };

  // ________________________________________________________

  const handleCreateChangeCategory = (e) => {
    setCreateCategoryForm({
      ...createCategoryForm,
      [e.target.name]: e.target.value,
    });
  };

  // ________________________________________________________

  const handleOpenEditTask = (task) => {
    setTaskToEdit(task);

    setEditTaskForm({
      title: task.title ?? "",
      deadline: task.deadline ?? "",
      price: task.price != null ? String(task.price) : "",
      estimatedHours:
        task.estimatedHours != null ? String(task.estimatedHours) : "",
      priority: task.priority ?? "LOW",
      description: task.description ?? "",
      link: task.link ?? "",
    });

    setEditError("");
    setShowEditTask(true);
  };

  // ________________________________________________________

  const handleEditChangeTask = (e) => {
    setEditTaskForm({ ...editTaskForm, [e.target.name]: e.target.value });
  };

  // ________________________________________________________

  const handleEditTask = async (e) => {
    e.preventDefault();

    setEditError("");
    setEditingTask(true);

    const body = {
      title: editTaskForm.title,
      deadline: editTaskForm.deadline,
      price: editTaskForm.price === "" ? "0" : editTaskForm.price,
      estimatedHours:
        editTaskForm.estimatedHours === "" ? "0" : editTaskForm.estimatedHours,
      priority: editTaskForm.priority,
      description: editTaskForm.description,
      link: editTaskForm.link,
    };

    try {
      const response = await fetch(`${API_BASE}/tasks/${taskToEdit.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${getToken()}`,
        },
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        const text = await response.text();
        setEditError(parseErrorMessage(text) || "Could not update the task");
        return;
      }

      const result = await response.json();

      const updatedTask = result.data.data;

      setCategories(
        categories.map((category) => ({
          ...category,
          tasks: (category.tasks ?? []).map((task) =>
            task.id === taskToEdit.id ? updatedTask : task,
          ),
        })),
      );

      setShowEditTask(false);
      setTaskToEdit(null);
    } catch {
      setEditError("Could not connect to the server");
    } finally {
      setEditingTask(false);
    }
  };

  // ________________________________________________________

  const handleOpenDeleteTask = (task) => {
    setTaskToDelete(task);
    setDeleteError("");
    setShowDeleteTaskWarning(true);
  };

  // ________________________________________________________

  const handleDeleteTask = async () => {
    try {
      const response = await fetch(`${API_BASE}/tasks/${taskToDelete.id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${getToken()}`,
        },
      });

      if (!response.ok) {
        throw new Error("Could not delete task");
      }

      setCategories(
        categories.map((category) => ({
          ...category,
          tasks: (category.tasks ?? []).filter(
            (task) => task.id !== taskToDelete.id,
          ),
        })),
      );

      setTaskToDelete(null);
      setShowDeleteTaskWarning(false);
    } catch (error) {
      console.error("Could not delete task:", error);
      setDeleteError("The task could not be deleted. Please try again.");
    }
  };

  // ________________________________________________________

  const handleStatusTask = async (task, newStatus) => {
    try {
      const response = await fetch(`${API_BASE}/tasks/${task.id}/status`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${getToken()}`,
        },
        body: JSON.stringify({
          status: newStatus,
        }),
      });

      if (!response.ok) {
        throw new Error("Could not update the task status");
      }

      const result = await response.json();

      const updatedTask = result.data.data;

      setCategories(
        categories.map((category) => ({
          ...category,
          tasks: (category.tasks ?? []).map((existing) =>
            existing.id === task.id ? updatedTask : existing,
          ),
        })),
      );
    } catch (error) {
      console.error("Could not update the task status:", error);
    }
  };

  // ________________________________________________________

  const handleOpenCreateTask = (category) => {
    setCreateTaskCategory(category);

    setCreateTaskForm({
      title: "",
      deadline: "",
      price: "",
      estimatedHours: "",
      priority: "LOW",
      description: "",
    });

    setEditError("");
    setShowCreateTask(true);
  };

  // ________________________________________________________

  const handleCreateChangeTask = (e) => {
    setCreateTaskForm({ ...createTaskForm, [e.target.name]: e.target.value });
  };

  // ________________________________________________________

  const handleCreateTask = async (e) => {
    e.preventDefault();

    setEditError("");
    setCreatingTask(true);

    const body = {
      title: createTaskForm.title,
      deadline:
        createTaskForm.deadline === "" ? null : setCreateTaskForm.deadline,
      price: createTaskForm.price === "" ? "0" : createTaskForm.price,
      priority: createTaskForm.priority,
      description: createTaskForm.description,
    };

    body.estimatedHours =
      createTaskForm.estimatedHours === ""
        ? "0"
        : createTaskForm.estimatedHours;

    try {
      const response = await fetch(
        `${API_BASE}/categories/${createTaskCategory.id}/tasks`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${getToken()}`,
          },
          body: JSON.stringify(body),
        },
      );

      if (!response.ok) {
        const text = await response.text();
        setEditError(parseErrorMessage(text) || "Could not create the task");
        return;
      }

      const result = await response.json();

      const createdTask = result.data.data;

      setCategories(
        categories.map((category) =>
          category.id === createTaskCategory.id
            ? { ...category, tasks: [...(category.tasks ?? []), createdTask] }
            : category,
        ),
      );

      setShowCreateTask(false);
      setCreateTaskCategory(null);
    } catch {
      setEditError("Could not connect to the server");
    } finally {
      setCreatingTask(false);
    }
  };

  // ________________________________________________________

  const findCategoryByTaskId = (taskId) => {
    return categories.find((category) =>
      (category.tasks ?? []).some((task) => task.id === taskId),
    );
  };

  // ________________________________________________________

  const moveTaskToCategory = async (task, targetCategory, targetPosition) => {
    const sourceCategoryId = task.categoryId;
    const targetCategoryId = targetCategory.id;

    const body = {
      categoryId: targetCategoryId,
      position: String(targetPosition),
    };

    try {
      const response = await fetch(`${API_BASE}/tasks/${task.id}/position`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${getToken()}`,
        },
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        const text = await response.text();
        setEditError(parseErrorMessage(text) || "Could not move task");
        return;
      }

      const result = await response.json();
      const updatedTask = result.data.data;

      setCategories((currentCategories) =>
        currentCategories.map((category) => {
          if (
            sourceCategoryId === targetCategoryId &&
            category.id === sourceCategoryId
          ) {
            const reorderedTasks = [...(category.tasks ?? [])]
              .sort((a, b) => a.position - b.position)
              .filter((currentTask) => currentTask.id !== task.id);

            reorderedTasks.splice(targetPosition, 0, updatedTask);

            return {
              ...category,
              tasks: reorderedTasks.map((currentTask, index) => ({
                ...currentTask,
                position: index,
              })),
            };
          }

          if (category.id === sourceCategoryId) {
            const remainingTasks = (category.tasks ?? [])
              .filter((currentTask) => currentTask.id !== task.id)
              .map((currentTask, index) => ({
                ...currentTask,
                position: index,
              }));

            return {
              ...category,
              tasks: remainingTasks,
            };
          }

          if (category.id === targetCategoryId) {
            const targetTasks = [...(category.tasks ?? [])]
              .sort((a, b) => a.position - b.position)
              .filter((currentTask) => currentTask.id !== task.id);

            targetTasks.splice(targetPosition, 0, updatedTask);

            return {
              ...category,
              tasks: targetTasks.map((currentTask, index) => ({
                ...currentTask,
                position: index,
              })),
            };
          }

          return category;
        }),
      );
    } catch {
      setEditError("Could not move task");
    }
  };

  // ________________________________________________________

  const moveCategory = async (category, targetCategory) => {
    const targetPosition = categories.findIndex(
      (item) => item.id === targetCategory.id,
    );

    if (targetPosition < 0) {
      return;
    }

    try {
      const response = await fetch(
        `${API_BASE}/categories/${category.id}/position`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${getToken()}`,
          },
          body: JSON.stringify({ position: String(targetPosition) }),
        },
      );

      if (!response.ok) {
        throw new Error("Could not move the category");
      }

      const reordered = categories.filter((item) => item.id !== category.id);

      reordered.splice(targetPosition, 0, category);

      setCategories(reordered);
    } catch (error) {
      console.error("Could not move the category:", error);
    }
  };

  // ________________________________________________________

  const handleDragEnd = ({ active, over }) => {
    if (!over || active.id === over.id) {
      return;
    }

    const draggedCategory = categories.find(
      (category) => category.id === active.id,
    );

    if (draggedCategory) {
      const targetCategory =
        categories.find((category) => category.id === over.id) ??
        findCategoryByTaskId(over.id);

      if (!targetCategory) {
        return;
      }

      moveCategory(draggedCategory, targetCategory);
      return;
    }

    let draggedTask = null;

    for (const category of categories) {
      const foundTask = (category.tasks ?? []).find(
        (task) => task.id === active.id,
      );

      if (foundTask) {
        draggedTask = foundTask;
        break;
      }
    }

    if (!draggedTask) {
      return;
    }

    const categoryDrop = categories.find((category) => category.id === over.id);

    if (categoryDrop) {
      const targetPosition = categoryDrop.tasks?.length ?? 0;

      moveTaskToCategory(draggedTask, categoryDrop, targetPosition);

      return;
    }

    const targetCategory = findCategoryByTaskId(over.id);

    if (!targetCategory) {
      return;
    }

    const orderedTasks = [...(targetCategory.tasks ?? [])].sort(
      (a, b) => a.position - b.position,
    );

    const targetPosition = orderedTasks.findIndex(
      (task) => task.id === over.id,
    );

    if (targetPosition === -1) {
      return;
    }

    moveTaskToCategory(draggedTask, targetCategory, targetPosition);
  };

  // ________________________________________________________

  return (
    <div className={styles.homePage}>
      <header className={styles.topBar}>
        <Link className={styles.wordmark} to="/">
          <img src="/logo.svg" alt="" />
          <span>
            Say <em>I Do</em>
          </span>
        </Link>

        <div className={styles.account}>
          <span className={styles.accountName}>{name}</span>
          {!loadingWedding && !wedding && (
            <button
              className={styles.createWedding}
              onClick={handleCreateWedding}
            >
              Create wedding
            </button>
          )}
          <button className={styles.logout} onClick={handleLogout}>
            Log out
          </button>
        </div>
      </header>

      <div className={styles.banner}>
        <img className={styles.bannerImage} src="/hero.jpg" alt="" />
      </div>

      <main className={styles.content}>
        <p className={styles.eyebrow}>Your planning</p>
        <h1 className={styles.heading}>
          {name ? `Welcome back, ${name}` : "Welcome back"}
        </h1>

        {loadingWedding && <p>Loading wedding...</p>}

        {!loadingWedding && loadError && (
          <section className={styles.summary}>
            <h2 className={styles.emptyTitle}>Something went wrong</h2>
            <p className={styles.emptyText}>{loadError}</p>
          </section>
        )}

        {!loadingWedding && !wedding && !loadError && (
          <section className={styles.summary}>
            <h2 className={styles.emptyTitle}>
              You haven't created a wedding yet
            </h2>
            <p className={styles.emptyText}>
              Use the Create wedding button to get started.
            </p>

            <button
              className={styles.createWedding}
              onClick={handleCreateWedding}
            >
              Create wedding
            </button>
          </section>
        )}

        {!loadingWedding && wedding && (
          <>
            <section className={styles.summary}>
              <div className={styles.summaryActions}>
                <button className={styles.editWedding} onClick={handleOpenEdit}>
                  {" "}
                  Edit{" "}
                </button>
                <button
                  className={styles.deleteWedding}
                  onClick={() => setShowDeleteWarning(true)}
                >
                  {" "}
                  Delete wedding{" "}
                </button>
              </div>
              <div className={styles.summaryMain}>
                <p className={styles.date}>
                  {new Date(wedding.date).toLocaleDateString("en-GB", {
                    day: "numeric",
                    month: "long",
                    year: "numeric",
                  })}
                </p>

                <h2 className={styles.weddingTitle}> {wedding.title} </h2>

                <p className={styles.venue}> {wedding.location} </p>
              </div>

              <div className={styles.summaryStats}>
                <div className={styles.stat}>
                  <span className={styles.statValue}> {days} </span>
                  <span className={styles.statLabel}> days to go </span>
                </div>

                <div className={styles.stat}>
                  <span className={styles.statValue}> {taskCount} </span>
                  <span className={styles.statLabel}> tasks </span>
                </div>

                <div className={styles.stat}>
                  <span className={styles.statValue}>
                    {" "}
                    {completedTaskCount}{" "}
                  </span>
                  <span className={styles.statLabel}> completed tasks </span>
                </div>

                <div className={styles.stat}>
                  <span className={styles.statValue}>
                    {" "}
                    {Math.round(totalHours * 10) / 10}{" "}
                  </span>
                  <span className={styles.statLabel}> hours of work </span>
                </div>

                <div className={styles.stat}>
                  <span className={styles.statValue}>
                    {" "}
                    {totalPrice.toLocaleString("en-US")} kr.{" "}
                  </span>
                  <span className={styles.statLabel}>
                    {wedding.budget > 0
                      ? `of ${wedding.budget.toLocaleString("en-US")} kr. budget`
                      : "planned cost"}
                  </span>
                </div>
              </div>
            </section>
          </>
        )}

        {showBudgetOverview && (
          <section className={styles.budgetOverview}>
            <div className={styles.budgetHeader}>
              <div>
                <p className={styles.eyebrow}>Budget overview</p>
                <h2 className={styles.budgetTitle}>Wedding budget</h2>
              </div>

              <button
                className={styles.budgetClose}
                onClick={() => setShowBudgetOverview(false)}
              >
                {" "}
                ×{" "}
              </button>
            </div>

            <div className={styles.budgetSummary}>
              <span className={styles.budgetGraphPlaceholder}>
                INSERT GRAPH
              </span>
            </div>

            <div className={styles.budgetCategories}>
              <div className={styles.budgetRow}>
                <span>Venue</span>
                <span>15,000 / 20,000 kr.</span>
              </div>

              <div className={styles.budgetRow}>
                <span>Food & drinks</span>
                <span>12,000 / 10,000 kr.</span>
              </div>

              <div className={styles.budgetRow}>
                <span>Flowers & decor</span>
                <span>5,500 / 8,000 kr.</span>
              </div>
            </div>
          </section>
        )}

        <DndContext onDragEnd={handleDragEnd}>
          <div className={styles.categoryBoard}>
            {categories.map((category) => (
              <DraggableCategory
                key={category.id}
                category={category}
                onEdit={handleOpenEditCategory}
                onDelete={handleOpenDeleteCategory}
                onAddTask={handleOpenCreateTask}
                onEditTask={handleOpenEditTask}
                onDeleteTask={handleOpenDeleteTask}
                onChangeStatus={handleStatusTask}
                totalPrice={totalPrice}
              />
            ))}

            {wedding && (
              <button
                className={styles.addCategoryColumn}
                onClick={() => setShowCreateCategory(true)}
              >
                + New category
              </button>
            )}
          </div>
        </DndContext>
      </main>

      <footer className={styles.footer}>
        <Link className={styles.footerLink} to="/privacy">
          {" "}
          Privacy policy{" "}
        </Link>
      </footer>

      {showDeleteWarning && (
        <div className={styles.modalOverlay}>
          <div className={styles.deleteModal}>
            <h2>Delete wedding?</h2>

            <p> Are you sure you want to delete this wedding?</p>

            <div className={styles.modalActions}>
              <button
                className={styles.deleteWedding}
                onClick={() => setShowDeleteWarning(false)}
              >
                Cancel
              </button>
              <button
                className={styles.deleteWedding}
                onClick={handleDeleteWedding}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {showEditWedding && (
        <div className={styles.modalOverlay}>
          <form className={styles.editModal} onSubmit={handleEditWedding}>
            <h2>Edit wedding</h2>

            {editError && <p className={styles.editError}> {editError} </p>}

            <label htmlFor="edit-title">Title</label>

            <input
              id="edit-title"
              name="title"
              type="text"
              value={editForm.title}
              onChange={handleEditChange}
              required
            />

            <label htmlFor="edit-date">Date</label>

            <input
              id="edit-date"
              name="date"
              type="date"
              value={editForm.date}
              onChange={handleEditChange}
              min={today}
              required
            />

            <label htmlFor="edit-location">Location</label>

            <input
              id="edit-location"
              name="location"
              type="text"
              value={editForm.location}
              onChange={handleEditChange}
            />

            <label htmlFor="edit-budget"> Budget </label>

            <input
              id="edit-budget"
              name="budget"
              type="number"
              min="0"
              step="1"
              value={editForm.budget}
              onChange={handleEditChange}
            />

            <label htmlFor="edit-description">Description</label>

            <textarea
              id="edit-description"
              name="description"
              rows="4"
              value={editForm.description}
              onChange={handleEditChange}
              required
            />

            <div className={styles.modalActions}>
              <button
                className={styles.createWedding}
                type="button"
                onClick={() => setShowEditWedding(false)}
              >
                Cancel
              </button>

              <button
                className={styles.createWedding}
                type="submit"
                disabled={editingWedding}
              >
                {editingWedding ? "Saving..." : "Save changes"}
              </button>
            </div>
          </form>
        </div>
      )}

      {showDeleteCategoryWarning && categoryToDelete && (
        <div className={styles.modalOverlay}>
          <div className={styles.deleteModal}>
            <h2>Delete category?</h2>

            <p> Are you sure you want to delete this Category?</p>

            <div className={styles.modalActions}>
              <button
                className={styles.deleteWedding}
                onClick={() => {
                  (setShowDeleteCategoryWarning(false),
                    setCategoryToDelete(null));
                }}
              >
                Cancel
              </button>
              <button
                className={styles.deleteWedding}
                onClick={handleDeleteCategory}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {showEditCategory && (
        <div className={styles.modalOverlay}>
          <form
            className={styles.editCategoryModal}
            onSubmit={handleEditCategory}
          >
            <h2>Edit category</h2>
            {editError && <p className={styles.editError}> {editError} </p>}

            <label htmlFor="category-title"> Category name </label>

            <input
              id="category-title"
              name="title"
              type="text"
              value={editFormCategory.title}
              onChange={handleEditChangeCategory}
              required
            />

            <label htmlFor="edit-category-budget"> Category budget </label>

            <input
              id="edit-category-budget"
              name="categoryBudget"
              type="number"
              min="0"
              step="1"
              value={editFormCategory.categoryBudget}
              onChange={handleEditChangeCategory}
            />

            <div className={styles.modalActions}>
              <button
                className={styles.deleteWedding}
                type="button"
                onClick={() => {
                  (setShowEditCategory(false), setEditingCategory(null));
                }}
              >
                Cancel
              </button>

              <button className={styles.createWedding} type="submit">
                Save changes
              </button>
            </div>
          </form>
        </div>
      )}

      {showCreateCategory && (
        <div className={styles.modalOverlay}>
          <form
            className={styles.editCategoryModal}
            onSubmit={handleCreateCategory}
          >
            <h2>Create category</h2>
            {editError && <p className={styles.editError}> {editError} </p>}
            <label htmlFor="category-title"> Title </label>
            <input
              id="category-title"
              name="title"
              type="text"
              value={createCategoryForm.title}
              onChange={handleCreateChangeCategory}
              required
            />

            <label htmlFor="create-category-budget"> Category budget </label>

            <input
              id="create-category-budget"
              name="categoryBudget"
              type="number"
              min="0"
              step="1"
              value={createCategoryForm.categoryBudget}
              onChange={handleCreateChangeCategory}
            />

            <div className={styles.modalActions}>
              <button
                className={styles.deleteWedding}
                type="button"
                onClick={() => {
                  setShowCreateCategory(false);
                }}
              >
                Cancel
              </button>

              <button className={styles.createWedding} type="submit">
                Create category
              </button>
            </div>
          </form>
        </div>
      )}

      {showEditTask && taskToEdit && (
        <div className={styles.modalOverlay}>
          <form className={styles.editCategoryModal} onSubmit={handleEditTask}>
            <h2>Edit task</h2>

            {editError && <p className={styles.editError}> {editError} </p>}

            <label htmlFor="edit-task-title"> Title </label>
            <input
              id="edit-task-title"
              name="title"
              type="text"
              value={editTaskForm.title}
              onChange={handleEditChangeTask}
              required
            />

            <label htmlFor="edit-task-deadline"> Deadline </label>
            <input
              id="edit-task-deadline"
              name="deadline"
              type="date"
              value={editTaskForm.deadline}
              onChange={handleEditChangeTask}
              required
            />

            <label htmlFor="edit-task-price"> Price </label>
            <input
              id="edit-task-price"
              name="price"
              type="number"
              min="0"
              step="1"
              value={editTaskForm.price}
              onChange={handleEditChangeTask}
            />

            <label htmlFor="edit-task-hours"> Estimated hours </label>
            <input
              id="edit-task-hours"
              name="estimatedHours"
              type="number"
              min="0"
              step="0.5"
              value={editTaskForm.estimatedHours}
              onChange={handleEditChangeTask}
              required
            />

            <label htmlFor="edit-task-priority"> Priority </label>
            <select
              id="edit-task-priority"
              name="priority"
              value={editTaskForm.priority}
              onChange={handleEditChangeTask}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>

            <label htmlFor="edit-task-link"> Link </label>
            <input
              id="edit-task-link"
              name="link"
              type="url"
              value={editTaskForm.link}
              onChange={handleEditChangeTask}
            />

            <label htmlFor="edit-task-description"> Description </label>
            <textarea
              id="edit-task-description"
              name="description"
              rows="3"
              value={editTaskForm.description}
              onChange={handleEditChangeTask}
            />

            <div className={styles.modalActions}>
              <button
                className={styles.deleteWedding}
                type="button"
                onClick={() => {
                  (setShowEditTask(false), setTaskToEdit(null));
                }}
              >
                Cancel
              </button>

              <button
                className={styles.createWedding}
                type="submit"
                disabled={editingTask}
              >
                {editingTask ? "Saving..." : "Save changes"}
              </button>
            </div>
          </form>
        </div>
      )}

      {showDeleteTaskWarning && taskToDelete && (
        <div className={styles.modalOverlay}>
          <div className={styles.deleteModal}>
            <h2>Delete task?</h2>

            <p> Are you sure you want to delete {taskToDelete.title}?</p>

            {deleteError && <p className={styles.editError}> {deleteError} </p>}

            <div className={styles.modalActions}>
              <button
                className={styles.deleteWedding}
                onClick={() => {
                  (setShowDeleteTaskWarning(false), setTaskToDelete(null));
                }}
              >
                Cancel
              </button>
              <button
                className={styles.deleteWedding}
                onClick={handleDeleteTask}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {showCreateTask && createTaskCategory && (
        <div className={styles.modalOverlay}>
          <form
            className={styles.editCategoryModal}
            onSubmit={handleCreateTask}
          >
            <h2>Create task</h2>

            {editError && <p className={styles.editError}> {editError} </p>}

            <label htmlFor="task-title"> Title </label>
            <input
              id="task-title"
              name="title"
              type="text"
              value={createTaskForm.title}
              onChange={handleCreateChangeTask}
              required
            />

            <label htmlFor="task-deadline"> Deadline </label>
            <input
              id="task-deadline"
              name="deadline"
              type="date"
              value={createTaskForm.deadline}
              onChange={handleCreateChangeTask}
              required
            />

            <label htmlFor="task-price"> Price </label>
            <input
              id="task-price"
              name="price"
              type="number"
              min="0"
              step="1"
              value={createTaskForm.price}
              onChange={handleCreateChangeTask}
            />

            <label htmlFor="task-hours"> Estimated hours </label>
            <input
              id="task-hours"
              name="estimatedHours"
              type="number"
              min="0"
              step="0.5"
              value={createTaskForm.estimatedHours}
              onChange={handleCreateChangeTask}
            />

            <label htmlFor="task-priority"> Priority </label>
            <select
              id="task-priority"
              name="priority"
              value={createTaskForm.priority}
              onChange={handleCreateChangeTask}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>

            <label htmlFor="task-description"> Description </label>
            <textarea
              id="task-description"
              name="description"
              rows="3"
              value={createTaskForm.description}
              onChange={handleCreateChangeTask}
            />

            <div className={styles.modalActions}>
              <button
                className={styles.deleteWedding}
                type="button"
                onClick={() => {
                  (setShowCreateTask(false), setCreateTaskCategory(null));
                }}
              >
                Cancel
              </button>

              <button
                className={styles.createWedding}
                type="submit"
                disabled={creatingTask}
              >
                {creatingTask ? "Creating..." : "Create task"}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}

export default HomePage;
