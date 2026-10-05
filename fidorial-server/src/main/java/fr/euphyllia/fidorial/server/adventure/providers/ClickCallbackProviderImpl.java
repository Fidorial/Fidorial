package fr.euphyllia.fidorial.server.adventure.providers;

import fr.euphyllia.fidorial.server.FidorialServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;

@SuppressWarnings("UnstableApiUsage") // we are permitted
public final class ClickCallbackProviderImpl implements ClickCallback.Provider {
    @Override
    public ClickEvent<?> create(ClickCallback<Audience> callback, ClickCallback.Options options) {
        return FidorialServer.getInstance().clickCallbacksManager().addClickEvent(callback, options);
    }
}
