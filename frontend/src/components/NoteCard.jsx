import { useState, useEffect } from "react";
import { summarizeNoteAPI } from "../services/api";
import './NoteCard.css';

const CARD_COLORS = ['', 'note-card--yellow', 'note-card--green', 'note-card--blue'];

// How many characters to show before truncating
const CONTENT_LIMIT = 120;

function NoteCard({ note, onDelete, onUpdate, index }) {
  const [isEditing, setIsEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editNote, setEditNote] = useState({
    title: note.title,
    content: note.content
  });

  // ── Expand/Collapse state ──────────────────────────────────
  const [isExpanded, setIsExpanded] = useState(false);
  const isBigNote = note.content.length > CONTENT_LIMIT;
  const displayedContent = isBigNote && !isExpanded
    ? note.content.slice(0, CONTENT_LIMIT) + '...'
    : note.content;

  // ── Summarize state ────────────────────────────────────────
  const [summary, setSummary] = useState(null);
  const [summarizing, setSummarizing] = useState(false);
  const [summaryError, setSummaryError] = useState(null);

  useEffect(() => {
    setEditNote({ title: note.title, content: note.content });
    setSummary(null);
    setIsExpanded(false); // reset expand when note changes
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

  const handleSummarize = async () => {
    if (summary) {
      setSummary(null);
      return;
    }
    setSummarizing(true);
    setSummaryError(null);
    try {
      const data = await summarizeNoteAPI(note.title, note.content);
      setSummary(data.result);
    } catch (err) {
      setSummaryError('Could not summarize. Try again.');
    } finally {
      setSummarizing(false);
    }
  };

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

          {/* ── Content with expand/collapse ─────────────────
              Shows truncated text for long notes.
              "Show more ▾ / Show less ▴" button appears only
              when content exceeds CONTENT_LIMIT characters.
          ─────────────────────────────────────────────────── */}
          <div className={`note-content-wrapper ${isBigNote && isExpanded ? 'note-content-wrapper--expanded' : ''}`}>
            <p className="note-content">
              {isBigNote && !isExpanded ? note.content.slice(0, CONTENT_LIMIT) + '...' : note.content}
            </p>
          </div>

          {isBigNote && (
            <button
              className="card-btn card-btn--expand"
              onClick={() => setIsExpanded(!isExpanded)}
            >
              {isExpanded ? 'Show less ▴' : 'Show more ▾'}
            </button>
          )}

          {/* ── AI Summary section ───────────────────────────── */}
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
