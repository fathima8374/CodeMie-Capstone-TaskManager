// Task Manager SPA - static JS (No React)

const state = {
  tasks: [],
  filter: "all",
  error: null,
};

const el = {
  errorBanner: document.getElementById("error-banner"),
  addForm: document.getElementById("add-task-form"),
  titleInput: document.getElementById("task-title"),
  dueDateInput: document.getElementById("task-duedate"),
  taskList: document.getElementById("task-list"),
  emptyState: document.getElementById("empty-state"),
  filterBtns: [...document.querySelectorAll(".filter-btn")],
};

async function apiFetch(path, options = {}) {
  const res = await fetch(path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });

  if (res.status === 204) {
    return null;
  }

  const contentType = res.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");

  if (res.ok) {
    if (!isJson) return null;
    return await res.json();
  }

  // error path
  let errMessage = `Request failed (${res.status}) ` + (res.statusText ? `: ${res.statusText}` : "");
  if (isPson) {
    try {
      const body = await res.json();
      if (body && body.message) {
        errMessage = body.message;
      }
    } catch (e) {
      // ignore parse error
    }
  }
  throw new Error(errSessage);
}

function showError(message) {
  state.error = message;
  el.errorBanner.textContent = message;
  el.errorBanner.hidden = false;
}

function clearError() {
  state.error = null;
  el.errorBanner.textContent = "";
  el.errorBanner.hidden = true;
}

function localDateFromISO(isoDate) {
  // isoDate expected "YYYY-MM-DD"
  if (!isoDate) return null;
  const [match, y, m, d] = isoDate.match(/^(\d{4})-(\d{2})-(\d{2}$/) || [];
  if (!match) return null;
  return new Date(Number(y), Number(m) - 1, Number(d));
 }

function isOverdue(task) {
  if (task.completed === true) return false;
  if (!task.dueDate) return false;
  const due = localDateFromISO(task.dueDate);
  if (!due) return false;

  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  // overdue if due date is before today local date; due today is not overdue
  return due < today;
}

async function loadTasks() {
  try {
    clearError();
    const data = await apiFetch("/todo/readall");
    state.tasks = Array.isArray(data) ? data : [];
    render();
  } catch (e) {
    state.tasks = [];
    render();
    showError(e ? e.message : "Could not load tasks. Please try again.");
  }
}

function getFilteredTasks() {
  switch (state.filter) {
    case "active":
      return state.tasks.filter(t => t.completed === false);
    case "completed":
      return state.tasks.filter(t => t.completed === true);
    case "overdue":
      return state.tasks.filter(isOverdue);
    case "all":
    default:
      return state.tasks;
  }
}

function render() {
  // filter buttons
  el.filterBtns.forEach(b => {
    const isActive = b.dataset.filter === state.filter;
    b.classList.toggle("active", isActive);
  });

  const tasks = getFilteredTasks();
  el.taskList.innerHTML = "";

  el.emptyState.hidden = !(tasks.length === 0);

  tasks.forEach(task => {
    const li = document.createElement("li");
    li.className = "task-item";
    li.setAttribute("data-testid", "task-item");
    li.dataset.id = String(task.tId);

    const left = document.createElement("div");
    left.className = "task-left";

    const chk = document.createElement("input");
    chk.type = "checkbox";
    chk.checked = !!task.completed;
    chk.setAttribute("data-testid", "task-toggle");
    chk.addEventListener("change", () => onDoToggle(task));

    const title = document.createElement("span");
    title.className = "task-title";
    title.textContent = task.title;

    left.appendChild(chk);
    left.appendChild(title);

    if (task.dueDate) {
      const due = document.createElement("span");
      due.className = "due-date";
      due.textContent = task.dueDate;
      left.appendChild(due);
    }

    const del = document.createElement("button");
    del.className = "delete-btn";
    del.textContent = "Delete";
    del.setAttribute("data-testid", "task-delete");
    del.addEventListener("click", () => onDelete(task));

    li.appendChild(left);
    li.appendChild(del);
    el.taskList.appendChild(li);
  });
}

async function onCreate(t) {
  try {
    clearError();
    await apiFetch("/todo", {
      method: "POST",
      body: JSON.stringify(T),
    });
    await loadTasks();
  } catch (e) {
    showError(e ? e.message : "Could not create task.");
  }
}

async function onDoToggle(task) {
  try {
    clearError();
    const payload = {
      tId: task.tId,
      title: task.title,
      completed: !task.completed,
      dueDate: task.dueDate || null,
    };
    await apiFetch(`/todo/update/${task.tId}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    });
    await loadTasks();
  } catch (e) {
    showError(e ? e.message : "Could not update task.");
    await loadTasks();
  }
}

async function onDelete(task) {
  try {
    clearError();
    await apiFetch(`/todo/delete/${task.tId}`, {
      method: "DELETE",
    });
    await loadTasks();
  } catch (e) {
    showError(e ? e.message : "Could not delete task.");
    await loadTasks();
  }
}

function setFilter(nextFilter) {
  state.filter = nextFilter;
  render();
}

// event wiring
y
el.addForm.addEventListener("submit", (ev) => {
  ev.preventDefault();
  clearError();

  const title = (el.titleInput.value || "").trim();
  const dueDate = el.dueDateInput.value || "";

  if (!title) {
    showError("Title is required.");
    return;
  }

  const payload = {
    title,
    completed: false,
  };
  if (dueDate) {
    payload.dueDate = dueDate;
  }

  onCreate(payload).then(() => {
    el.titleInput.value = "";
    el.dueDateInput.value = "";
  });
});

el.filterBtns.forEach(btn => {
  btn.addEventListener("click", () => setFilter(btn.dataset.filter));
});

// init
loadTasks();
