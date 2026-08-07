package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.network.RadioNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class RadioScreen extends Screen {
    
    public static boolean isLightMode = false;

    private final InteractionHand hand;
    private int activeChannel = 0;
    
    // Per-Channel Memory
    private final float[] channelFreqs = new float[5];
    private final String[] channelAlgos = new String[5];
    private final String[] channelKeys = new String[5];
    private final String[] channelBws = new String[5];
    
    private float currentVolumeValue;
    
    // Real-world VHF/UHF tactical ranges
    private final float minFreq = 200.000f;
    private final float maxFreq = 670.000f;
    
    private boolean isDraggingTune = false;
    private boolean isDraggingVolume = false;
    
    private EditBox freqField;
    private EditBox keyField;
    
    private final String[] ALGORITHMS = {"CLEAR", "AES-128", "DES", "BLOWFISH", "TWOFISH", "CHACHA20"};
    private final String[] BANDWIDTHS = {"12.5k", "25.0k", "50.0k"};
    
    private final String radioName;

    public RadioScreen(InteractionHand hand, int activeChannel, String[] freqs, String[] algos, String[] keys, String[] bws, float currentVol, String radioName) {
        super(Component.literal(radioName + " Configuration"));
        this.hand = hand;
        this.activeChannel = activeChannel;
        this.currentVolumeValue = currentVol;
        
        for (int i = 0; i < 5; i++) {
            try {
                this.channelFreqs[i] = Float.parseFloat(freqs[i]);
            } catch (NumberFormatException e) {
                this.channelFreqs[i] = 446.000f;
            }
            this.channelAlgos[i] = algos[i] != null ? algos[i] : "CLEAR";
            this.channelKeys[i] = keys[i] != null ? keys[i] : "";
            this.channelBws[i] = bws[i] != null ? bws[i] : "25.0k";
        }
        this.radioName = radioName;
    }

    public static void open(InteractionHand hand, ItemStack stack) {
        int channel = 0;
        String[] freqs = {"446.000", "446.000", "446.000", "446.000", "446.000"};
        String[] algos = {"CLEAR", "CLEAR", "CLEAR", "CLEAR", "CLEAR"};
        String[] keys = {"", "", "", "", ""};
        String[] bws = {"25.0k", "25.0k", "25.0k", "25.0k", "25.0k"};
        float vol = 1.0f;
        
        if (stack.hasTag()) {
            net.minecraft.nbt.CompoundTag tag = stack.getTag();
            if (tag.contains("channel")) channel = tag.getInt("channel");
            if (tag.contains("volume")) vol = tag.getFloat("volume");
            
            String oldFreq = tag.contains("frequency") ? tag.getString("frequency") : "446.000";
            
            for (int i = 0; i < 5; i++) {
                freqs[i] = tag.contains("ch" + i + "_freq") ? tag.getString("ch" + i + "_freq") : oldFreq;
                algos[i] = tag.contains("ch" + i + "_algo") ? tag.getString("ch" + i + "_algo") : "CLEAR";
                keys[i] = tag.contains("ch" + i + "_key") ? tag.getString("ch" + i + "_key") : "";
                bws[i] = tag.contains("ch" + i + "_bw") ? tag.getString("ch" + i + "_bw") : "25.0k";
            }
        }
        
        String name = stack.getHoverName().getString().toUpperCase();
        if (name.equals("AIR") || name.isEmpty()) name = "PRC-152A";
        Minecraft.getInstance().setScreen(new RadioScreen(hand, channel, freqs, algos, keys, bws, vol, name));
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        int displayY = centerY - 50;
        int displayX = centerX - 70;

        // Expanded for 3 decimal precision
        this.freqField = new EditBox(this.font, displayX + 22, displayY + 15, 60, 12, Component.literal("Freq"));
        this.freqField.setBordered(false);
        this.freqField.setTextColor(0xFF00FF00);
        this.freqField.setValue(String.format(Locale.US, "%.3f", this.channelFreqs[this.activeChannel]));
        this.freqField.setFilter(s -> s.isEmpty() || s.matches("^[0-9]{0,3}(\\.[0-9]{0,3})?$"));
        this.addRenderableWidget(this.freqField);
        
        int keyY = displayY + 50 + 75; 
        this.keyField = new EditBox(this.font, displayX + 15, keyY + 4, 110, 10, Component.literal("Key"));
        this.keyField.setBordered(false);
        this.keyField.setTextColor(0xFFFF5555);
        this.keyField.setValue(this.channelKeys[this.activeChannel]);
        this.keyField.setMaxLength(16);
        this.addRenderableWidget(this.keyField);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int boxW = 320;
        int boxH = 200;
        int boxX = centerX - (boxW / 2);
        int boxY = centerY - (boxH / 2);

        int bgBase = isLightMode ? 0xFFE0E0E0 : 0xFF181A1B;
        int bgTop = isLightMode ? 0xFFFFFFFF : 0xFF35393D;
        int bgBot = isLightMode ? 0xFFAAAAAA : 0xFF0A0B0C;
        int bgLeft = isLightMode ? 0xFFFFFFFF : 0xFF2A2D30;
        int bgRight = isLightMode ? 0xFFAAAAAA : 0xFF0A0B0C;
        int textTitle = isLightMode ? 0xFF666666 : 0xFF555555;
        int textLabel = isLightMode ? 0xFF555555 : 0xFFAAAAAA;

        guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, bgBase);
        guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + 2, bgTop);
        guiGraphics.fill(boxX, boxY + boxH - 2, boxX + boxW, boxY + boxH, bgBot);
        guiGraphics.fill(boxX, boxY, boxX + 2, boxY + boxH, bgLeft);
        guiGraphics.fill(boxX + boxW - 2, boxY, boxX + boxW, boxY + boxH, bgRight);
        
        guiGraphics.drawString(this.font, this.radioName, boxX + 10, boxY + 10, textTitle, false);

        int modeBtnX = boxX + boxW - 35;
        int modeBtnY = boxY + 8;
        guiGraphics.fill(modeBtnX, modeBtnY, modeBtnX + 25, modeBtnY + 12, isLightMode ? 0xFF333333 : 0xFFEEEEEE);
        guiGraphics.drawString(this.font, isLightMode ? "DARK" : "LITE", modeBtnX + 3, modeBtnY + 2, isLightMode ? 0xFFEEEEEE : 0xFF333333, false);

        int displayW = 140;
        int displayH = 50;
        int displayX = centerX - (displayW / 2);
        int displayY = centerY - 50;
        
        guiGraphics.fill(displayX, displayY, displayX + displayW, displayY + displayH, 0xFF0A0B0C); 
        guiGraphics.fill(displayX, displayY, displayX + displayW, displayY + 1, 0xFF000000); 
        guiGraphics.fill(displayX, displayY + displayH - 1, displayX + displayW, displayY + displayH, 0xFF2A2D30);
        guiGraphics.fill(displayX, displayY, displayX + 1, displayY + displayH, 0xFF000000);
        guiGraphics.fill(displayX + displayW - 1, displayY, displayX + displayW, displayY + displayH, 0xFF2A2D30);

        guiGraphics.drawString(this.font, "FM", displayX + 5, displayY + 5, 0xFF005500, false);
        guiGraphics.drawString(this.font, "TX/RX", displayX + displayW - 35, displayY + 5, 0xFF005500, false);

        int barY = displayY + 35;
        guiGraphics.fill(displayX + 10, barY, displayX + displayW - 10, barY + 2, 0xFF112211);
        
        float currentFreqFloat = this.channelFreqs[this.activeChannel];
        try {
            currentFreqFloat = Float.parseFloat(this.freqField.getValue());
        } catch (Exception ignored) {}
        
        float clampedFreq = Math.max(minFreq, Math.min(maxFreq, currentFreqFloat));
        float freqPercent = (clampedFreq - minFreq) / (maxFreq - minFreq);
        int needleX = displayX + 10 + (int)(freqPercent * (displayW - 20));
        guiGraphics.fill(needleX - 1, barY - 4, needleX + 2, barY + 6, 0xFF00FF00);

        guiGraphics.drawString(this.font, "MHz", this.freqField.getX() + this.freqField.getWidth() + 2, displayY + 15, 0xFF00FF00, false);
        
        guiGraphics.drawString(this.font, "CH", displayX - 20, displayY + displayH + 14, textLabel, false);
        for (int i = 0; i < 5; i++) {
            int btnX = displayX + 10 + (i * 24);
            int chY = displayY + displayH + 10;
            boolean isChActive = (this.activeChannel == i);
            drawTacticalButton(guiGraphics, btnX, chY, String.valueOf(i+1), isChActive, 0xFF00FF00);
        }

        int algoY = displayY + displayH + 45;
        int fieldBgOuter = isLightMode ? 0xFFAAAAAA : 0xFF2A2D30;
        int fieldBgInner = isLightMode ? 0xFFEEEEEE : 0xFF111111;

        guiGraphics.drawString(this.font, "ALG", displayX - 25, algoY + 4, textLabel, false);
        String activeAlgo = this.channelAlgos[this.activeChannel];
        guiGraphics.fill(displayX + 10, algoY, displayX + 65, algoY + 16, fieldBgOuter);
        guiGraphics.fill(displayX + 11, algoY + 1, displayX + 64, algoY + 15, fieldBgInner);
        guiGraphics.drawCenteredString(this.font, activeAlgo.substring(0, Math.min(4, activeAlgo.length())), displayX + 37, algoY + 4, activeAlgo.equals("CLEAR") ? 0xFF00FF00 : 0xFFFF5555);

        guiGraphics.drawString(this.font, "BW", displayX + 70, algoY + 4, textLabel, false);
        String activeBw = this.channelBws[this.activeChannel];
        guiGraphics.fill(displayX + 85, algoY, displayX + 130, algoY + 16, fieldBgOuter);
        guiGraphics.fill(displayX + 86, algoY + 1, displayX + 129, algoY + 15, fieldBgInner);
        guiGraphics.drawCenteredString(this.font, activeBw, displayX + 107, algoY + 4, 0xFF00FF00);

        int keyY = displayY + displayH + 75;
        guiGraphics.drawString(this.font, "KEY", displayX - 25, keyY + 4, textLabel, false);
        int keyBgOuter = isLightMode ? 0xFFAAAAAA : 0xFF0B0C0E;
        int keyBgInner = isLightMode ? 0xFFCCCCCC : 0xFF4A4E52;
        guiGraphics.fill(displayX + 10, keyY, displayX + 130, keyY + 16, keyBgOuter);
        guiGraphics.fill(displayX + 10, keyY + 15, displayX + 130, keyY + 16, keyBgInner);

        int dialL_X = boxX + 50;
        int dialL_Y = centerY - 20;
        drawDigitalDial(guiGraphics, dialL_X, dialL_Y, freqPercent, "TUNE");

        int sliderX = boxX + boxW - 45;
        int sliderY = centerY - 50;
        int sliderH = 95;
        drawVerticalSlider(guiGraphics, sliderX, sliderY, sliderH, currentVolumeValue, "VOL");

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawTacticalButton(GuiGraphics guiGraphics, int btnX, int btnY, String text, boolean isPressed, int ledColor) {
        int w = 20;
        int h = 18;
        
        int btnUpBase = isLightMode ? 0xFFDDDDDD : 0xFF2A2D30;
        int btnUpHL = isLightMode ? 0xFFFFFFFF : 0xFF4A4E52;
        int btnUpSH = isLightMode ? 0xFFAAAAAA : 0xFF0B0C0E;
        
        int btnDnBase = isLightMode ? 0xFFCCCCCC : 0xFF111111;
        int btnDnHL = isLightMode ? 0xFFAAAAAA : 0xFF000000;
        int btnDnSH = isLightMode ? 0xFFEEEEEE : 0xFF333333;
        
        int textColor = isLightMode ? (isPressed ? 0xFF444444 : 0xFF222222) : (isPressed ? 0xFF888888 : 0xFFDDDDDD);
        
        if (isPressed) {
            guiGraphics.fill(btnX, btnY + 2, btnX + w, btnY + h, btnDnBase);
            guiGraphics.fill(btnX, btnY + 2, btnX + w, btnY + 4, btnDnHL);
            guiGraphics.fill(btnX, btnY + h - 1, btnX + w, btnY + h, btnDnSH);
            guiGraphics.drawString(this.font, text, btnX + 7, btnY + 6, textColor, false);
            guiGraphics.fill(btnX + 8, btnY + h - 3, btnX + 12, btnY + h - 1, ledColor); 
        } else {
            guiGraphics.fill(btnX, btnY, btnX + w, btnY + h, btnUpBase);
            guiGraphics.fill(btnX, btnY, btnX + w, btnY + 2, btnUpHL);
            guiGraphics.fill(btnX, btnY + h - 2, btnX + w, btnY + h, btnUpSH);
            guiGraphics.drawString(this.font, text, btnX + 7, btnY + 5, textColor, false);
            guiGraphics.fill(btnX + 8, btnY + h - 3, btnX + 12, btnY + h - 1, isLightMode ? 0xFF888888 : 0xFF222222); 
        }
    }

    private void drawDigitalDial(GuiGraphics guiGraphics, int x, int y, float percent, String label) {
        int base1 = isLightMode ? 0xFFAAAAAA : 0xFF0B0C0E;
        int base2 = isLightMode ? 0xFFEEEEEE : 0xFF2A2D30;
        int base3 = isLightMode ? 0xFFDDDDDD : 0xFF1A1C1E;
        int textC = isLightMode ? 0xFF555555 : 0xFFAAAAAA;

        guiGraphics.fill(x - 26, y - 26, x + 26, y + 26, base1); 
        guiGraphics.fill(x - 24, y - 24, x + 24, y + 24, base2); 
        guiGraphics.fill(x - 22, y - 22, x + 22, y + 22, base3); 
        
        guiGraphics.drawCenteredString(this.font, label, x, y + 32, textC);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        float angle = -135f + (percent * 270f);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        guiGraphics.fill(-2, -22, 2, -12, 0xFFD62929); 
        guiGraphics.pose().popPose();
    }

    private void drawVerticalSlider(GuiGraphics guiGraphics, int x, int y, int height, float percent, String label) {
        int trackBg = isLightMode ? 0xFFCCCCCC : 0xFF0B0C0E;
        int trackSh = isLightMode ? 0xFFAAAAAA : 0xFF000000;
        int tickC   = isLightMode ? 0xFF999999 : 0xFF555555;
        
        guiGraphics.fill(x - 4, y, x + 4, y + height, trackBg);
        guiGraphics.fill(x - 2, y + 1, x + 2, y + height - 1, trackSh);
        
        for (int i = 0; i <= 4; i++) {
            int tickY = y + (i * (height / 4));
            guiGraphics.fill(x - 8, tickY, x - 5, tickY + 1, tickC);
        }

        int fillHeight = (int)(percent * height);
        guiGraphics.fill(x - 1, y + height - fillHeight, x + 1, y + height - 1, 0xFF00FF00);

        int handleY = y + height - fillHeight;
        int hd1 = isLightMode ? 0xFFAAAAAA : 0xFF0B0C0E;
        int hd2 = isLightMode ? 0xFFFFFFFF : 0xFF35393D;
        int hd3 = isLightMode ? 0xFFEEEEEE : 0xFF2A2D30;
        int hd4 = isLightMode ? 0xFF888888 : 0xFF555555;

        guiGraphics.fill(x - 12, handleY - 7, x + 12, handleY + 7, hd1); 
        guiGraphics.fill(x - 11, handleY - 6, x + 11, handleY + 6, hd2); 
        guiGraphics.fill(x - 10, handleY - 5, x + 10, handleY + 5, hd3); 
        guiGraphics.fill(x - 8, handleY - 1, x + 8, handleY + 1, hd4); 

        guiGraphics.drawCenteredString(this.font, label, x, y - 12, isLightMode ? 0xFF555555 : 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        this.freqField.setFocused(false);
        this.keyField.setFocused(false);

        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            int boxW = 320;
            int boxH = 200;
            int boxX = centerX - (boxW / 2);
            int boxY = centerY - (boxH / 2);
            
            int modeBtnX = boxX + boxW - 35;
            int modeBtnY = boxY + 8;
            if (mouseX >= modeBtnX && mouseX <= modeBtnX + 25 && mouseY >= modeBtnY && mouseY <= modeBtnY + 12) {
                isLightMode = !isLightMode;
                playClick();
                return true;
            }

            int displayX = centerX - 70;
            int displayY = centerY - 50;
            int displayH = 50;
            
            int dialL_X = boxX + 50;
            int dialL_Y = centerY - 20;
            int sliderX = boxX + boxW - 45;
            int sliderY = centerY - 50;
            int sliderH = 95;
            
            for (int i = 0; i < 5; i++) {
                int btnX = displayX + 10 + (i * 24);
                int chY = displayY + displayH + 10;
                
                if (mouseX >= btnX && mouseX <= btnX + 20 && mouseY >= chY && mouseY <= chY + 18) {
                    saveCurrentChannelLocally();
                    this.activeChannel = i;
                    loadChannelLocally();
                    playClick();
                    return true;
                }
            }
            
            int algoY = displayY + displayH + 45;
            if (mouseX >= displayX + 10 && mouseX <= displayX + 65 && mouseY >= algoY && mouseY <= algoY + 16) {
                cycleAlgorithm();
                playClick();
                return true;
            }
            
            if (mouseX >= displayX + 85 && mouseX <= displayX + 130 && mouseY >= algoY && mouseY <= algoY + 16) {
                cycleBandwidth();
                playClick();
                return true;
            }
            
            if ((mouseX >= displayX && mouseX <= displayX + 140 && mouseY >= displayY && mouseY <= displayY + 50) ||
                Math.hypot(mouseX - dialL_X, mouseY - dialL_Y) <= 26) {
                this.isDraggingTune = true;
                updateFreqFromMouse(mouseX, displayX, 140);
                return true;
            }
            
            if (mouseX >= sliderX - 15 && mouseX <= sliderX + 15 && mouseY >= sliderY - 8 && mouseY <= sliderY + sliderH + 8) {
                this.isDraggingVolume = true;
                updateVolumeFromMouse(mouseY, sliderY, sliderH);
                return true;
            }
        }
        return false;
    }
    
    private void cycleAlgorithm() {
        String current = this.channelAlgos[this.activeChannel];
        int index = 0;
        for (int i = 0; i < ALGORITHMS.length; i++) {
            if (ALGORITHMS[i].equals(current)) index = i;
        }
        index = (index + 1) % ALGORITHMS.length;
        this.channelAlgos[this.activeChannel] = ALGORITHMS[index];
    }
    
    private void cycleBandwidth() {
        String current = this.channelBws[this.activeChannel];
        int index = 0;
        for (int i = 0; i < BANDWIDTHS.length; i++) {
            if (BANDWIDTHS[i].equals(current)) index = i;
        }
        index = (index + 1) % BANDWIDTHS.length;
        this.channelBws[this.activeChannel] = BANDWIDTHS[index];
    }

    private void saveCurrentChannelLocally() {
        try {
            float parsed = Float.parseFloat(this.freqField.getValue());
            this.channelFreqs[this.activeChannel] = Math.max(minFreq, Math.min(maxFreq, parsed));
        } catch (Exception ignored) {}
        this.channelKeys[this.activeChannel] = this.keyField.getValue();
    }
    
    private void loadChannelLocally() {
        this.freqField.setValue(String.format(Locale.US, "%.3f", this.channelFreqs[this.activeChannel]));
        this.keyField.setValue(this.channelKeys[this.activeChannel]);
    }

    private void playClick() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), 0.5f, 1.2f);
        }
    }

    private void saveAllToNetwork() {
        saveCurrentChannelLocally();
        
        String[] freqs = new String[5];
        String[] algos = new String[5];
        String[] keys = new String[5];
        String[] bws = new String[5];
        
        for (int i = 0; i < 5; i++) {
            freqs[i] = String.format(Locale.US, "%.3f", this.channelFreqs[i]);
            algos[i] = this.channelAlgos[i];
            keys[i] = this.channelKeys[i];
            bws[i] = this.channelBws[i];
        }
        
        RadioNetwork.CHANNEL.sendToServer(new RadioNetwork.SyncRadioFrequencyPacket(this.activeChannel, freqs, algos, keys, bws, this.currentVolumeValue, this.hand == InteractionHand.MAIN_HAND));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isDraggingTune || this.isDraggingVolume) {
            this.isDraggingTune = false;
            this.isDraggingVolume = false;
            saveAllToNetwork();
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.literal("§a[RADIO] Config Saved"), true);
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        if (this.isDraggingTune) {
            updateFreqFromMouse(mouseX, centerX - 70, 140);
            return true;
        } else if (this.isDraggingVolume) {
            int sliderY = centerY - 50;
            int sliderH = 95;
            updateVolumeFromMouse(mouseY, sliderY, sliderH);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            saveAllToNetwork();
            this.freqField.setFocused(false);
            this.keyField.setFocused(false);
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.literal("§a[RADIO] Channel Saved"), true);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        saveAllToNetwork();
        super.onClose();
    }

    private void updateFreqFromMouse(double mouseX, int displayX, int displayW) {
        float percent = (float)(mouseX - (displayX + 10)) / (displayW - 20);
        percent = Math.max(0.0f, Math.min(1.0f, percent));
        float newFreq = minFreq + (percent * (maxFreq - minFreq));
        this.channelFreqs[this.activeChannel] = newFreq;
        this.freqField.setValue(String.format(Locale.US, "%.3f", newFreq));
    }
    
    private void updateVolumeFromMouse(double mouseY, int sliderTopY, int sliderHeight) {
        float percent = 1.0f - (float)(mouseY - sliderTopY) / sliderHeight;
        this.currentVolumeValue = Math.max(0.0f, Math.min(1.0f, percent));
    }

    @Override
    public boolean isPauseScreen() { return false; }
}