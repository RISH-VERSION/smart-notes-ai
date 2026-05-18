import { useState } from "react";
import './NoteForm.css';

function NoteForm({ onAdd }) {
  const [note, setNote] = useState({
    title: "",
    content: ""
  });

  const [expanded, setExpanded] = useState(false);

  const handleChange = (e) => {
    setNote({ ...note, [e.target.name]: e.target.value });  
  };

  const handleAdd = () => {
    if(!note.title.trim() || !note.content.trim()) return;

    onAdd(note);

    setNote({title: "", content: ""});
    setExpanded(false);
  };

  return (
    <div className="note-form" onClick={() => setExpanded(true)}>

      {expanded && (
          <input 
            name="title"
            placeholder="Title"
            value={note.title}
            onChange={handleChange} 
          />
      )}


      <textarea
        name="content"
        placeholder="Take a note..."
        value={note.content}
        onChange={handleChange}
      />

      {expanded && (
        <button onClick={handleAdd}>Add</button>
      )}

      {expanded && (
        <button className="close-btn" onClick={(e) => {
          e.stopPropagation();
          setExpanded(false);
          setNote({ title: "", content: "" });
        }}>Close</button>
      )} 

    </div>
  )
}

export default NoteForm;