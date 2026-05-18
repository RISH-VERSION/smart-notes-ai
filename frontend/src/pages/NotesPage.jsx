import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getNotesAPI, createNoteAPI, deleteNoteAPI,
  updateNoteAPI, removeToken, semanticSearchAPI
} from '../services/api';
import NoteForm from '../components/NoteForm';
import NoteGrid from '../components/NoteGrid';
import NoteConfirmDel from '../components/NotesConfirmDel';
import './NotesPage.css';

function NotesPage() {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showConfirm, setShowConfirm] = useState(false);
  const [selectedNoteId, setSelectedNoteId] = useState(null);

  // ── Semantic Search state ──────────────────────────────────
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResult, setSearchResult] = useState(null);  // AI answer
  const [searching, setSearching] = useState(false);       // loading state

  const navigate = useNavigate();

  const handleLogout = () => {
    removeToken();
    navigate('/login');
  };

  useEffect(() => {
    fetchNotes();
  }, []);

  const fetchNotes = async () => {
    setLoading(true);
    try {
      const response = await getNotesAPI();
      setNotes(Array.isArray(response) ? response : response.content || []);
    } catch (err) {
      setError('Failed to load notes');
    } finally {
      setLoading(false);
    }
  };

  const handleAddNote = async (newNote) => {
    try {
      const savedNote = await createNoteAPI(newNote.title, newNote.content);
      setNotes((prev) => [savedNote, ...prev]);
    } catch (err) {
      setError('Failed to create note');
    }
  };

  const handleDeleteNote = (id) => {
    setSelectedNoteId(id);
    setShowConfirm(true);
  };

  const handleConfirmDelete = async () => {
    try {
      await deleteNoteAPI(selectedNoteId);
      setNotes((prev) => prev.filter((note) => note.id !== selectedNoteId));
      setShowConfirm(false);
    } catch (err) {
      setError('Failed to delete note');
      setShowConfirm(false);
    }
  };

  const handleUpdateNote = async (id, updatedNote) => {
    try {
      const saved = await updateNoteAPI(id, updatedNote);
      setNotes((prev) => prev.map((note) => note.id === id ? saved : note));
    } catch (err) {
      setError('Failed to update note');
    }
  };

  // ── Semantic Search handler ────────────────────────────────
  // Called when user presses Enter in search bar
  // Interview tip: Enter key search = better UX than button click
  // because user doesn't have to move hand to mouse
  const handleSearch = async (e) => {
    // Only search on Enter key press
    if (e.key !== 'Enter') return;
    if (!searchQuery.trim()) {
      setSearchResult(null); // clear result if empty
      return;
    }

    setSearching(true);
    setSearchResult(null);

    try {
      const data = await semanticSearchAPI(searchQuery);
      setSearchResult(data.result);
    } catch (err) {
      setSearchResult('Search failed. Try again.');
    } finally {
      setSearching(false);
    }
  };

  // Clear search when user clears the input
  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
    if (!e.target.value.trim()) {
      setSearchResult(null); // clear AI result when input cleared
    }
  };

  return (
    <>
      <nav className="navbar">
        <div className="nav-brand">
          <div className="nav-icon">📝</div>
          <span className="nav-title">Smart Notes</span>
        </div>
        <div className="nav-actions">
          {/* Search bar — press Enter to search semantically */}
          <input
            className="nav-search"
            placeholder="Ask your notes..."
            value={searchQuery}
            onChange={handleSearchChange}
            onKeyDown={handleSearch}
          />
          <button className="logout-btn" onClick={handleLogout}>Sign out</button>
        </div>
      </nav>

      <div className="notes-content">

        {error && <p className="error-msg">{error}</p>}
        {loading && <p className="status-msg">Loading notes...</p>}

        {/* ── AI Search Result box ───────────────────────────
            Shows below navbar when user searches.
            Disappears when search is cleared.
        ──────────────────────────────────────────────────── */}
        {searching && (
          <div className="search-result-box">
            <p className="search-result-loading">🔍 Searching your notes...</p>
          </div>
        )}

        {searchResult && (
          <div className="search-result-box">
            <p className="search-result-label">🤖 AI Answer</p>
            <p className="search-result-text">{searchResult}</p>
            {/* Show what query was searched */}
            <p className="search-result-query">Query: "{searchQuery}"</p>
            <button
              className="search-clear-btn"
              onClick={() => {
                setSearchResult(null);
                setSearchQuery('');
              }}
            >
              Clear
            </button>
          </div>
        )}

        <NoteForm onAdd={handleAddNote} />

        <NoteGrid
          notes={notes}
          onDelete={handleDeleteNote}
          onUpdate={handleUpdateNote}
        />

        {showConfirm && (
          <NoteConfirmDel
            onConfirm={handleConfirmDelete}
            onCancel={() => setShowConfirm(false)}
          />
        )}
      </div>
    </>
  );
}

export default NotesPage;