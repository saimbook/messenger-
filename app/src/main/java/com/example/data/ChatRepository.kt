package com.example.data

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allRooms: Flow<List<ChatRoom>> = chatDao.getAllRooms()
    val allCallLogs: Flow<List<CallLogItem>> = chatDao.getAllCallLogs()

    fun getMessagesForRoom(roomId: String): Flow<List<Message>> = chatDao.getMessagesForRoom(roomId)

    suspend fun insertMessage(message: Message) {
        chatDao.insertMessage(message)
    }

    suspend fun insertRoom(room: ChatRoom) {
        chatDao.insertRoom(room)
    }

    suspend fun insertCallLog(callLog: CallLogItem) {
        chatDao.insertCallLog(callLog)
    }
}
