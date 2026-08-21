package com.mescode.japanese.service.chatbot;

import java.util.concurrent.CompletableFuture;

/**
 * Abstraction for chatbot responses so the UI can later switch from the
 * simulated implementation to a real AI provider without being rewritten.
 */
public interface ChatService extends AutoCloseable {

    CompletableFuture<MizukiReply> reply(String message);

    default void resetSession() {
        // Stateless providers do not need to reset anything.
    }

    @Override
    default void close() {
        // Most implementations do not need cleanup.
    }
}
