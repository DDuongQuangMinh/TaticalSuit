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

    private final int windowWidth = 560;
    private final int windowHeight = 390;
    private int windowX;
    private int windowY;

    private int activeTab = 0; // 0 = Physical, 1 = Config, 2 = CLI

    private boolean isOn;
    private String frequency;
    private String algo;
    private String key;
    private boolean isIntercepting;

    private float zoomLevel = 1.0f;

    // Drag and Drop Variables
    private final String[] AVAILABLE_MODULES = {
        "PWR-SUPPLY-AC", 
        "ANTENNA-VHF", 
        "ANTENNA-UHF", 
        "SPKR-EXTERNAL", 
        "DATA-LINK-CBL"
    };
    
    private String installedPower = "PWR-SUPPLY-AC";
    private String installedAntenna = "ANTENNA-VHF";
    private String installedSpeaker = "SPKR-EXTERNAL";
    private String installedData = "DATA-LINK-CBL";
    
    private String draggedModule = null;

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

        drawClearText(guiGraphics, "Tactical Router Console - Harris RT-1694D", windowX + 8, windowY + 6, 0xFF000000);

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
                drawClearCenteredText(guiGraphics, tabNames[i], currentTabX + (tabW / 2), tabY + 6, 0xFF000000);
            } else {
                guiGraphics.fill(currentTabX, tabY + 2, currentTabX + tabW, tabY + 20, 0xFFDDDDDD);
                guiGraphics.fill(currentTabX, tabY + 2, currentTabX + tabW, tabY + 3, 0xFFAAAAAA); 
                guiGraphics.fill(currentTabX, tabY + 2, currentTabX + 1, tabY + 20, 0xFFAAAAAA); 
                guiGraphics.fill(currentTabX + tabW - 1, tabY + 2, currentTabX + tabW, tabY + 20, 0xFFAAAAAA); 
                drawClearCenteredText(guiGraphics, tabNames[i], currentTabX + (tabW / 2), tabY + 7, 0xFF000000);
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

    private void drawClearText(GuiGraphics guiGraphics, String text, int x, int y, int color) {
        guiGraphics.drawString(this.font, text, x, y, color, false);
    }

    private void drawClearCenteredText(GuiGraphics guiGraphics, String text, int x, int y, int color) {
        guiGraphics.drawString(this.font, text, x - (this.font.width(text) / 2), y, color, false);
    }

    private void renderPhysicalTab(GuiGraphics guiGraphics, int mouseX, int mouseY, int winW, int winH) {
        int contentX = windowX + 15;
        int contentY = windowY + 50;

        guiGraphics.fill(contentX, contentY, contentX + 110, contentY + 265, 0xFFFFFFFF);
        guiGraphics.fill(contentX, contentY, contentX + 110, contentY + 1, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY + 264, contentX + 110, contentY + 265, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY, contentX + 1, contentY + 265, 0xFFAAAAAA);
        guiGraphics.fill(contentX + 109, contentY, contentX + 110, contentY + 265, 0xFFAAAAAA);

        drawClearText(guiGraphics, "MODULES", contentX + 5, contentY + 5, 0xFF000000);
        guiGraphics.fill(contentX + 1, contentY + 18, contentX + 109, contentY + 32, 0xFFDDDDDD); 
        drawClearText(guiGraphics, "RT-1694D-CHASSIS", contentX + 5, contentY + 21, 0xFF000000);
        
        int listY = contentY + 35;
        for (String mod : AVAILABLE_MODULES) {
            boolean isInstalled = mod.equals(installedPower) || mod.equals(installedAntenna) || mod.equals(installedSpeaker) || mod.equals(installedData);
            boolean isDragged = mod.equals(draggedModule);
            
            if (isInstalled || isDragged) {
                guiGraphics.fill(contentX + 5, listY, contentX + 105, listY + 12, 0xFFDDDDDD);
                drawClearText(guiGraphics, mod, contentX + 8, listY + 2, 0xFFAAAAAA);
            } else {
                drawClearText(guiGraphics, mod, contentX + 5, listY + 2, 0xFF000000);
            }
            listY += 15;
        }

        int viewX = contentX + 120;
        int viewY = contentY;
        int viewWidth = winW - 150;
        int viewHeight = 265;

        guiGraphics.fill(viewX, viewY, viewX + viewWidth, viewY + viewHeight, 0xFFFFFFFF);
        guiGraphics.fill(viewX, viewY, viewX + viewWidth, viewY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX, viewY + viewHeight - 1, viewX + viewWidth, viewY + viewHeight, 0xFFAAAAAA);
        guiGraphics.fill(viewX, viewY, viewX + 1, viewY + viewHeight, 0xFFAAAAAA);
        guiGraphics.fill(viewX + viewWidth - 1, viewY, viewX + viewWidth, viewY + viewHeight, 0xFFAAAAAA);

        drawClearCenteredText(guiGraphics, "Physical Device View", viewX + (viewWidth / 2), viewY + 8, 0xFF000000);

        int btnY = viewY + 25;
        // Zoom In Button
        guiGraphics.fill(viewX + 60, btnY, viewX + 140, btnY + 15, 0xFFDDDDDD);
        guiGraphics.fill(viewX + 60, btnY, viewX + 140, btnY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 60, btnY+14, viewX + 140, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 60, btnY, viewX + 61, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 139, btnY, viewX + 140, btnY + 15, 0xFFAAAAAA);
        drawClearCenteredText(guiGraphics, "Zoom In", viewX + 100, btnY + 4, 0xFF000000);
        
        // Original Size Button
        guiGraphics.fill(viewX + 165, btnY, viewX + 245, btnY + 15, 0xFFDDDDDD);
        guiGraphics.fill(viewX + 165, btnY, viewX + 245, btnY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 165, btnY+14, viewX + 245, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 165, btnY, viewX + 166, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 244, btnY, viewX + 245, btnY + 15, 0xFFAAAAAA);
        drawClearCenteredText(guiGraphics, "Original Size", viewX + 205, btnY + 4, 0xFF000000);
        
        // Zoom Out Button
        guiGraphics.fill(viewX + 270, btnY, viewX + 350, btnY + 15, 0xFFDDDDDD);
        guiGraphics.fill(viewX + 270, btnY, viewX + 350, btnY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 270, btnY+14, viewX + 350, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 270, btnY, viewX + 271, btnY + 15, 0xFFAAAAAA);
        guiGraphics.fill(viewX + 349, btnY, viewX + 350, btnY + 15, 0xFFAAAAAA);
        drawClearCenteredText(guiGraphics, "Zoom Out", viewX + 310, btnY + 4, 0xFF000000);

        int chassisX = viewX + 25;
        int chassisY = viewY + 60;
        int chassisW = viewWidth - 50;
        int chassisH = 150;

        guiGraphics.enableScissor(viewX + 1, viewY + 45, viewX + viewWidth - 1, viewY + viewHeight - 1);
        guiGraphics.pose().pushPose();
        
        float centerX = viewX + (viewWidth / 2f);
        float centerY = viewY + 130f;
        
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(zoomLevel, zoomLevel, 1.0f);
        guiGraphics.pose().translate(-centerX, -centerY, 0);

        guiGraphics.fill(chassisX, chassisY, chassisX + chassisW, chassisY + chassisH, 0xFF4A5A35);
        guiGraphics.fill(chassisX, chassisY + 75, chassisX + chassisW, chassisY + 77, 0xFF354224);

        guiGraphics.fill(chassisX + 90, chassisY + 15, chassisX + 240, chassisY + 65, 0xFF111511);
        if (isOn && installedPower != null) {
            drawClearText(guiGraphics, "HARRIS OS 2.4", chassisX + 95, chassisY + 20, 0xFF00FF00);
            
            if (frequency.equals("0.000") || frequency.equals("0.0")) {
                drawClearText(guiGraphics, "MODE: SCANNER", chassisX + 95, chassisY + 32, 0xFF00FF00);
                if (isIntercepting) {
                    drawClearText(guiGraphics, "[SWEEPING FREQS]", chassisX + 95, chassisY + 44, 0xFFD62929);
                }
            } else {
                drawClearText(guiGraphics, "FREQ: " + frequency + " MHz", chassisX + 95, chassisY + 32, 0xFF00FF00);
                drawClearText(guiGraphics, "ALGO: " + algo, chassisX + 95, chassisY + 44, 0xFF00FF00);
                if (isIntercepting) {
                    drawClearText(guiGraphics, "[MITM HACK ACTIVE]", chassisX + 95, chassisY + 56, 0xFFD62929);
                }
            }
        }

        // Left Side Connections
        drawScaledString(guiGraphics, "AUDIO", chassisX + 35, chassisY + 18, 0.7f, 0xFF000000);
        guiGraphics.fill(chassisX + 37, chassisY + 26, chassisX + 49, chassisY + 38, 0xFF000000);
        if (installedData != null) guiGraphics.fill(chassisX + 39, chassisY + 28, chassisX + 47, chassisY + 36, 0xFF4488FF);

        drawScaledString(guiGraphics, "DATA", chassisX + 37, chassisY + 45, 0.7f, 0xFF000000);
        guiGraphics.fill(chassisX + 37, chassisY + 53, chassisX + 49, chassisY + 65, 0xFF000000);
        if (installedData != null) guiGraphics.fill(chassisX + 39, chassisY + 55, chassisX + 47, chassisY + 63, 0xFF4488FF);

        // Right Side Brand & Modules
        drawScaledString(guiGraphics, "RT-1694D(P)(C)/U", chassisX + 260, chassisY + 10, 0.8f, 0xFFFFFFFF);
        drawClearText(guiGraphics, "HARRIS", chassisX + 280, chassisY + 20, 0xFFFFFFFF);

        drawScaledString(guiGraphics, "ANTENNA", chassisX + 310, chassisY + 32, 0.6f, 0xFFFFFFFF);
        guiGraphics.fill(chassisX + 312, chassisY + 38, chassisX + 332, chassisY + 48, 0xFF111111);
        if (installedAntenna != null) {
            guiGraphics.fill(chassisX + 314, chassisY + 40, chassisX + 330, chassisY + 46, 0xFF222222);
        }

        drawScaledString(guiGraphics, "ACCESSORY", chassisX + 280, chassisY + 43, 0.6f, 0xFF000000);
        guiGraphics.fill(chassisX + 287, chassisY + 49, chassisX + 299, chassisY + 61, 0xFF000000);
        guiGraphics.fill(chassisX + 289, chassisY + 51, chassisX + 297, chassisY + 59, 0xFF4488FF);

        drawScaledString(guiGraphics, "PWR", chassisX + 255, chassisY + 36, 0.6f, 0xFFFFFFFF);
        guiGraphics.fill(chassisX + 251, chassisY + 44, chassisX + 267, chassisY + 60, 0xFF000000);
        if (installedPower != null) {
            int pwrColor = isOn ? 0xFF00FF00 : 0xFF550000;
            guiGraphics.fill(chassisX + 253, chassisY + 46, chassisX + 265, chassisY + 58, pwrColor);
        } else {
            guiGraphics.fill(chassisX + 253, chassisY + 46, chassisX + 265, chassisY + 58, 0xFF111111);
        }

        // Bottom Section Speakers & Vent
        drawScaledString(guiGraphics, "SPKR", chassisX + 160, chassisY + 85, 0.6f, 0xFF000000);
        guiGraphics.fill(chassisX + 162, chassisY + 93, chassisX + 174, chassisY + 105, 0xFF000000);
        if (installedSpeaker != null) guiGraphics.fill(chassisX + 164, chassisY + 95, chassisX + 172, chassisY + 103, 0xFF4488FF);

        drawScaledString(guiGraphics, "AUX", chassisX + 195, chassisY + 85, 0.6f, 0xFF000000);
        guiGraphics.fill(chassisX + 192, chassisY + 93, chassisX + 204, chassisY + 105, 0xFF000000);
        if (installedSpeaker != null) guiGraphics.fill(chassisX + 194, chassisY + 95, chassisX + 202, chassisY + 103, 0xFF4488FF);

        drawScaledString(guiGraphics, "PA CTL", chassisX + 175, chassisY + 115, 0.6f, 0xFF000000);
        guiGraphics.fill(chassisX + 177, chassisY + 123, chassisX + 189, chassisY + 135, 0xFF000000);
        if (installedSpeaker != null) guiGraphics.fill(chassisX + 179, chassisY + 125, chassisX + 187, chassisY + 133, 0xFF4488FF);

        guiGraphics.fill(chassisX + 230, chassisY + 85, chassisX + 325, chassisY + 135, 0xFF354224);
        for (int gx = 0; gx < 11; gx++) {
            for (int gy = 0; gy < 6; gy++) {
                guiGraphics.fill(chassisX + 235 + (gx * 8), chassisY + 90 + (gy * 8), chassisX + 238 + (gx * 8), chassisY + 93 + (gy * 8), 0xFF111111);
            }
        }

        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();

        guiGraphics.fill(contentX, contentY + 275, contentX + winW - 30, contentY + 325, 0xFFFFFFFF);
        guiGraphics.fill(contentX, contentY + 275, contentX + winW - 30, contentY + 276, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY + 324, contentX + winW - 30, contentY + 325, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY + 275, contentX + 1, contentY + 325, 0xFFAAAAAA);
        guiGraphics.fill(contentX + winW - 31, contentY + 275, contentX + winW - 30, contentY + 325, 0xFFAAAAAA);

        drawClearText(guiGraphics, "The RT-1694D provides secure military-grade communications routing.", contentX + 5, contentY + 280, 0xFF000000);
        drawClearText(guiGraphics, "Tune to 0.000 MHz and activate Intercept for WIDEBAND SCAN MODE.", contentX + 5, contentY + 294, 0xFF000000);
        drawClearText(guiGraphics, "Configure cryptography and tuning via the CLI terminal tab.", contentX + 5, contentY + 308, 0xFF000000);

        // Draw the module attached to the cursor
        if (draggedModule != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 400); // Draw above everything
            guiGraphics.fill(mouseX - 50, mouseY - 6, mouseX + 50, mouseY + 6, 0xFFEEEEEE);
            guiGraphics.fill(mouseX - 50, mouseY - 6, mouseX + 50, mouseY - 5, 0xFFAAAAAA);
            guiGraphics.fill(mouseX - 50, mouseY + 5, mouseX + 50, mouseY + 6, 0xFFAAAAAA);
            guiGraphics.fill(mouseX - 50, mouseY - 6, mouseX - 49, mouseY + 6, 0xFFAAAAAA);
            guiGraphics.fill(mouseX + 49, mouseY - 6, mouseX + 50, mouseY + 6, 0xFFAAAAAA);
            drawClearCenteredText(guiGraphics, draggedModule, mouseX, mouseY - 4, 0xFF000000);
            guiGraphics.pose().popPose();
        }
    }

    private void renderConfigTab(GuiGraphics guiGraphics, int winW, int winH) {
        int contentX = windowX + 15;
        int contentY = windowY + 50;

        guiGraphics.fill(contentX, contentY, contentX + 110, contentY + 325, 0xFFFFFFFF);
        guiGraphics.fill(contentX, contentY, contentX + 110, contentY + 1, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY + 324, contentX + 110, contentY + 325, 0xFFAAAAAA);
        guiGraphics.fill(contentX, contentY, contentX + 1, contentY + 325, 0xFFAAAAAA);
        guiGraphics.fill(contentX + 109, contentY, contentX + 110, contentY + 325, 0xFFAAAAAA);

        drawClearText(guiGraphics, "GLOBAL", contentX + 5, contentY + 5, 0xFF000000);
        drawClearText(guiGraphics, "  Settings", contentX + 5, contentY + 19, 0xFF333333);
        guiGraphics.fill(contentX + 1, contentY + 18, contentX + 109, contentY + 29, 0xFFDDDDDD); 
        drawClearText(guiGraphics, "  Settings", contentX + 5, contentY + 19, 0xFF000000);
        
        drawClearText(guiGraphics, "ROUTING", contentX + 5, contentY + 43, 0xFF000000);
        drawClearText(guiGraphics, "  Static", contentX + 5, contentY + 57, 0xFFAAAAAA);
        
        drawClearText(guiGraphics, "INTERFACE", contentX + 5, contentY + 81, 0xFF000000);
        drawClearText(guiGraphics, "  FastEthernet0/0", contentX + 5, contentY + 95, 0xFF555555);
        drawClearText(guiGraphics, "  VHF Antenna", contentX + 5, contentY + 109, 0xFF555555);

        int viewX = contentX + 120;
        int viewY = contentY;
        int viewWidth = winW - 150;
        int viewHeight = 325;

        guiGraphics.fill(viewX, viewY, viewX + viewWidth, viewY + viewHeight, 0xFFFFFFFF);
        guiGraphics.fill(viewX, viewY, viewX + viewWidth, viewY + 1, 0xFFAAAAAA);
        guiGraphics.fill(viewX, viewY + viewHeight - 1, viewX + viewWidth, viewY + viewHeight, 0xFFAAAAAA);
        guiGraphics.fill(viewX, viewY, viewX + 1, viewY + viewHeight, 0xFFAAAAAA);
        guiGraphics.fill(viewX + viewWidth - 1, viewY, viewX + viewWidth, viewY + viewHeight, 0xFFAAAAAA);

        drawClearCenteredText(guiGraphics, "Device Configuration", viewX + (viewWidth / 2), viewY + 8, 0xFF000000);

        int textX = viewX + 15;
        int startY = viewY + 35;
        
        drawClearText(guiGraphics, "Power Status:", textX, startY, 0xFF000000);
        drawClearText(guiGraphics, isOn ? "ONLINE" : "OFFLINE", textX + 100, startY, isOn ? 0xFF00AA00 : 0xFFD62929);

        drawClearText(guiGraphics, "Tuned Frequency:", textX, startY + 25, 0xFF000000);
        drawClearText(guiGraphics, frequency.equals("0.000") || frequency.equals("0.0") ? "SWEEPING ALL" : frequency + " MHz", textX + 100, startY + 25, 0xFF000000);

        drawClearText(guiGraphics, "Encryption Algo:", textX, startY + 50, 0xFF000000);
        drawClearText(guiGraphics, frequency.equals("0.000") || frequency.equals("0.0") ? "N/A (SCANNING)" : algo, textX + 100, startY + 50, 0xFF000000);

        drawClearText(guiGraphics, "MITM Hack Status:", textX, startY + 75, 0xFF000000);
        String interceptStatus = isIntercepting ? (frequency.equals("0.000") || frequency.equals("0.0") ? "SWEEPING SIGNALS" : "EXPLOITING PACKETS") : "DISABLED";
        int interceptColor = isIntercepting ? 0xFFD62929 : 0xFF000000;
        drawClearText(guiGraphics, interceptStatus, textX + 100, startY + 75, interceptColor);
        
        drawClearText(guiGraphics, "Note: All configurations must be applied", textX, startY + 125, 0xFF555555);
        drawClearText(guiGraphics, "manually via the CLI interface.", textX, startY + 139, 0xFF555555);
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

        int maxLines = 29; // Increased for taller window
        int startIndex = Math.max(0, terminalLog.size() - maxLines + logScroll);
        int yOffset = cliY + 5;

        for (int i = startIndex; i < Math.min(terminalLog.size(), startIndex + maxLines); i++) {
            drawClearText(guiGraphics, terminalLog.get(i), cliX + 5, yOffset, 0xFF00FF00);
            yOffset += 10;
        }

        if (terminalLog.size() < maxLines || startIndex + maxLines >= terminalLog.size()) {
            String prefix = this.isOn ? "RT-1694D> " : "> ";
            String cursor = (System.currentTimeMillis() % 1000 > 500) ? "_" : "";
            drawClearText(guiGraphics, prefix + currentInput + cursor, cliX + 5, yOffset, 0xFF00FF00);
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
                int contentX = windowX + 15;
                int contentY = windowY + 50;
                int viewX = contentX + 120;
                int viewY = contentY;
                int viewWidth = windowWidth - 150;
                
                int btnY = viewY + 25;
                
                // Zoom In
                if (mouseX >= viewX + 60 && mouseX <= viewX + 140 && mouseY >= btnY && mouseY <= btnY + 15) {
                    zoomLevel = Math.min(3.0f, zoomLevel + 0.25f);
                    playClickSound();
                    return true;
                }
                // Original Size
                if (mouseX >= viewX + 165 && mouseX <= viewX + 245 && mouseY >= btnY && mouseY <= btnY + 15) {
                    zoomLevel = 1.0f;
                    playClickSound();
                    return true;
                }
                // Zoom Out
                if (mouseX >= viewX + 270 && mouseX <= viewX + 350 && mouseY >= btnY && mouseY <= btnY + 15) {
                    zoomLevel = Math.max(0.5f, zoomLevel - 0.25f);
                    playClickSound();
                    return true;
                }

                // Check left panel module list for Drag Start
                int listY = contentY + 35;
                for (String mod : AVAILABLE_MODULES) {
                    if (mouseX >= contentX + 5 && mouseX <= contentX + 105 && mouseY >= listY && mouseY <= listY + 12) {
                        boolean isInstalled = mod.equals(installedPower) || mod.equals(installedAntenna) || mod.equals(installedSpeaker) || mod.equals(installedData);
                        if (!isInstalled) {
                            draggedModule = mod;
                            playClickSound();
                            return true;
                        }
                    }
                    listY += 15;
                }

                // Check clicks on the physical chassis
                if (mouseX >= viewX + 1 && mouseX <= viewX + viewWidth - 1 && mouseY >= viewY + 45 && mouseY <= viewY + 265 - 1) {
                    float centerX = viewX + (viewWidth / 2f);
                    float centerY = viewY + 130f;
                    double mappedX = (mouseX - centerX) / zoomLevel + centerX;
                    double mappedY = (mouseY - centerY) / zoomLevel + centerY;
                    
                    int chassisX = viewX + 25;
                    int chassisY = viewY + 60;
                    
                    // Power Button Interaction
                    int pwrBtnX = chassisX + 251; int pwrBtnY = chassisY + 44;
                    if (mappedX >= pwrBtnX && mappedX <= pwrBtnX + 16 && mappedY >= pwrBtnY && mappedY <= pwrBtnY + 16) {
                        if (installedPower == null) {
                            terminalLog.add("ERROR: AC Power Supply missing. Cannot boot.");
                        } else {
                            this.isOn = !this.isOn;
                            syncToServer();
                            terminalLog.add(this.isOn ? "SYSTEM BOOTING... ONLINE." : "SYSTEM SHUTTING DOWN... OFFLINE.");
                        }
                        playClickSound();
                        return true;
                    }

                    // Remove modules from chassis slots
                    if (installedAntenna != null && mappedX >= chassisX+310 && mappedX <= chassisX+335 && mappedY >= chassisY+38 && mappedY <= chassisY+50) {
                        draggedModule = installedAntenna; installedAntenna = null; playClickSound(); return true;
                    }
                    if (installedPower != null && mappedX >= chassisX+250 && mappedX <= chassisX+270 && mappedY >= chassisY+42 && mappedY <= chassisY+62) {
                        draggedModule = installedPower; installedPower = null; 
                        if (this.isOn) { this.isOn = false; terminalLog.add("CRITICAL: POWER SUPPLY REMOVED. SYSTEM OFFLINE."); syncToServer(); }
                        playClickSound(); return true;
                    }
                    if (installedSpeaker != null && mappedX >= chassisX+160 && mappedX <= chassisX+210 && mappedY >= chassisY+80 && mappedY <= chassisY+135) {
                        draggedModule = installedSpeaker; installedSpeaker = null; playClickSound(); return true;
                    }
                    if (installedData != null && mappedX >= chassisX+30 && mappedX <= chassisX+55 && mappedY >= chassisY+20 && mappedY <= chassisY+70) {
                        draggedModule = installedData; installedData = null; playClickSound(); return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggedModule != null && button == 0) {
            int viewX = windowX + 15 + 120;
            int viewY = windowY + 50;
            int viewWidth = windowWidth - 150;
            
            if (mouseX >= viewX + 1 && mouseX <= viewX + viewWidth - 1 && mouseY >= viewY + 45 && mouseY <= viewY + 265 - 1) {
                float centerX = viewX + (viewWidth / 2f);
                float centerY = viewY + 130f;
                double mappedX = (mouseX - centerX) / zoomLevel + centerX;
                double mappedY = (mouseY - centerY) / zoomLevel + centerY;
                
                int chassisX = viewX + 25;
                int chassisY = viewY + 60;

                if (draggedModule.startsWith("ANTENNA") && mappedX >= chassisX+310 && mappedX <= chassisX+335 && mappedY >= chassisY+38 && mappedY <= chassisY+50) {
                    if (installedAntenna != null) terminalLog.add("ERROR: Slot occupied.");
                    else { installedAntenna = draggedModule; playClickSound(); }
                }
                else if (draggedModule.equals("PWR-SUPPLY-AC") && mappedX >= chassisX+250 && mappedX <= chassisX+270 && mappedY >= chassisY+42 && mappedY <= chassisY+62) {
                    if (installedPower != null) terminalLog.add("ERROR: Slot occupied.");
                    else { installedPower = draggedModule; playClickSound(); }
                }
                else if (draggedModule.equals("SPKR-EXTERNAL") && mappedX >= chassisX+160 && mappedX <= chassisX+210 && mappedY >= chassisY+80 && mappedY <= chassisY+135) {
                    if (installedSpeaker != null) terminalLog.add("ERROR: Slot occupied.");
                    else { installedSpeaker = draggedModule; playClickSound(); }
                }
                else if (draggedModule.equals("DATA-LINK-CBL") && mappedX >= chassisX+30 && mappedX <= chassisX+55 && mappedY >= chassisY+20 && mappedY <= chassisY+70) {
                    if (installedData != null) terminalLog.add("ERROR: Slot occupied.");
                    else { installedData = draggedModule; playClickSound(); }
                }
            }
            draggedModule = null; // Always reset when dropped
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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