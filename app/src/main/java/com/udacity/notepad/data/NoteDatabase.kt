package com.udacity.notepad.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.udacity.notepad.data.NotesContract.NoteTable.CREATED_AT
import com.udacity.notepad.data.NotesContract.NoteTable.IS_PINNED
import com.udacity.notepad.data.NotesContract.NoteTable.TEXT
import com.udacity.notepad.data.NotesContract.NoteTable.UPDATED_AT
import com.udacity.notepad.data.NotesContract.NoteTable._ID
import com.udacity.notepad.data.NotesContract.NoteTable._TABLE_NAME
import org.jetbrains.anko.db.transaction
import java.util.*

class NoteDatabase(context: Context?) {
    private val helper: NotesOpenHelper = NotesOpenHelper(context)

    fun getAll(): List<Note> {
        return helper.readableDatabase.query(_TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                CREATED_AT).use(this::allFromCursor)
    }

    fun loadAllByIds(vararg ids: Int): List<Note> {
        val questionMarks = ids.map { "?" }.joinToString { ", " }
        val args = ids.map { it.toString() }
        val selection = "$_ID IN ($questionMarks)"
        return helper.readableDatabase.query(_TABLE_NAME,
                null,
                selection,
                args.toTypedArray(),
                null,
                null,
                CREATED_AT).use(this::allFromCursor)
    }

    fun insert(vararg notes: Note) {
        val values = fromNotes(notes)
        return helper.writableDatabase.transaction {
            for (value in values) {
                insert(_TABLE_NAME, null, value)
            }
        }
    }

    fun update(note: Note) {
        val values = fromNote(note)
        helper.writableDatabase.update(_TABLE_NAME,
                values,
                "$_ID = ?", arrayOf(Integer.toString(note.id)))
    }

    fun delete(note: Note) {
        helper.writableDatabase.delete(_TABLE_NAME,
                "$_ID = ?", arrayOf(Integer.toString(note.id)))
    }

    private fun fromCursor(cursor: Cursor): Note {
        var col = 0
        return Note().apply {
            id = cursor.getInt(col++)
            text = cursor.getString(col++)
            isPinned = cursor.getInt(col++) != 0
            createdAt = Date(cursor.getLong(col++))
            updatedAt = Date(cursor.getLong(col))
        }
    }

    private fun allFromCursor(cursor: Cursor): List<Note> {
        val retval: MutableList<Note> = ArrayList()
        while (cursor.moveToNext()) {
            retval.add(fromCursor(cursor))
        }
        return retval
    }

    private fun fromNote(note: Note): ContentValues {
        return ContentValues().apply {
            val noteId = note.id
            if (noteId != -1) {
                put(_ID, noteId)
            }
            put(TEXT, note.text)
            put(IS_PINNED, note.isPinned)
            put(CREATED_AT, note.createdAt.time)
            put(UPDATED_AT, note.updatedAt!!.time)
        }
    }

    private fun fromNotes(notes: Array<out Note>): List<ContentValues> {
        val values: MutableList<ContentValues> = ArrayList()
        for (note in notes) {
            values.add(fromNote(note))
        }
        return values
    }
}