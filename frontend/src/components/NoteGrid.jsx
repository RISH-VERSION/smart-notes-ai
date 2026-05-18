import NoteCard from "./NoteCard";
import './NoteGrid.css';

function NoteGrid({notes = [], index, onDelete, onUpdate}) {
  return (
    <div className="note-grid">
      {
        notes.length === 0 ? (
          <p>No notes found</p>
        ) : (
          notes.map((note) => (
            <NoteCard 
              key={note.id}
              note={note} 
              index={index}
              onDelete={onDelete}
              onUpdate={onUpdate} />
          ))
        )
      }
    </div>
  )
}

export default NoteGrid;