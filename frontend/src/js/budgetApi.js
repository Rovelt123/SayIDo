import { getToken } from "../utils/storage";

import { API_BASE } from "../utils/api";

export async function budgetRequest(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${getToken()}`,
    },
  });
  if (!response.ok) {
    const text = await response.text();
    let message = text;
    try {
      message = JSON.parse(text).message || text;
    } catch {
      
    }
    throw new Error(message || "Could not load or save your budget. Please try again.");
  }
  const result = await response.json();
  return result.data.data;
}

