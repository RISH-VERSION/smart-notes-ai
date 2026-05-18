import "./NotesConfirmDel.css";

function NoteConfirmDel({ onConfirm, onCancel }) {

    return (
        <div className="confirm-overlay">
            <div className="confirm-box">
                <button className="confirm-close" onClick={onCancel}>&times;</button>
                <h3>Delete this note?</h3>
                <p>This action cannot be undone. The note will be permanently removed.</p>
                <div className="confirm-btns">
                    <button className="confirm-btn--cancel" onClick={onCancel}>Cancel</button>
                    <button className="confirm-btn--delete" onClick={onConfirm}>Delete</button>
                </div>
            </div>
        </div>
    );
}

export default NoteConfirmDel;