package de.zonlykroks.p2p.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.zonlykroks.p2p.client.P2PClient;
import de.zonlykroks.p2p.config.P2PYACLConfig;
import de.zonlykroks.p2p.mixin.accessors.ScreenAccessor;
import de.zonlykroks.p2p.util.ConnectionProgress;
import de.zonlykroks.p2p.util.GoleDownloader;
import de.zonlykroks.p2p.util.GoleStarter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.QuickPlay;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.opengl.GL20.*;


public class CreateScreen extends Screen {
    private final Screen parent;
    private LevelSummary selectedWorld;
    private CompletableFuture<List<LevelSummary>> levelsFuture = null;
    private Optional<WorldIcon> worldIcon;
    private boolean shouldTunnel = false;

    private ButtonWidget createButton;

    private final boolean inGame;

    public CreateScreen(Screen parent, boolean inGame) {
        super(Text.translatable("p2p.screen.create.title"));
        this.parent = parent;
        this.inGame = inGame;
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    public void handleCreation() {
        if(this.selectedWorld == null && !inGame) return;

        if(!inGame) {
            P2PClient.ipToStateMap.clear();
            P2PClient.clearAllTunnels(false);
        }
        // Then do your magic here.
        // Will gladly do

        new GoleDownloader();

        if(shouldTunnel && !inGame) {
            for (int i = 0; i < P2PYACLConfig.get().savedIPs.size(); i++) {
                GoleStarter goleStarter = new GoleStarter(P2PYACLConfig.get().savedIPs.get(i), P2PYACLConfig.get().savedToPort.get(i), true);
                goleStarter.start();
            }
        }else if(shouldTunnel) {
            for (int i = 0; i < P2PYACLConfig.get().savedIPs.size(); i++) {
                String ip = P2PYACLConfig.get().savedIPs.get(i);

                if(P2PClient.ipToStateMap.containsKey(ip)) {
                    if(P2PClient.ipToStateMap.get(ip) == ConnectionProgress.SUCCESS) {
                        continue;
                    }
                }

                GoleStarter goleStarter = new GoleStarter(ip, P2PYACLConfig.get().savedToPort.get(i), true);
                goleStarter.start();
            }
        }

        Runnable startWorld;

        if(inGame) {
            startWorld = () -> MinecraftClient.getInstance().setScreen(null);
        }else {
            startWorld = () -> QuickPlay.startQuickPlay(MinecraftClient.getInstance(), new RunArgs.QuickPlay(null,selectedWorld.getName(),"",""), null);
        }

        if(shouldTunnel) {
            MinecraftClient.getInstance().setScreen(new ConnectionStateScreen(this,startWorld));
        } else {
            startWorld.run();
        }

    }

    @Override
    public void tick() {
        boolean ipSizeMatchPortSize = P2PYACLConfig.get().savedIPs.size() == P2PYACLConfig.get().savedToPort.size();

        boolean doublePorts = P2PYACLConfig.get().savedToPort.stream().anyMatch(i -> Collections.frequency(P2PYACLConfig.get().savedToPort, i) > 1);

        this.createButton.active = (inGame || ipSizeMatchPortSize) && !doublePorts;
    }

    @Override
    protected void init() {
        assert this.client != null;
        int startX = this.width / 2 - 50;

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (btn) -> this.client.setScreen(this.parent)).dimensions(5, 5, this.textRenderer.getWidth(ScreenTexts.BACK) + 10, 20).build());

        this.createButton = ButtonWidget.builder(Text.translatable("p2p.screen.create.btn.create"), (btn) -> handleCreation()).dimensions(this.width - this.textRenderer.getWidth(Text.translatable("p2p.screen.create.btn.create")) - 15, 5, this.textRenderer.getWidth(Text.translatable("p2p.screen.create.btn.create")) + 10, 20).build();

        LevelStorage.LevelList saves = this.client.getLevelStorage().getLevelList();

        if(!inGame) {
            this.levelsFuture = client.getLevelStorage().loadSummaries(saves);
            this.worldIcon = Optional.empty();
            this.levelsFuture.thenAccept((val) -> {
                if(!val.isEmpty()) {
                    this.selectedWorld = val.get(0);
                    handleWorldIcon();
                }

                this.addDrawableChild(CyclingButtonWidget.<LevelSummary>builder(optVal -> Text.of(optVal.getDisplayName())).values(val).build(10, this.height - 10 - (this.textRenderer.fontHeight * 3) - 25, startX - 15, 20, Text.translatable("p2p.screen.create.world_select"), (button, value) -> {
                    selectedWorld = value;
                    handleWorldIcon();
                }));
            });
        }

        this.addDrawableChild(this.createButton);

        this.addDrawableChild(CyclingButtonWidget.<Boolean>builder(optVal -> {
            if(optVal) {
                return Text.translatable("p2p.screen.create.btn.public").formatted(Formatting.GREEN);
            } else {
                return Text.translatable("p2p.screen.create.btn.private").formatted(Formatting.RED);
            }
        }).values(true, false).initially(false).build(startX + 5, 10+this.textRenderer.fontHeight+10, 200, 20, Text.translatable("p2p.screen.create.btn.public_private"), (button, value) -> {
            this.shouldTunnel = value;
        }));

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("p2p.screen.config_button"), button -> {
            MinecraftClient.getInstance().setScreen(P2PYACLConfig.getInstance().generateScreen(this));
        }).dimensions(startX + 5, 10 + this.textRenderer.fontHeight + 20 + 25 + 25 + 5, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Your IP: " + getPublicIP()), button -> this.client.keyboard.setClipboard(getPublicIP())).dimensions(startX + 5, 10 + this.textRenderer.fontHeight + 20 +25 + 25 + 5 + 20 + 10, 200, 20).build());
    }

    private void handleWorldIcon() {
        assert this.client != null;
        if(Files.exists(selectedWorld.getIconPath())) {
            this.worldIcon = Optional.of(WorldIcon.forWorld(this.client.getTextureManager(), this.selectedWorld.getName()));
            try {
                this.worldIcon.get().load(NativeImage.read(Files.newInputStream(selectedWorld.getIconPath())));
            } catch (Exception ignored) {
                this.worldIcon = Optional.empty();
            }
        } else {
            this.worldIcon = Optional.empty();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int startX = this.width / 2 - 50;

        this.renderBackground(context, mouseX, mouseY, delta);

        context.fill(5, 10 + textRenderer.fontHeight + 10, startX, this.height - 5, 0x77000000);
        context.drawBorder(5, 10 + textRenderer.fontHeight + 10, startX - 5, this.height - 5 - (10 + textRenderer.fontHeight + 10), 0x22000000);

        for (Drawable drawable : ((ScreenAccessor) this).getDrawables()) {
            drawable.render(context, mouseX, mouseY, delta);
        }

        Text worldInfoText = Text.translatable("p2p.screen.create.no_world_selected");

        if(selectedWorld != null)  {
            // Draw world name in that box.
            worldInfoText = selectedWorld.getDetails();
        }

        context.drawTextWrapped(textRenderer, worldInfoText, 10, this.height - 10 - (this.textRenderer.fontHeight * 3), startX - 15, 0xFFFFFF);

        if(levelsFuture != null) {
            if(!levelsFuture.isDone()) {
                context.drawText(textRenderer, "Loading Worlds...", 5, 10 + textRenderer.fontHeight + 10, 0x212121, false);
            }
        }

        // public void drawTexture(Identifier texture, int x, int y, int u, int v, int width, int height) {
        // Draw world icon in the box from x: 5, y: 5 to 5 above the button.

        context.enableScissor(15, textRenderer.fontHeight + 25, 15 + startX - 25, textRenderer.fontHeight + 25 + startX - 25);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        if(worldIcon == null || worldIcon.isEmpty()) {
            context.drawTexture(Identifier.ofVanilla("textures/misc/unknown_server.png"), 10, 10 + textRenderer.fontHeight + 10, 0, 0, startX - 15, startX - 15);
        } else if(worldIcon != null){
            context.drawTexture(worldIcon.get().getTextureId(), 15, textRenderer.fontHeight + 25, 0, 0, startX - 25, startX - 25);
        }

        // Draw border around image.
        context.drawBorder(15, textRenderer.fontHeight + 25, startX - 25, startX - 25, 0x2FFFFFFF);
        context.disableScissor();

        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        Text tunnelText = Text.translatable("p2p.screen.create.access_info");
        if(shouldTunnel) {
            tunnelText = Text.translatable("p2p.screen.create.access_info_tunnel");
        }

        context.drawTextWrapped(textRenderer, tunnelText, startX + 5, 10+this.textRenderer.fontHeight+15 + 20, 200, 0xFFFFFF);
    }

    private String getPublicIP() {
        try {
            URL ip = new URI(P2PYACLConfig.get().ipPingService).toURL();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    ip.openStream()));

            return in.readLine();
        }catch (Exception e) {
            return "x.x.x.x";
        }
    }
}
