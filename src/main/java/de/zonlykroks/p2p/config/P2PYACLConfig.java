package de.zonlykroks.p2p.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class P2PYACLConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("p2p.json");

    @SerialEntry
    public String golePath = "";

    @SerialEntry
    public String ipPingService = "http://v4.ident.me/";

    @SerialEntry
    public boolean verboseLogging = false;

    @SerialEntry
    public List<String> savedIPs = List.of(
    );

    @SerialEntry
    public List<String> savedToPort = List.of(
    );

    @SerialEntry
    public boolean allowCheatsInLANWorld = false;

    @SerialEntry
    public boolean closeConnectionsOnWorldLeave = true;
    @SerialEntry
    public int localClientGamePort = 39332;

    @SerialEntry
    public int localServerPort = 25565;

    @SerialEntry
    public int connectTimeoutInSeconds = 60;

    @SerialEntry
    public CustomGameModeEnum lanGameMode = CustomGameModeEnum.SURVIVAL;

    public static final ConfigClassHandler<P2PYACLConfig> HANDLER = ConfigClassHandler.createBuilder(P2PYACLConfig.class)
            .id(Identifier.of("p2p", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(CONFIG_PATH)
                    .build())
            .build();

    public static P2PYACLConfig get() {
        return HANDLER.instance();
    }

    public static void load() {
        HANDLER.load();
    }

    public static void save() {
        HANDLER.save();
    }

    public static @NotNull YetAnotherConfigLib getInstance() {
        return YetAnotherConfigLib.create(HANDLER, (P2PYACLConfig defaults, P2PYACLConfig config, YetAnotherConfigLib.Builder builder) -> {
            var ips = ListOption.<String>createBuilder()
                    .name(Text.translatable("p2p.config.ips"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.ips.description")))
                    .controller(StringControllerBuilder::create)
                    .initial("127.0.0.1")
                    .binding(defaults.savedIPs, () -> config.savedIPs, (v) -> config.savedIPs = v)
                    .build();

            var savedToPort = ListOption.<String>createBuilder()
                    .name(Text.translatable("p2p.config.portip"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.portip.description")))
                    .controller(StringControllerBuilder::create)
                    .initial("5000")
                    .binding(defaults.savedToPort, () -> config.savedToPort, (v) -> config.savedToPort = v)
                    .build();

            var golePath = Option.<String>createBuilder()
                    .name(Text.translatable("p2p.config.golePath"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.golePath.description")))
                    .controller(StringControllerBuilder::create)
                    .binding(defaults.golePath, () -> config.golePath, (v) -> config.golePath = v)
                    .build();

            var ipPingService = Option.<String>createBuilder()
                    .name(Text.translatable("p2p.config.ipPingService"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.ipPingService.description")))
                    .controller(StringControllerBuilder::create)
                    .binding(defaults.ipPingService, () -> config.ipPingService, (v) -> config.ipPingService = v)
                    .build();

            var closeConnectionsOnWorldLeave = Option.<Boolean>createBuilder()
                    .name(Text.translatable("p2p.config.closeConnectionsOnWorldLeave"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.closeConnectionsOnWorldLeave.description")))
                    .controller(BooleanControllerBuilder::create)
                    .binding(defaults.closeConnectionsOnWorldLeave, () -> config.closeConnectionsOnWorldLeave, (v) -> config.closeConnectionsOnWorldLeave = v)
                    .build();

            var verboseLogging = Option.<Boolean>createBuilder()
                    .name(Text.translatable("p2p.config.verbose"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.verbose.description")))
                    .controller(BooleanControllerBuilder::create)
                    .binding(defaults.verboseLogging, () -> config.verboseLogging, (v) -> config.verboseLogging = v)
                    .build();

            var localClientGamePort = Option.<Integer>createBuilder()
                    .name(Text.translatable("p2p.config.localClientGamePort"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.localClientGamePort.description")))
                    .controller(IntegerFieldControllerBuilder::create)
                    .binding(defaults.localClientGamePort, () -> config.localClientGamePort, (v) -> config.localClientGamePort = v)
                    .build();

            var localServerPort = Option.<Integer>createBuilder()
                    .name(Text.translatable("p2p.config.localServerPort"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.localServerPort.description")))
                    .controller(IntegerFieldControllerBuilder::create)
                    .binding(defaults.localServerPort, () -> config.localServerPort, (v) -> config.localServerPort = v)
                    .build();

            var connectTimeoutInSeconds = Option.<Integer>createBuilder()
                    .name(Text.translatable("p2p.config.connectTimeoutInSeconds"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.connectTimeoutInSeconds.description")))
                    .controller(IntegerFieldControllerBuilder::create)
                    .binding(defaults.connectTimeoutInSeconds, () -> config.connectTimeoutInSeconds, (v) -> config.connectTimeoutInSeconds = v)
                    .build();

            var enableCheatsInLANWorld = Option.<Boolean>createBuilder()
                    .name(Text.translatable("p2p.config.cheats"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.cheats.description")))
                    .controller(BooleanControllerBuilder::create)
                    .binding(defaults.allowCheatsInLANWorld, () -> config.allowCheatsInLANWorld, (v) -> config.allowCheatsInLANWorld = v)
                    .build();

            var lanGameMode = Option.<CustomGameModeEnum>createBuilder()
                    .name(Text.translatable("p2p.config.gamemode"))
                    .description(OptionDescription.of(Text.translatable("p2p.config.gamemode.description")))
                    .customController(opt -> new EnumController<>(opt, CustomGameModeEnum.class))
                    .binding(defaults.lanGameMode, () -> config.lanGameMode, (value) -> config.lanGameMode = value)
                    .build();

            return builder
                    .title(Text.translatable("p2p.config.title"))
                    .category(ConfigCategory.createBuilder()
                            .name(Text.translatable("p2p.config.title"))
                            .options(List.of(verboseLogging,golePath, ipPingService, localClientGamePort, localServerPort, connectTimeoutInSeconds, lanGameMode, enableCheatsInLANWorld, closeConnectionsOnWorldLeave))
                            .group(ips)
                            .group(savedToPort)
                            .build());
        });

    }

    public enum CustomGameModeEnum {
        CREATIVE("creative"), SURVIVAL("survival"), SPECTATOR("spectator"), ADVENTURE("adventure");

        public final String name;

        CustomGameModeEnum(String name) {
            this.name = name;
        }
    }
}
