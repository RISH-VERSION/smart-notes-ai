const BASE_URL = 'http://localhost:8080'; 
// ─── Token Helpers ───────────────────────────────────────────
export const saveToken = (token) => localStorage.setItem('token', token);
export const getToken = () => localStorage.getItem('token');
export const removeToken = () => localStorage.removeItem('token');

// ─── Shared fetch helper ─────────────────────────────────────
const authHeaders = () => ({
  'Authorization': `Bearer ${getToken()}`,
  'Content-Type': 'application/json'
});

const handleResponse = async (response) => {
  const contentType = response.headers.get('content-type');
  const data = contentType?.includes('application/json')
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    // Auto-handle token expiry globally
    if (response.status === 401) {
      // removeToken();
      // window.location.href = '/login'; // adjust to your route
    }
    throw new Error(typeof data === 'string' ? data : data?.message || 'Something went wrong');
  }
  return data;
};

// ─── Auth APIs ───────────────────────────────────────────────
export const loginAPI = async (username, password) => {
  const response = await fetch(`${BASE_URL}/api/users/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  console.log("Login API response status:", response.status);
  return handleResponse(response);
};

export const registerAPI = async (username, email, password) => {
  const response = await fetch(`${BASE_URL}/api/users/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, password })
  });
  return handleResponse(response);
};

// ─── Notes APIs ──────────────────────────────────────────────
export const getNotesAPI = async () => {
  const response = await fetch(`${BASE_URL}/api/notes`, {
    headers: authHeaders()
  });
  return handleResponse(response);
};

export const createNoteAPI = async (title, content) => {
  const response = await fetch(`${BASE_URL}/api/notes`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ title, content })
  });
  return handleResponse(response);
};

export const updateNoteAPI = async (id, note) => {
  const response = await fetch(`${BASE_URL}/api/notes/${id}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(note)
  });
  return handleResponse(response);
};

export const deleteNoteAPI = async (id) => {
  const response = await fetch(`${BASE_URL}/api/notes/${id}`, {
    method: 'DELETE',
    headers: authHeaders()
  });
  return handleResponse(response);
};

export const searchNotesAPI = async (keyword) => {
  const response = await fetch(
    `${BASE_URL}/api/notes/search?keyword=${encodeURIComponent(keyword)}`,
    { headers: authHeaders() }
  );
  return handleResponse(response);
};

// ─── AI APIs ─────────────────────────────────────────────────
// Sends note title + content to backend → Gemini summarizes it
// Returns { summary: "..." }
export const summarizeNoteAPI = async (title, content) => {
  const response = await fetch(`${BASE_URL}/api/ai/summarize`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ title, content })
  });
  return handleResponse(response);
};

// ─── AI Semantic Search ───────────────────────────────────────
// Sends query to RAG pipeline → ChromaDB finds similar notes
// → Gemini generates answer from those notes
export const semanticSearchAPI = async (query) => {
  const response = await fetch(`${BASE_URL}/api/ai/search`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ query })
  });
  return handleResponse(response);
};