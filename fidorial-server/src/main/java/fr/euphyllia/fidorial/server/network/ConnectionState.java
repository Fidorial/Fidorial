package fr.euphyllia.fidorial.server.network;

public enum ConnectionState {
    HANDSHAKE,
    STATUS,
    LOGIN,
    CONFIGURATION,
    PLAY,
    MOCK_PLAY
}
