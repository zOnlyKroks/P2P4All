package de.zonlykroks.p2p.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public class P2PScreen extends Screen {
    private final Screen parent;

    public P2PScreen(@Nullable Screen parent) {
        super(Text.translatable("p2p.screen.title"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        assert this.client != null;
        context.drawCenteredTextWithShadow(this.client.textRenderer, Text.translatable("p2p.screen.title"), this.width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        assert this.client != null;
        int fontHeight = this.client.textRenderer.fontHeight;
        final DynamicGridWidget grid = getDynamicGridWidget(fontHeight);

        grid.forEachChild(this::addDrawableChild);

        ButtonWidget buttonWidget = ButtonWidget.builder(ScreenTexts.BACK, (btn) -> this.client.setScreen(this.parent)).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build();

        this.addDrawableChild(buttonWidget);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("p2p.button.issues"), button -> {
            double probability = 0.10;

            double randomNumber = Math.random();

            URI uri;

            try {
                if (randomNumber < probability) {
                    uri = new URI("https://youtu.be/xvFZjo5PgG0?autoplay=1");
                }else {
                    uri = new URI("https://github.com/zOnlyKroks/P2P4All/issues");
                }

                Util.getOperatingSystem().open(uri);
            }catch (Exception e) {
                e.printStackTrace();
            }

        }).dimensions(this.width - 58, this.height - 30, this.client.textRenderer.getWidth(Text.translatable("p2p.button.issues")) + 10, 20).build());
    }

    @NotNull
    private DynamicGridWidget getDynamicGridWidget(int fontHeight) {
        assert this.client != null;
        DynamicGridWidget grid = new DynamicGridWidget(15, 10 + fontHeight + 10, width - 20, height - 20 - fontHeight - 10 - 20);

        ImageButtonWidget joinScreenButton = new ImageButtonWidget(0, 0, 0, 0, Text.translatable("p2p.screen.btn.join"), Identifier.of("p2p", "textures/gui/join.webp"));
        ImageButtonWidget createScreenButton = new ImageButtonWidget(0, 0, 0, 0, Text.translatable("p2p.screen.btn.create"), Identifier.of("p2p", "textures/gui/create.webp"));

        createScreenButton.setClickEvent( (btn) -> this.client.setScreen(new CreateScreen(this, false)));

        joinScreenButton.setClickEvent( (btn) -> this.client.setScreen(new JoinScreen(this)));

        grid.addChild(createScreenButton, 2, 1);
        grid.addChild(joinScreenButton, 2, 1);

        grid.setPadding(5);

        grid.calculateLayout();
        return grid;
    }
}
