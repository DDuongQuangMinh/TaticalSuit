package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.client.audio.VoiceManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AudioDeviceScreen extends Screen {

    private List<String> inputs;
    private List<String> outputs;
    
    private int currentInputIndex = 0;
    private int currentOutputIndex = 0;
    
    private boolean micOpen = false;
    private boolean speakerOpen = false;
    private float micScroll = 0f;
    private float speakerScroll = 0f;

    public AudioDeviceScreen() {
        super(Component.literal("Radio Hardware Setup"));
    }

    @Override
    protected void init() {
        super.init();

        // Fetching devices directly from native OpenAL bindings now
        this.inputs = VoiceManager.getAvailableInputs();
        this.outputs = VoiceManager.getAvailableOutputs();

        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).equals(VoiceManager.currentMicName)) currentInputIndex = i;
        }
        for (int i = 0; i < outputs.size(); i++) {
            if (outputs.get(i).equals(VoiceManager.currentSpeakerName)) currentOutputIndex = i;
        }

        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;

        this.addRenderableWidget(Button.builder(
            Component.literal("§aApply & Restart Audio Engine"),
            button -> {
                String targetMic = inputs.get(currentInputIndex);
                String targetSpeaker = outputs.get(currentOutputIndex);
                
                new Thread(() -> VoiceManager.restartAudioEngine(targetMic, targetSpeaker)).start();
                
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.displayClientMessage(Component.literal("§e[RADIO] Rebinding Hardware to OpenAL..."), true);
                }
                this.onClose();
            }
        ).bounds(centerX - 100, startY + 110, 200, 20).build());
        
        // Voice Activation Toggle Button
        this.addRenderableWidget(Button.builder(
            Component.literal(VoiceManager.useVoiceActivation ? "§aInput Mode: Voice Activation" : "§cInput Mode: Push To Talk"),
            button -> {
                VoiceManager.useVoiceActivation = !VoiceManager.useVoiceActivation;
                button.setMessage(Component.literal(VoiceManager.useVoiceActivation ? "§aInput Mode: Voice Activation" : "§cInput Mode: Push To Talk"));
            }
        ).bounds(centerX - 100, startY + 80, 200, 20).build());
    }

    private String truncate(String text) {
        if (text.length() > 38) {
            return text.substring(0, 35) + "...";
        }
        return text;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        
        guiGraphics.drawCenteredString(this.font, "TACTICAL RADIO HARDWARE", this.width / 2, this.height / 2 - 70, 0xFFD62929);
        guiGraphics.drawCenteredString(this.font, "OpenAL Native Capture Engine - Select your target microphone.", this.width / 2, this.height / 2 - 55, 0xAAAAAA);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;
        int micX = centerX - 150;
        int micY = startY;
        int spkX = centerX - 150;
        int spkY = startY + 40; 
        int boxW = 300;
        int boxH = 20;

        renderComboBox(guiGraphics, "Microphone: " + truncate(inputs.get(currentInputIndex)), micX, micY, boxW, boxH, mouseX, mouseY, micOpen);
        renderComboBox(guiGraphics, "Speaker: " + truncate(outputs.get(currentOutputIndex)), spkX, spkY, boxW, boxH, mouseX, mouseY, speakerOpen);

        if (speakerOpen) {
            renderDropdown(guiGraphics, spkX, spkY, boxW, outputs, currentOutputIndex, speakerOpen, speakerScroll, mouseX, mouseY);
        }
        if (micOpen) {
            renderDropdown(guiGraphics, micX, micY, boxW, inputs, currentInputIndex, micOpen, micScroll, mouseX, mouseY);
        }
    }

    private void renderComboBox(GuiGraphics guiGraphics, String text, int x, int y, int w, int h, int mouseX, int mouseY, boolean isOpen) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bgColor = hovered || isOpen ? 0xFF555555 : 0xFF333333;
        
        guiGraphics.fill(x, y, x + w, y + h, 0xFF000000); 
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor); 
        
        guiGraphics.drawString(this.font, text, x + 5, y + 6, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, isOpen ? "▲" : "▼", x + w - 12, y + 6, 0xFFAAAAAA, false);
    }

    private void renderDropdown(GuiGraphics guiGraphics, int x, int y, int w, List<String> items, int selectedIndex, boolean isOpen, float scroll, int mouseX, int mouseY) {
        if (!isOpen) return;
        
        int itemHeight = 14;
        int maxDropHeight = 100;
        int totalHeight = items.size() * itemHeight + 4;
        int dropHeight = Math.min(totalHeight, maxDropHeight);
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300); 
        
        guiGraphics.fill(x, y + 20, x + w, y + 20 + dropHeight, 0xFF555555); 
        guiGraphics.fill(x + 1, y + 21, x + w - 1, y + 20 + dropHeight - 1, 0xFF111111); 
        
        guiGraphics.enableScissor(x + 1, y + 21, x + w - 1, y + 20 + dropHeight - 1);
        
        int listY = y + 22 - (int)scroll;
        for (int i = 0; i < items.size(); i++) {
            int itemY = listY + (i * itemHeight);
            
            if (itemY + itemHeight >= y + 21 && itemY <= y + 20 + dropHeight - 1) {
                boolean hovered = mouseX >= x + 1 && mouseX <= x + w - 1 && mouseY >= itemY && mouseY <= itemY + itemHeight;
                if (hovered) {
                    guiGraphics.fill(x + 1, itemY, x + w - 1, itemY + itemHeight, 0xFF333333);
                }
                int textColor = (i == selectedIndex) ? 0xFF00FF00 : (hovered ? 0xFFFFFFFF : 0xFFAAAAAA);
                guiGraphics.drawString(this.font, truncate(items.get(i)), x + 5, itemY + 3, textColor, false);
            }
        }
        
        guiGraphics.disableScissor();
        
        if (totalHeight > maxDropHeight) {
            int scrollbarX = x + w - 6;
            guiGraphics.fill(scrollbarX, y + 22, scrollbarX + 4, y + 20 + dropHeight - 2, 0xFF333333);
            int thumbHeight = Math.max(10, (int)((float)maxDropHeight / totalHeight * (maxDropHeight - 4)));
            int thumbY = y + 22 + (int)((scroll / (totalHeight - maxDropHeight)) * (maxDropHeight - 4 - thumbHeight));
            guiGraphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFF888888);
        }
        
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int startY = this.height / 2 - 40;
            
            int micX = centerX - 150;
            int micY = startY;
            int spkX = centerX - 150;
            int spkY = startY + 40;
            int dropW = 300;
            
            if (micOpen) {
                int dropH = Math.min(inputs.size() * 14 + 4, 100);
                if (mouseX >= micX && mouseX <= micX + dropW && mouseY >= micY + 20 && mouseY <= micY + 20 + dropH) {
                    int index = (int) (((mouseY - (micY + 22)) + micScroll) / 14);
                    if (index >= 0 && index < inputs.size()) {
                        currentInputIndex = index;
                        micOpen = false;
                    }
                    return true;
                }
                micOpen = false; 
                return true;
            }
            
            if (speakerOpen) {
                int dropH = Math.min(outputs.size() * 14 + 4, 100);
                if (mouseX >= spkX && mouseX <= spkX + dropW && mouseY >= spkY + 20 && mouseY <= spkY + 20 + dropH) {
                    int index = (int) (((mouseY - (spkY + 22)) + speakerScroll) / 14);
                    if (index >= 0 && index < outputs.size()) {
                        currentOutputIndex = index;
                        speakerOpen = false;
                    }
                    return true;
                }
                speakerOpen = false; 
                return true;
            }
            
            if (mouseX >= micX && mouseX <= micX + dropW && mouseY >= micY && mouseY <= micY + 20) {
                micOpen = true;
                speakerOpen = false;
                return true;
            }
            if (mouseX >= spkX && mouseX <= spkX + dropW && mouseY >= spkY && mouseY <= spkY + 20) {
                speakerOpen = true;
                micOpen = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;
        int dropW = 300;
        
        if (micOpen) {
            int micX = centerX - 150;
            int micY = startY;
            int dropH = Math.min(inputs.size() * 14 + 4, 100);
            if (mouseX >= micX && mouseX <= micX + dropW && mouseY >= micY + 20 && mouseY <= micY + 20 + dropH) {
                micScroll -= delta * 14;
                float maxScroll = Math.max(0, inputs.size() * 14 + 4 - 100);
                micScroll = Math.max(0, Math.min(micScroll, maxScroll));
                return true;
            }
        }
        
        if (speakerOpen) {
            int spkX = centerX - 150;
            int spkY = startY + 40;
            int dropH = Math.min(outputs.size() * 14 + 4, 100);
            if (mouseX >= spkX && mouseX <= spkX + dropW && mouseY >= spkY + 20 && mouseY <= spkY + 20 + dropH) {
                speakerScroll -= delta * 14;
                float maxScroll = Math.max(0, outputs.size() * 14 + 4 - 100);
                speakerScroll = Math.max(0, Math.min(speakerScroll, maxScroll));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}