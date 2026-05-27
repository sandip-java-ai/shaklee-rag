package com.sandip.shaklee.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationMemory {

    // sessionId → list of messages
    private final Map<String, List<Message>> sessions = new ConcurrentHashMap<>();

    public void addMessage(String sessionId, String role, String content) {
        sessions.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(new Message(role, content));
    }

    public List<Message> getHistory(String sessionId) {
        return sessions.getOrDefault(sessionId, new ArrayList<>());
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public record Message(String role, String content) {}
}