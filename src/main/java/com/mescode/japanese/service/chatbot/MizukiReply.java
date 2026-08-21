package com.mescode.japanese.service.chatbot;

import java.util.List;

/**
 * Rich chatbot response rendered by the floating tutor UI.
 */
public record MizukiReply(
        String message,
        String sourceLabel,
        List<String> suggestions,
        MizukiAction action
) {

    public MizukiReply {
        message = message == null ? "" : message.trim();
        sourceLabel = sourceLabel == null || sourceLabel.isBlank()
                ? "Mizuki"
                : sourceLabel.trim();
        suggestions = suggestions == null
                ? List.of()
                : suggestions.stream()
                        .filter(value -> value != null && !value.isBlank())
                        .map(String::trim)
                        .limit(3)
                        .toList();
        action = action == null ? MizukiAction.none() : action;
    }

    public MizukiReply(String message, String sourceLabel, List<String> suggestions) {
        this(message, sourceLabel, suggestions, MizukiAction.none());
    }

    public static MizukiReply text(String message) {
        return new MizukiReply(message, "Mizuki", List.of(), MizukiAction.none());
    }
}
