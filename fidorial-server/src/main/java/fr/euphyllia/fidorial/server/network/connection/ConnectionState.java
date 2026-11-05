package fr.euphyllia.fidorial.server.network.connection;

public enum ConnectionState {
    HANDSHAKE,
    STATUS,
    LOGIN,
    CONFIGURATION,
    PLAY,
    MOCK_PLAY
}
