package fr.euphyllia.fidorial.server.moderation;

import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServiceRegistrar;
import fr.euphyllia.fidorial.server.permission.OperatorList;
import fr.fidorial.moderation.BanManager;
import fr.fidorial.moderation.WhitelistManager;

import java.nio.file.Path;

/**
 * Who may join and what they may do: operators, bans, whitelist and code of conduct.
 */
public final class ModerationModule implements ServerModule {

    private final OperatorList operators = new OperatorList(Path.of("ops.json"));
    private final FidorialBanManager bans = new FidorialBanManager(Path.of("banned-players.json"), Path.of("banned-ips.json"));
    private final FidorialWhitelist whitelist = new FidorialWhitelist(Path.of("whitelist.json"));
    private final CodeOfConductManager codeOfConduct;

    public ModerationModule(final ServerConfiguration config) {
        this.codeOfConduct = new CodeOfConductManager(config.codeOfConduct().enabled(), config.codeOfConduct().path());
    }

    @Override
    public void registerServices(final ServiceRegistrar services) {
        services.provide(BanManager.class, bans);
        services.provide(WhitelistManager.class, whitelist);
    }

    @Override
    public void load() {
        operators.load();
        bans.load();
        whitelist.load();
    }

    @Override
    public void autoSave() {
        bans.purgeExpired();
    }

    public OperatorList operators() {
        return operators;
    }

    public FidorialBanManager bans() {
        return bans;
    }

    public FidorialWhitelist whitelist() {
        return whitelist;
    }

    public CodeOfConductManager codeOfConduct() {
        return codeOfConduct;
    }
}
