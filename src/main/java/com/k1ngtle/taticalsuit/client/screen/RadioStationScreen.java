package com.k1ngtle.taticalsuit.client.screen;

import com.k1ngtle.taticalsuit.block.RadioStationBlockEntity;
import com.k1ngtle.taticalsuit.network.RadioNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RadioStationScreen extends Screen {

    private final RadioStationBlockEntity station;

    private final int windowWidth = 500;
    private final int windowHeight = 350;
    private int windowX;
    private int windowY;

    private int activeTab = 0; // 0 = Physical, 1 = Config, 2 = CLI

    private boolean isOn;
    private String frequency;
    private String algo;
    private String key;
    private boolean isIntercepting;

    private final List<String> terminalLog = new ArrayList<>();
    private String currentInput = "";
    private int logScroll = 0;

    public RadioStationScreen(RadioStationBlockEntity station) {
        super(Component.literal("Tactical Router Console"));
        this.station = station;

        this.isOn = station.isOn();
        this.frequency = station.getFrequency();
        this.algo = station.getAlgo();
        this.key = station.getKey();
        this.isIntercepting = station.isIntercepting();

        terminalLog.add("Cisco Systems Console Connection Established.");
        terminalLog.add("Interfacing with L3Harris RT-1694D(P)(C)/U Base Station...");
        terminalLog.add("");
        terminalLog.add("FALCON III RT-1694D BOOT...");
        terminalLog.add("OS LOADED. WAITING FOR INPUT.");
        if (this.isIntercepting) {
            if (this.frequency.equals("0.000") || this.frequency.equals("0.0")) {
                terminalLog.add("WARNING: WIDEBAND SCANNER ACTIVE.");
            } else {
                terminalLog.add("WARNING: MITM INTERCEPT ACTIVE.");
            }
        }
    }

    public static void open(RadioStationBlockEntity station) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new RadioStationScreen(station));
    }

    @Override
    protected void init() {
        super.init();
        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;
    }

    private void syncToServer() {
        RadioNetwork.CHANNEL.sendToServer(new RadioNetwork.UpdateStationPacket(
                station.getBlockPos(), isOn, frequency, algo, key, isIntercepting
        ));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        guiGraphics.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0xFFEEEEEE);
        guiGraphics.fill(windowX, windowY, windowX + windowWidth, windowY + 18, 0xFFFFFFFF);
        guiGraphics.fill(windowX, windowY + 18, windowX + windowWidth, windowY + 19, 0xFFAAAAAA);
        
        guiGraphics.fill(windowX, windowY, windowX + 1, windowY + windowHeight, 0xFF777777);
        guiGraphics.fill(windowX + windowWidth - 1, windowY, windowX + windowWidth, windowY + windowHeight, 0xFF777777);
        guiGraphics.fill(windowX, windowY, windowX + windowWidth, windowY + 1, 0xFF777777);
        guiGraphics.fill(windowX, windowY + windowHeight - 1, windowX + windowWidth, windowY + windowHeight, 0xFF777777);

        guiGraphics.drawString(this.font, "Tactical Router Console - Harris RT-1694D", windowX + 8, windowY + 6, 0xFF000000, false);

        int tabY = windowY + 22;
        String[] tabNames = {"Physical", "Config", "CLI"};
        int currentTabX = windowX + 15;

        guiGraphics.fill(windowX + 10, windowY + 42, windowX + windowWidth - 10, windowY + 43, 0xFFAAAAAA);

        for (int i = 0; i < 3; i++) {
            int tabW = 70;
            
            if (activeTab == i) {
                guiGraphics.fill(currentTabX, tabY, currentTabX + tabW, tabY + 21, 0xFFFFFFFF); 
                guiGraphics.fill(currentTabX, tabY, currentTabX + tabW, tabY + 1, 0xFFAAAAAA); 
                guiGraphics.fill(currentTabX, tabY, currentTabX + 1, tabY + 21, 0xFFAAAAAA); 
                guiGraphics.fill(currentTabX + tabW - 1, tabY, currentTabX + tabW, tabY + 21, 0xFFAAAAAA); 
                guiGraphics.drawCenteredString(this.font, tabNames[i], currentTabX + (tabW / 2), tabY + 6, 0xFF000000);
            } else {
                guiGraphics.fill(currentTabX, tabY + 2, currentTabX + tabW, tabY + 20, 0xFFDDDDDD);
                guiGraphics.fill(currentTabX, tabY + 2, currentTabX + tabW, tabY + 3, 0xFFAAAAAA); 
                guiGraphics.fill(currentTabX, tabY + 2, currentTabX + 1, tabY + 20, 0xFFAAAAAA); 
                guiGraphics.fill(currentTabX + tabW - 1, tabY + 2, currentTabX + tabW, tabY + 20, 0xFFAAAAAA); 
                guiGraphics.drawCenteredString(this.font, tabNames[i], currentTabX + (tabW / 2), tabY + 7, 0xFF000000);
            }
            currentTabX += tabW + 4;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (activeTab == 0) {
            renderPhysicalTab(guiGraphics, mouseX, mouseY, windowWidth, windowHeight);
        } else if (activeTab == 1) {
            renderConfigTab(guiGraphics, windowWidth, windowHeight);
        } else if (activeTab == 2) {
            renderCLITab(guiGraphics, windowWidth, windowHeight);
        }
    }

    private void renderPhysicalTab(GuiGraphics guiGraphics, int mouseX, int mouseY, int winW, int winH) {
        int contentX = windowX + 15;
        int contentY = windowY + 50;

        guiGraphics.fill(contentX, contentY, contentX + 110, contentY + 225, 0xFFFFFFFF);
        guiGraphics.fill(contentX, contentY, contentX + 110, contentY + 1, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY + 224, contentX + 110, contentY + 225, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY, contentX + 1, contentY + 225, 0xFFAAAAAA);
        guiGraphics.fill(contentX + 109, contentY, contentX + 110, contentY + 225, 0xFFAAAAAA);

        guiGraphics.drawString(this.font, "MODULES", contentX + 5, contentY + 5, 0xFF000000, false);
        guiGraphics.fill(contentX + 1, contentY + 18, contentX + 109, contentY + 32, 0xFFDDDDDD); 
        guiGraphics.drawString(this.font, "RT-1694D-CHASSIS", contentX + 5, contentY + 21, 0xFF000000, false);
        
        guiGraphics.drawString(this.font, "PWR-SUPPLY-AC", contentX + 5, contentY + 35, 0xFF000000, false);
        guiGraphics.drawString(this.font, "ANTENNA-VHF", contentX + 5, contentY + 49, 0xFF000000, false);
        guiGraphics.drawString(this.font, "ANTENNA-UHF", contentX + 5, contentY + 63, 0xFF000000, false);
        guiGraphics.drawString(this.font, "SPKR-EXTERNAL", contentX + 5, contentY + 77, 0xFF000000, false);
        guiGraphics.drawString(this.font, "DATA-LINK-CBL", contentX + 5, contentY + 91, 0xFF000000, false);

        int viewX = contentX + 120;
        int viewY = contentY;
        int viewWidth = winW - 150;
        int viewHeight = 225;

        guiGraphics.fill(viewX, viewY, viewX + viewWidth, viewY + viewHeight, 0xFFFFFFFF);
        guiGraphics.fill(viewX, viewY, viewX + viewWidth, viewY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX, viewY + viewHeight - 1, viewX + viewWidth, viewY + viewHeight, 0xFFAAAAAA);
        guiGraphics.fill(viewX, viewY, viewX + 1, viewY + viewHeight, 0xFFAAAAAA);
        guiGraphics.fill(viewX + viewWidth - 1, viewY, viewX + viewWidth, viewY + viewHeight, 0xFFAAAAAA);

        guiGraphics.drawCenteredString(this.font, "Physical Device View", viewX + (viewWidth / 2), viewY + 8, 0xFF000000);

        int btnY = viewY + 25;
        guiGraphics.fill(viewX + 25, btnY, viewX + 105, btnY + 15, 0xFFDDDDDD);
        guiGraphics.fill(viewX + 25, btnY, viewX + 105, btnY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 25, btnY+14, viewX + 105, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 25, btnY, viewX + 26, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 104, btnY, viewX + 105, btnY + 15, 0xFFAAAAAA);
        guiGraphics.drawCenteredString(this.font, "Zoom In", viewX + 65, btnY + 4, 0xFF000000);
        
        guiGraphics.fill(viewX + 115, btnY, viewX + 235, btnY + 15, 0xFFDDDDDD);
        guiGraphics.fill(viewX + 115, btnY, viewX + 235, btnY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 115, btnY+14, viewX + 235, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 115, btnY, viewX + 116, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 234, btnY, viewX + 235, btnY + 15, 0xFFAAAAAA);
        guiGraphics.drawCenteredString(this.font, "Original Size", viewX + 175, btnY + 4, 0xFF000000);
        
        guiGraphics.fill(viewX + 245, btnY, viewX + 325, btnY + 15, 0xFFDDDDDD);
        guiGraphics.fill(viewX + 245, btnY, viewX + 325, btnY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 245, btnY+14, viewX + 325, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 245, btnY, viewX + 246, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 324, btnY, viewX + 325, btnY + 15, 0xFFAAAAAA);
        guiGraphics.drawCenteredString(this.font, "Zoom Out", viewX + 285, btnY + 4, 0xFF000000);

        int chassisX = viewX + 25;
        int chassisY = viewY + 60;
        int chassisW = viewWidth - 50;
        int chassisH = 135;

        guiGraphics.fill(chassisX, chassisY, chassisX + chassisW, chassisY + chassisH, 0xFF4A5A35);
        guiGraphics.fill(chassisX, chassisY + 65, chassisX + chassisW, chassisY + 67, 0xFF354224);

        guiGraphics.fill(chassisX + 85, chassisY + 15, chassisX + 215, chassisY + 60, 0xFF111511);
        if (isOn) {
            guiGraphics.drawString(this.font, "HARRIS OS 2.4", chassisX + 90, chassisY + 20, 0xFF00FF00, false);
            
            if (frequency.equals("0.000") || frequency.equals("0.0")) {
                guiGraphics.drawString(this.font, "MODE: SCANNER", chassisX + 90, chassisY + 32, 0xFF00FF00, false);
                if (isIntercepting) {
                    guiGraphics.drawString(this.font, "[SWEEPING FREQS]", chassisX + 90, chassisY + 44, 0xFFD62929, false);
                }
            } else {
                guiGraphics.drawString(this.font, "FREQ: " + frequency + " MHz", chassisX + 90, chassisY + 32, 0xFF00FF00, false);
                guiGraphics.drawString(this.font, "ALGO: " + algo, chassisX + 90, chassisY + 44, 0xFF00FF00, false);
                if (isIntercepting) {
                    guiGraphics.drawString(this.font, "[MITM HACK ACTIVE]", chassisX + 90, chassisY + 56, 0xFFD62929, false);
                }
            }
        }

        drawScaledString(guiGraphics, "AUDIO", chassisX + 31, chassisY + 18, 0.7f, 0xFF000000);
        guiGraphics.fill(chassisX + 33, chassisY + 26, chassisX + 45, chassisY + 38, 0xFF000000);
        guiGraphics.fill(chassisX + 35, chassisY + 28, chassisX + 43, chassisY + 36, 0xFF4488FF);

        drawScaledString(guiGraphics, "DATA", chassisX + 33, chassisY + 45, 0.7f, 0xFF000000);
        guiGraphics.fill(chassisX + 33, chassisY + 53, chassisX + 45, chassisY + 65, 0xFF000000);
        guiGraphics.fill(chassisX + 35, chassisY + 55, chassisX + 43, chassisY + 63, 0xFF4488FF);

        drawScaledString(guiGraphics, "RT-1694D(P)(C)/U", chassisX + 225, chassisY + 10, 0.8f, 0xFFFFFFFF);
        guiGraphics.drawString(this.font, "HARRIS", chassisX + 235, chassisY + 20, 0xFFFFFFFF, false);

        drawScaledString(guiGraphics, "ANTENNA", chassisX + 258, chassisY + 32, 0.6f, 0xFFFFFFFF);
        guiGraphics.fill(chassisX + 265, chassisY + 38, chassisX + 281, chassisY + 48, 0xFF222222);

        drawScaledString(guiGraphics, "ACCESSORY", chassisX + 236, chassisY + 43, 0.6f, 0xFF000000);
        guiGraphics.fill(chassisX + 243, chassisY + 49, chassisX + 255, chassisY + 61, 0xFF000000);
        guiGraphics.fill(chassisX + 245, chassisY + 51, chassisX + 253, chassisY + 59, 0xFF4488FF);

        drawScaledString(guiGraphics, "PWR", chassisX + 220, chassisY + 36, 0.6f, 0xFFFFFFFF);
        int pwrColor = isOn ? 0xFF00FF00 : 0xFF550000;
        guiGraphics.fill(chassisX + 218, chassisY + 44, chassisX + 234, chassisY + 60, 0xFF000000); 
        guiGraphics.fill(chassisX + 220, chassisY + 46, chassisX + 232, chassisY + 58, pwrColor); 

        drawScaledString(guiGraphics, "SPKR", chassisX + 150, chassisY + 75, 0.6f, 0xFF000000);
        guiGraphics.fill(chassisX + 152, chassisY + 83, chassisX + 164, chassisY + 95, 0xFF000000);
        guiGraphics.fill(chassisX + 154, chassisY + 85, chassisX + 162, chassisY + 93, 0xFF4488FF);

        drawScaledString(guiGraphics, "AUX", chassisX + 185, chassisY + 75, 0.6f, 0xFF000000);
        guiGraphics.fill(chassisX + 182, chassisY + 83, chassisX + 194, chassisY + 95, 0xFF000000);
        guiGraphics.fill(chassisX + 184, chassisY + 85, chassisX + 192, chassisY + 93, 0xFF4488FF);

        drawScaledString(guiGraphics, "PA CTL", chassisX + 165, chassisY + 105, 0.6f, 0xFF000000);
        guiGraphics.fill(chassisX + 167, chassisY + 113, chassisX + 179, chassisY + 125, 0xFF000000);
        guiGraphics.fill(chassisX + 169, chassisY + 115, chassisX + 177, chassisY + 123, 0xFF4488FF);

        guiGraphics.fill(chassisX + 210, chassisY + 75, chassisX + 285, chassisY + 125, 0xFF354224);
        for (int gx = 0; gx < 9; gx++) {
            for (int gy = 0; gy < 6; gy++) {
                guiGraphics.fill(chassisX + 215 + (gx * 8), chassisY + 80 + (gy * 8), chassisX + 218 + (gx * 8), chassisY + 83 + (gy * 8), 0xFF111111);
            }
        }

        guiGraphics.fill(contentX, contentY + 235, contentX + winW - 30, contentY + 285, 0xFFFFFFFF);
        guiGraphics.fill(contentX, contentY + 235, contentX + winW - 30, contentY + 236, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY + 284, contentX + winW - 30, contentY + 285, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY + 235, contentX + 1, contentY + 285, 0xFFAAAAAA);
        guiGraphics.fill(contentX + winW - 31, contentY + 235, contentX + winW - 30, contentY + 285, 0xFFAAAAAA);

        guiGraphics.drawString(this.font, "The RT-1694D provides secure military-grade communications routing.", contentX + 5, contentY + 240, 0xFF000000, false);
        guiGraphics.drawString(this.font, "Tune to 0.000 MHz and activate Intercept for WIDEBAND SCAN MODE.", contentX + 5, contentY + 254, 0xFF000000, false);
        guiGraphics.drawString(this.font, "Configure cryptography and tuning via the CLI terminal tab.", contentX + 5, contentY + 268, 0xFF000000, false);
    }

    private void renderConfigTab(GuiGraphics guiGraphics, int winW, int winH) {
        int contentX = windowX + 15;
        int contentY = windowY + 50;

        guiGraphics.fill(contentX, contentY, contentX + 110, contentY + 285, 0xFFFFFFFF);
        guiGraphics.fill(contentX, contentY, contentX + 110, contentY + 1, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY + 284, contentX + 110, contentY + 285, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY, contentX + 1, contentY + 285, 0xFFAAAAAA);
        guiGraphics.fill(contentX + 109, contentY, contentX + 110, contentY + 285, 0xFFAAAAAA);

        guiGraphics.drawString(this.font, "GLOBAL", contentX + 5, contentY + 5, 0xFF000000, false);
        guiGraphics.drawString(this.font, "  Settings", contentX + 5, contentY + 19, 0xFF333333, false);
        guiGraphics.fill(contentX + 1, contentY + 18, contentX + 109, contentY + 29, 0xFFDDDDDD); 
        guiGraphics.drawString(this.font, "  Settings", contentX + 5, contentY + 19, 0xFF000000, false);
        
        guiGraphics.drawString(this.font, "ROUTING", contentX + 5, contentY + 43, 0xFF000000, false);
        guiGraphics.drawString(this.font, "  Static", contentX + 5, contentY + 57, 0xFFAAAAAA, false);
        
        guiGraphics.drawString(this.font, "INTERFACE", contentX + 5, contentY + 81, 0xFF000000, false);
        guiGraphics.drawString(this.font, "  FastEthernet0/0", contentX + 5, contentY + 95, 0xFF555555, false);
        guiGraphics.drawString(this.font, "  VHF Antenna", contentX + 5, contentY + 109, 0xFF555555, false);

        int viewX = contentX + 120;
        int viewY = contentY;
        int viewWidth = winW - 150;
        int viewHeight = 285;

        guiGraphics.fill(viewX, viewY, viewX + viewWidth, viewY + viewHeight, 0xFFFFFFFF);
        guiGraphics.fill(viewX, viewY, viewX + viewWidth, viewY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX, viewY + viewHeight - 1, viewX + viewWidth, viewY + viewHeight, 0xFFAAAAAA);
        guiGraphics.fill(viewX, viewY, viewX + 1, viewY + viewHeight, 0xFFAAAAAA);
        guiGraphics.fill(viewX + viewWidth - 1, viewY, viewX + viewWidth, viewY + viewHeight, 0xFFAAAAAA);

        guiGraphics.drawCenteredString(this.font, "Device Configuration", viewX + (viewWidth / 2), viewY + 8, 0xFF000000);

        int textX = viewX + 15;
        int startY = viewY + 35;
        
        guiGraphics.drawString(this.font, "Power Status:", textX, startY, 0xFF000000, false);
        guiGraphics.drawString(this.font, isOn ? "ONLINE" : "OFFLINE", textX + 100, startY, isOn ? 0xFF00AA00 : 0xFFD62929, false);

        guiGraphics.drawString(this.font, "Tuned Frequency:", textX, startY + 25, 0xFF000000, false);
        guiGraphics.drawString(this.font, frequency.equals("0.000") || frequency.equals("0.0") ? "SWEEPING ALL" : frequency + " MHz", textX + 100, startY + 25, 0xFF000000, false);

        guiGraphics.drawString(this.font, "Encryption Algo:", textX, startY + 50, 0xFF000000, false);
        guiGraphics.drawString(this.font, frequency.equals("0.000") || frequency.equals("0.0") ? "N/A (SCANNING)" : algo, textX + 100, startY + 50, 0xFF000000, false);

        guiGraphics.drawString(this.font, "MITM Hack Status:", textX, startY + 75, 0xFF000000, false);
        String interceptStatus = isIntercepting ? (frequency.equals("0.000") || frequency.equals("0.0") ? "SWEEPING SIGNALS" : "EXPLOITING PACKETS") : "DISABLED";
        int interceptColor = isIntercepting ? 0xFFD62929 : 0xFF000000;
        guiGraphics.drawString(this.font, interceptStatus, textX + 100, startY + 75, interceptColor, false);
        
        guiGraphics.drawString(this.font, "Note: All configurations must be applied", textX, startY + 125, 0xFF555555, false);
        guiGraphics.drawString(this.font, "manually via the CLI interface.", textX, startY + 139, 0xFF555555, false);
    }

    private void renderCLITab(GuiGraphics guiGraphics, int winW, int winH) {
        int cliX = windowX + 15;
        int cliY = windowY + 50;
        int cliW = winW - 30;
        int cliH = winH - 65;

        guiGraphics.fill(cliX - 2, cliY - 2, cliX + cliW + 2, cliY + cliH + 2, 0xFFFFFFFF);
        guiGraphics.fill(cliX - 2, cliY - 2, cliX + cliW + 2, cliY - 1, 0xFFAAAAAA);
        guiGraphics.fill(cliX - 2, cliY + cliH + 1, cliX + cliW + 2, cliY + cliH + 2, 0xFFAAAAAA);
        guiGraphics.fill(cliX - 2, cliY - 2, cliX - 1, cliY + cliH + 2, 0xFFAAAAAA);
        guiGraphics.fill(cliX + cliW + 1, cliY - 2, cliX + cliW + 2, cliY + cliH + 2, 0xFFAAAAAA);

        guiGraphics.fill(cliX, cliY, cliX + cliW, cliY + cliH, 0xFF000000);

        int maxLines = 26;
        int startIndex = Math.max(0, terminalLog.size() - maxLines + logScroll);
        int yOffset = cliY + 5;

        for (int i = startIndex; i < Math.min(terminalLog.size(), startIndex + maxLines); i++) {
            guiGraphics.drawString(this.font, terminalLog.get(i), cliX + 5, yOffset, 0xFF00FF00, false);
            yOffset += 10;
        }

        if (terminalLog.size() < maxLines || startIndex + maxLines >= terminalLog.size()) {
            String prefix = this.isOn ? "RT-1694D> " : "> ";
            String cursor = (System.currentTimeMillis() % 1000 > 500) ? "_" : "";
            guiGraphics.drawString(this.font, prefix + currentInput + cursor, cliX + 5, yOffset, 0xFF00FF00, false);
        }
    }

    private void drawScaledString(GuiGraphics guiGraphics, String text, int x, int y, float scale, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.drawString(this.font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int tabY = windowY + 22;
            int currentTabX = windowX + 15;
            for (int i = 0; i < 3; i++) {
                int tabW = 70;
                if (mouseX >= currentTabX && mouseX <= currentTabX + tabW && mouseY >= tabY && mouseY <= tabY + 20) {
                    this.activeTab = i;
                    playClickSound();
                    return true;
                }
                currentTabX += tabW + 4;
            }

            if (activeTab == 0) {
                int viewX = windowX + 15 + 120;
                int chassisX = viewX + 25;
                int chassisY = windowY + 50 + 60;
                
                int btnX = chassisX + 218;
                int btnY = chassisY + 44;

                if (mouseX >= btnX - 2 && mouseX <= btnX + 18 && mouseY >= btnY - 2 && mouseY <= btnY + 18) {
                    this.isOn = !this.isOn;
                    syncToServer();
                    terminalLog.add(this.isOn ? "SYSTEM BOOTING... ONLINE." : "SYSTEM SHUTTING DOWN... OFFLINE.");
                    playClickSound();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playClickSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), 0.5f, 1.2f);
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (activeTab == 2) {
            if (this.font.width("> " + currentInput + codePoint) < (windowWidth - 30)) {
                currentInput += codePoint;
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeTab == 2) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !currentInput.isEmpty()) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                processCommand(currentInput.trim());
                currentInput = "";
                logScroll = 0; 
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (activeTab == 2) {
            if (delta > 0) {
                logScroll = Math.max(-terminalLog.size() + 15, logScroll - 1);
            } else if (delta < 0) {
                logScroll = Math.min(0, logScroll + 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void processCommand(String cmd) {
        String prefix = this.isOn ? "RT-1694D> " : "> ";
        terminalLog.add(prefix + cmd);
        
        if (cmd.isEmpty()) return;
        
        String lowerCmd = cmd.toLowerCase();

        if (lowerCmd.equals("power")) {
            this.isOn = !this.isOn;
            terminalLog.add(this.isOn ? "SYSTEM BOOTING... ONLINE." : "SYSTEM SHUTTING DOWN... OFFLINE.");
            syncToServer();
            return;
        }

        if (!this.isOn) {
            terminalLog.add("ERROR: System offline. Click the physical PWR button or type 'power' to boot.");
            return;
        }

        if (lowerCmd.equals("help")) {
            terminalLog.add("AVAILABLE COMMANDS:");
            terminalLog.add("- power           : Toggles device power");
            terminalLog.add("- tune <freq>     : Sets routing frequency (e.g. tune 446.025)");
            terminalLog.add("                    Use 'tune 0' for Wideband Sweeping.");
            terminalLog.add("- crypto <alg> <k>: Sets encryption algo and key");
            terminalLog.add("- intercept       : Toggles MITM Sweeping / Hack bypass");
            terminalLog.add("- clear           : Clears terminal log");
        } else if (lowerCmd.equals("clear")) {
            terminalLog.clear();
            terminalLog.add("Terminal cleared.");
        } else if (lowerCmd.startsWith("tune ")) {
            String val = cmd.substring(5).trim();
            try {
                float fVal = Float.parseFloat(val);
                this.frequency = String.format(Locale.US, "%.3f", fVal);
                if (this.frequency.equals("0.000") || this.frequency.equals("0.0")) {
                    terminalLog.add("SUCCESS: Tuned to 0.000 MHz. WIDEBAND SCAN MODE READY.");
                } else {
                    terminalLog.add("SUCCESS: RF Tuned to " + this.frequency + " MHz");
                }
                syncToServer();
            } catch (NumberFormatException e) {
                terminalLog.add("ERROR: Invalid frequency format. Use a number like 446.025");
            }
        } else if (lowerCmd.startsWith("crypto ")) {
            String[] parts = cmd.substring(7).trim().split(" ", 2);
            if (parts.length >= 1) {
                this.algo = parts[0].toUpperCase();
                this.key = parts.length == 2 ? parts[1] : "";
                terminalLog.add("SUCCESS: Algorithm set to " + this.algo);
                terminalLog.add("SUCCESS: Key updated.");
                syncToServer();
            } else {
                terminalLog.add("ERROR: Usage: crypto <algo> <key>");
            }
        } else if (lowerCmd.equals("intercept")) {
            this.isIntercepting = !this.isIntercepting;
            if (this.isIntercepting) {
                if (this.frequency.equals("0.000") || this.frequency.equals("0.0")) {
                    terminalLog.add("WARNING: Wideband Scanning ACTIVE.");
                } else {
                    terminalLog.add("WARNING: MITM Exploitation ACTIVE.");
                }
            } else {
                terminalLog.add("Intercept sequence TERMINATED.");
            }
            syncToServer();
        } else {
            terminalLog.add("ERROR: Unknown command. Type 'help' for a list of commands.");
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}