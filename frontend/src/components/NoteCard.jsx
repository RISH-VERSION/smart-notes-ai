import { useState, useEffect } from "react";
import { summarizeNoteAPI } from "../services/api";
import './NoteCard.css';

// Color variants — cycles through colors like Google Keep
// Interview tip: modulo (%) operator cycles index back to 0
const CARD_COLORS = ['', 'note-card--yellow', 'note-card--green', 'note-card--blue'];

function NoteCard({ note, onDelete, onUpdate, index }) {
  const [isEditing, setIsEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editNote, setEditNote] = useState({
    title: note.title,
    content: note.content
  });

  // ── Summarize state ────────────────────────────────────────
  const [summary, setSummary] = useState(null);       // stores AI summary text
  const [summarizing, setSummarizing] = useState(false); // loading state
  const [summaryError, setSummaryError] = useState(null); // error state

  // Sync editNote if parent updates the note (e.g. after save)
  useEffect(() => {
    setEditNote({ title: note.title, content: note.content });
    setSummary(null); // clear summary when note changes
  }, [note]);

  const handleChange = (e) => {
    setEditNote({ ...editNote, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    if (!editNote.title.trim() || !editNote.content.trim()) return;
    setSaving(true);
    await onUpdate(note.id, editNote);
    setSaving(false);
    setIsEditing(false);
  };

  const handleCancel = () => {
    setEditNote({ title: note.title, content: note.content });
    setIsEditing(false);
  };

  // ── Summarize handler ──────────────────────────────────────
  // Toggle behaviour — if summary already shown, hide it
  // If not shown, call Gemini API and display result
  const handleSummarize = async () => {
    if (summary) {
      setSummary(null); // toggle off
      return;
    }

    setSummarizing(true);
    setSummaryError(null);

    try {
      const data = await summarizeNoteAPI(note.title, note.content);
      setSummary(data.result); // backend returns { summary: "..." }
    } catch (err) {
      setSummaryError('Could not summarize. Try again.');
    } finally {
      setSummarizing(false);
    }
  };

  // Derive color class from index — cycles yellow → green → blue → white
  const colorClass = CARD_COLORS[index % CARD_COLORS.length];

  return (
    <div className={`note-card ${colorClass}`}>
      {isEditing ? (
        <>
          <input
            className="card-edit-input"
            name="title"
            value={editNote.title}
            onChange={handleChange}
            placeholder="Title"
          />
          <textarea
            className="card-edit-textarea"
            name="content"
            value={editNote.content}
            onChange={handleChange}
            placeholder="Content"
          />
          <div className="note-card-actions">
            <button className="card-btn card-btn--edit" onClick={handleSave} disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
            <button className="card-btn card-btn--delete" onClick={handleCancel}>
              Cancel
            </button>
          </div>
        </>
      ) : (
        <>
          <h3>{note.title}</h3>
          <p>{note.content}</p>

          {/* ── AI Summary section ───────────────────────────
              Only renders when summarizing or summary exists.
              Conditional rendering — React best practice.
          ─────────────────────────────────────────────────── */}
          {summarizing && (
            <p className="summary-loading">✨ Summarizing...</p>
          )}

          {summary && (
            <div className="summary-box">
              <p className="summary-label">✨ AI Summary</p>
              <p className="summary-text">{summary}</p>
            </div>
          )}

          {summaryError && (
            <p className="summary-error">{summaryError}</p>
          )}

          <p className="note-date">
            {new Date(note.updatedAt).toLocaleDateString('en-IN', {
              day: 'numeric', month: 'short', year: 'numeric'
            })}
          </p>

          <div className="note-card-actions">
            <button className="card-btn card-btn--edit" onClick={() => setIsEditing(true)}>
              Edit ✏️
            </button>
            <button className="card-btn card-btn--delete" onClick={() => onDelete(note.id)}>
              Delete 🗑️
            </button>
          </div>

          {/* Summarize button — separate row, full width */}
          <button
            className="card-btn card-btn--summarize"
            onClick={handleSummarize}
            disabled={summarizing}
          >
            {summarizing ? '✨ Summarizing...' : summary ? 'Hide Summary' : '✨ Summarize'}
          </button>
        </>
      )}
    </div>
  );
}

export default NoteCard;