package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.YunaApplication
import com.example.data.local.db.entities.ConversationEntity
import com.example.data.local.db.entities.MemoryEntity
import com.example.data.local.db.entities.QuickNoteEntity
import com.example.data.local.prefs.YunaSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatHistoryViewModel : ViewModel() {
    private val app = YunaApplication.instance
    private val chatDao = app.database.chatDao()

    val conversations: StateFlow<List<ConversationEntity>> = chatDao.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            chatDao.deleteMessagesForConversation(id)
            chatDao.deleteConversation(id)
        }
    }

    fun renameConversation(id: Long, newTitle: String) {
        viewModelScope.launch {
            chatDao.updateConversation(id, newTitle)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatDao.clearAllMessages()
            chatDao.clearAllConversations()
        }
    }
}

class SmartMemoryViewModel : ViewModel() {
    private val app = YunaApplication.instance
    private val memoryDao = app.database.memoryDao()
    private val prefs = app.preferencesDataStore

    val memories: StateFlow<List<MemoryEntity>> = memoryDao.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val settings: StateFlow<YunaSettings> = prefs.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, YunaSettings())

    fun toggleMasterMemory(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setMasterMemoryEnabled(enabled)
        }
    }

    fun addMemory(category: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            memoryDao.insertMemory(
                MemoryEntity(
                    category = category,
                    content = content,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleMemoryActive(memory: MemoryEntity) {
        viewModelScope.launch {
            memoryDao.updateMemory(memory.copy(isEnabled = !memory.isEnabled))
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryDao.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memoryDao.clearAllMemories()
        }
    }
}

class QuickNotesViewModel : ViewModel() {
    private val app = YunaApplication.instance
    private val noteDao = app.database.noteDao()

    val notes: StateFlow<List<QuickNoteEntity>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addNote(title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            noteDao.insertNote(
                QuickNoteEntity(
                    title = if (title.isBlank()) "Quick Note" else title,
                    content = content
                )
            )
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            noteDao.deleteNote(id)
        }
    }
}
