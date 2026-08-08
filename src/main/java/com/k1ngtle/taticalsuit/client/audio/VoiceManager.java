package com.k1ngtle.taticalsuit.client.audio;

import com.k1ngtle.taticalsuit.item.RadioItem;
import com.k1ngtle.taticalsuit.network.RadioNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.system.MemoryUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.lang.reflect.Method;

public class VoiceManager {
    
    // OpenAL and RNNoise demand exactly 48kHz and 480 samples (10ms) per frame
    public static final int SAMPLE_RATE = 48000;
    public static final int FRAME_SIZE = 480; 
    public static final int AL_FORMAT_MONO16 = 4353; // Hardcoded to prevent AL10 thread context crashes
    
    private static long captureDevice = MemoryUtil.NULL;
    private static SourceDataLine speakerLine = null;
    
    private static boolean isTransmitting = false;
    private static volatile boolean isRunning = false;
    public static boolean loopbackDebug = false;
    
    // Voice Activation (VAD) variables
    public static boolean useVoiceActivation = false;
    public static int voiceActivationThreshold = 12;
    private static int voiceHoldFrames = 0;
    
    public static boolean isSquelchOverrideHeld = false; // NEW: Squelch Tracker
    
    private static float currentVolume = 1.0f;

    public static String currentMicName = "Default System Device";
    public static String currentSpeakerName = "Default System Device";

    private static Thread captureThread;
    private static Thread playbackThread;
    
    public static class AudioTask {
        public final byte[] data;
        public final net.minecraft.core.BlockPos pos;
        public AudioTask(byte[] data, net.minecraft.core.BlockPos pos) {
            this.data = data;
            this.pos = pos;
        }
    }
    private static final ConcurrentLinkedQueue<AudioTask> audioQueue = new ConcurrentLinkedQueue<>();
    
    // RNNoise Reflection Objects
    private static Object rnnoiseInstance = null;
    private static Method rnnoiseProcessMethod = null;
    private static boolean isRNNoiseLoaded = false;

    public static void init() {
        try {
            Class<?> rnnoiseClass;
            try {
                rnnoiseClass = Class.forName("de.maxhenkel.rnnoise4j.RNNoise");
            } catch (ClassNotFoundException e) {
                rnnoiseClass = Class.forName("io.github.jaredmdobson.rnnoise4j.RNNoise");
            }
            
            rnnoiseInstance = rnnoiseClass.getDeclaredConstructor().newInstance();
            rnnoiseProcessMethod = rnnoiseClass.getMethod("process", float[].class);
            isRNNoiseLoaded = true;
            System.out.println("[TacticalSuit] SUCCESS: AI RNNoise Engine Loaded!");
        } catch (Exception e) {
            System.out.println("[TacticalSuit] RNNoise library not found. Falling back to native Java Noise Gate.");
            isRNNoiseLoaded = false;
        }

        restartAudioEngine(currentMicName, currentSpeakerName);
    }

    public static void restartAudioEngine(String targetMic, String targetSpeaker) {
        System.out.println("[TacticalSuit] Binding Hybrid Audio Engine (OpenAL Capture / Async Playback)...");
        
        isRunning = false;
        audioQueue.clear();
        
        // Wait for old threads to die cleanly
        try { if (captureThread != null) captureThread.join(500); } catch (Exception ignored) {}
        try { if (playbackThread != null) playbackThread.join(500); } catch (Exception ignored) {}
        
        // Close previous OpenAL hardware handles
        if (captureDevice != MemoryUtil.NULL) {
            ALC11.alcCaptureStop(captureDevice);
            ALC11.alcCaptureCloseDevice(captureDevice);
            captureDevice = MemoryUtil.NULL;
        }
        
        // Close previous Java Speaker handles
        if (speakerLine != null) {
            speakerLine.stop();
            speakerLine.close();
            speakerLine = null;
        }

        currentMicName = targetMic;
        currentSpeakerName = targetSpeaker;
        
        String alMicName = targetMic.equals("Default System Device") ? null : targetMic;
        
        try {
            // Init OpenAL strictly for the Microphone (Bypasses AL10 context constraints)
            captureDevice = ALC11.alcCaptureOpenDevice(alMicName, SAMPLE_RATE, AL_FORMAT_MONO16, SAMPLE_RATE / 2);
            if (captureDevice != MemoryUtil.NULL) {
                ALC11.alcCaptureStart(captureDevice);
            } else {
                System.err.println("[TacticalSuit] CRITICAL: Failed to open OpenAL Capture Device!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Init Java Sound Line strictly for the Speaker (Bypasses AL10 Thread crashes entirely)
            // UPGRADED TO STEREO (2 CHANNELS) FOR 3D SPATIAL AUDIO!
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            speakerLine = (SourceDataLine) AudioSystem.getLine(info);
            speakerLine.open(format);
            speakerLine.start();
        } catch (Exception e) {
            System.err.println("[TacticalSuit] CRITICAL: Failed to initialize speaker playback line!");
            e.printStackTrace();
        }

        isRunning = true;
        startCaptureThread();
        startPlaybackThread();
    }

    private static void startCaptureThread() {
        captureThread = new Thread(() -> {
            int[] samplesReady = new int[1];
            ShortBuffer captureBuffer = BufferUtils.createShortBuffer(FRAME_SIZE);
            int tickCount = 0;
            
            while (isRunning) {
                if (captureDevice == MemoryUtil.NULL) {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    continue;
                }

                ALC11.alcGetIntegerv(captureDevice, ALC11.ALC_CAPTURE_SAMPLES, samplesReady);
                
                if (samplesReady[0] >= FRAME_SIZE) {
                    captureBuffer.clear();
                    ALC11.alcCaptureSamples(captureDevice, captureBuffer, FRAME_SIZE);
                    
                    // Constantly process audio if VAD is on, otherwise only when PTT is held
                    if (useVoiceActivation || isTransmitting) {
                        short[] pcmData = new short[FRAME_SIZE];
                        captureBuffer.get(pcmData);
                        
                        pcmData = processNoiseReduction(pcmData);
                        
                        long sum = 0;
                        for (short sample : pcmData) sum += sample * sample;
                        double rms = Math.sqrt(sum / (double)FRAME_SIZE);
                        final int currentVol = (int) rms;

                        boolean shouldTransmit = false;
                        
                        if (useVoiceActivation) {
                            if (currentVol > voiceActivationThreshold) {
                                voiceHoldFrames = 50; // Hold mic open for ~500ms after you stop speaking
                                shouldTransmit = true;
                            } else if (voiceHoldFrames > 0) {
                                voiceHoldFrames--;
                                shouldTransmit = true;
                            }
                        } else {
                            shouldTransmit = isTransmitting;
                        }

                        if (shouldTransmit && currentVol > 5) { // Small baseline filter
                            if (tickCount++ % 15 == 0) {
                                Minecraft mc = Minecraft.getInstance();
                                mc.execute(() -> {
                                    if (mc.player != null) {
                                        String freq = getActiveFrequency();
                                        String algo = getActiveEncryptionAlgo();
                                        mc.player.displayClientMessage(
                                            Component.literal("§c[TX] " + freq + " MHz (" + algo + ") §a(ACTIVE) §7| Vol: " + currentVol), true
                                        );
                                    }
                                });
                            }

                            byte[] byteData = new byte[FRAME_SIZE * 2];
                            for (int i = 0; i < FRAME_SIZE; i++) {
                                byteData[i * 2] = (byte) (pcmData[i] & 0xFF);
                                byteData[(i * 2) + 1] = (byte) ((pcmData[i] >> 8) & 0xFF);
                            }

                            String freq = getActiveFrequency();
                            String algo = getActiveEncryptionAlgo();
                            String key = getActiveEncryptionKey();
                            String bw = getActiveBandwidth();
                            
                            byte[] encryptedData = processCrypto(byteData, byteData.length, algo, key, Cipher.ENCRYPT_MODE);

                            if (loopbackDebug) {
                                byte[] lbBuffer = processCrypto(encryptedData, encryptedData.length, algo, key, Cipher.DECRYPT_MODE);
                                playAudio(lbBuffer, null);
                            } else {
                                // FIXED: Added 0.0 at the end for the "distance" parameter!
                                RadioNetwork.CHANNEL.sendToServer(new RadioNetwork.VoicePacket(encryptedData, freq, algo, key, false, null, bw, 0.0));
                            }
                        }
                    } else {
                        tickCount = 0;
                        voiceHoldFrames = 0;
                    }
                } else {
                    try { Thread.sleep(5); } catch (InterruptedException ignored) {}
                }
            }
        });
        captureThread.setDaemon(true);
        captureThread.setName("TacticalRadio-Capture");
        captureThread.start();
    }

    private static void startPlaybackThread() {
        playbackThread = new Thread(() -> {
            byte[] stereoSilence = new byte[FRAME_SIZE * 4]; // 2 bytes per sample * 2 channels = 4 bytes
            int silenceFramesWritten = 0;

            while (isRunning) {
                AudioTask task = audioQueue.poll();
                if (task != null && speakerLine != null && speakerLine.isOpen()) {
                    byte[] data = task.data;
                    net.minecraft.core.BlockPos pos = task.pos;
                    float vol = currentVolume;
                    
                    float leftVol = 1.0f;
                    float rightVol = 1.0f;

                    // 3D SPATIAL MATH
                    if (pos != null) {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player != null) {
                            net.minecraft.world.phys.Vec3 playerPos = mc.player.position();
                            net.minecraft.world.phys.Vec3 sourceVec = new net.minecraft.world.phys.Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                            
                            double distance = playerPos.distanceTo(sourceVec);
                            float distVol = (float) Math.max(0.0, 1.0 - (distance / 20.0)); // 20 Block Falloff
                            
                            net.minecraft.world.phys.Vec3 lookVec = mc.player.getLookAngle();
                            net.minecraft.world.phys.Vec3 dirToSource = sourceVec.subtract(playerPos).normalize();
                            
                            // Cross product with UP vector to get player's Right vector
                            net.minecraft.world.phys.Vec3 rightVec = lookVec.cross(new net.minecraft.world.phys.Vec3(0, 1, 0)).normalize();
                            double pan = dirToSource.dot(rightVec); // -1.0 (left) to 1.0 (right)
                            
                            leftVol = distVol * (float)Math.min(1.0, 1.0 - pan + 0.2);
                            rightVol = distVol * (float)Math.min(1.0, 1.0 + pan + 0.2);
                        }
                    }

                    // Convert 16-bit Mono to 16-bit Stereo
                    byte[] stereoData = new byte[data.length * 2];
                    for (int i = 0; i < data.length; i += 2) {
                        short sample = (short) ((data[i + 1] << 8) | (data[i] & 0xFF));
                        
                        short lSample = (short) (sample * vol * leftVol);
                        short rSample = (short) (sample * vol * rightVol);
                        
                        // Left channel
                        stereoData[i * 2] = (byte) (lSample & 0xFF);
                        stereoData[i * 2 + 1] = (byte) ((lSample >> 8) & 0xFF);
                        // Right channel
                        stereoData[i * 2 + 2] = (byte) (rSample & 0xFF);
                        stereoData[i * 2 + 3] = (byte) ((rSample >> 8) & 0xFF);
                    }
                    
                    speakerLine.write(stereoData, 0, stereoData.length);
                    silenceFramesWritten = 0; // Reset the silence tail
                } else if (speakerLine != null && speakerLine.isOpen()) {
                    // FIX: Java Sound Driver Buffer Underrun Loop Bug
                    if (silenceFramesWritten < 10) {
                        speakerLine.write(stereoSilence, 0, stereoSilence.length);
                        silenceFramesWritten++;
                    } else {
                        try { Thread.sleep(5); } catch (InterruptedException ignored) {}
                    }
                } else {
                    try { Thread.sleep(5); } catch (InterruptedException ignored) {}
                }
            }
        });
        playbackThread.setDaemon(true);
        playbackThread.setName("TacticalRadio-Playback");
        playbackThread.start();
    }

    private static short[] processNoiseReduction(short[] pcm) {
        if (isRNNoiseLoaded && rnnoiseInstance != null) {
            try {
                float[] floatPcm = new float[FRAME_SIZE];
                for (int i = 0; i < FRAME_SIZE; i++) floatPcm[i] = pcm[i] / 32768.0f;
                floatPcm = (float[]) rnnoiseProcessMethod.invoke(rnnoiseInstance, (Object) floatPcm);
                for (int i = 0; i < FRAME_SIZE; i++) pcm[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, floatPcm[i] * 32768.0f));
                return pcm;
            } catch (Exception e) {}
        } 
        
        long energy = 0;
        for (short sample : pcm) energy += Math.abs(sample);
        if ((energy / FRAME_SIZE) < 50) Arrays.fill(pcm, (short) 0);
        return pcm;
    }

    public static void updateState() {
        // Called safely from the Main Thread to update shared variables
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof RadioItem && stack.hasTag() && stack.getTag().contains("volume")) {
                currentVolume = stack.getTag().getFloat("volume");
                return;
            }
        }
        currentVolume = 1.0f;
    }

    public static void playAudio(byte[] data, net.minecraft.core.BlockPos pos) {
        if (isRunning) {
            // FIX: Queue Cap to prevent massive echo/delay buildup during lag spikes
            if (audioQueue.size() > 15) {
                audioQueue.clear(); 
            }
            audioQueue.add(new AudioTask(data, pos));
        }
    }

    public static List<String> getAvailableInputs() {
        List<String> devices = new ArrayList<>();
        devices.add("Default System Device");
        List<String> alDevices = ALUtil.getStringList(MemoryUtil.NULL, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        if (alDevices != null) devices.addAll(alDevices);
        return devices;
    }

    public static List<String> getAvailableOutputs() {
        List<String> devices = new ArrayList<>();
        devices.add("Default System Device");
        List<String> alDevices = ALUtil.getStringList(MemoryUtil.NULL, ALC11.ALC_ALL_DEVICES_SPECIFIER);
        if (alDevices != null) devices.addAll(alDevices);
        return devices;
    }

    public static void setTransmitting(boolean transmitting) {
        isTransmitting = transmitting;
    }

    public static byte[] processCrypto(byte[] data, int length, String algo, String keyStr, int mode) {
        if (algo.equals("CLEAR") || keyStr.isEmpty()) {
            if (data.length == length) return data;
            byte[] exact = new byte[length];
            System.arraycopy(data, 0, exact, 0, length);
            return exact;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyStr.getBytes("UTF-8"));
            
            Cipher cipher;
            if (algo.equals("DES")) {
                byte[] keyBytes = Arrays.copyOf(hash, 8);
                SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "DES");
                IvParameterSpec iv = new IvParameterSpec(new byte[8]); 
                cipher = Cipher.getInstance("DES/CFB8/NoPadding");
                cipher.init(mode, secretKey, iv);
            } else if (algo.equals("BLOWFISH")) {
                byte[] keyBytes = Arrays.copyOf(hash, 16);
                SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "Blowfish");
                IvParameterSpec iv = new IvParameterSpec(new byte[8]); 
                cipher = Cipher.getInstance("Blowfish/CFB8/NoPadding");
                cipher.init(mode, secretKey, iv);
            } else if (algo.equals("CHACHA20")) {
                byte[] keyBytes = Arrays.copyOf(hash, 32); 
                SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "ChaCha20");
                try {
                    java.security.spec.AlgorithmParameterSpec spec = (java.security.spec.AlgorithmParameterSpec) 
                        Class.forName("javax.crypto.spec.ChaCha20ParameterSpec").getConstructor(byte[].class, int.class).newInstance(new byte[12], 1);
                    cipher = Cipher.getInstance("ChaCha20");
                    cipher.init(mode, secretKey, spec);
                } catch (Exception ex) {
                    byte[] fbKey = Arrays.copyOf(hash, 16);
                    SecretKeySpec fbSecretKey = new SecretKeySpec(fbKey, "AES");
                    IvParameterSpec fbIv = new IvParameterSpec(new byte[16]); 
                    cipher = Cipher.getInstance("AES/CTR/NoPadding");
                    cipher.init(mode, fbSecretKey, fbIv);
                }
            } else if (algo.equals("TWOFISH")) {
                byte[] keyBytes = Arrays.copyOf(hash, 16);
                SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "Twofish");
                IvParameterSpec iv = new IvParameterSpec(new byte[16]); 
                try {
                    cipher = Cipher.getInstance("Twofish/CTR/NoPadding");
                    cipher.init(mode, secretKey, iv);
                } catch (Exception e) {
                    byte[] fbKey = Arrays.copyOf(hash, 16);
                    fbKey[0] ^= 0xFF; 
                    SecretKeySpec fbSecretKey = new SecretKeySpec(fbKey, "AES");
                    cipher = Cipher.getInstance("AES/CTR/NoPadding");
                    cipher.init(mode, fbSecretKey, iv);
                }
            } else { 
                byte[] keyBytes = Arrays.copyOf(hash, 16);
                SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
                IvParameterSpec iv = new IvParameterSpec(new byte[16]); 
                cipher = Cipher.getInstance("AES/CTR/NoPadding");
                cipher.init(mode, secretKey, iv);
            }
            return cipher.doFinal(data, 0, length);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[length];
        }
    }

    private static String getActiveFrequency() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return null;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof RadioItem && stack.hasTag()) {
                int ch = stack.getTag().contains("channel") ? stack.getTag().getInt("channel") : 0;
                if (stack.getTag().contains("ch" + ch + "_freq")) return stack.getTag().getString("ch" + ch + "_freq");
                if (stack.getTag().contains("frequency")) return stack.getTag().getString("frequency"); 
                return "145.0";
            }
        }
        return null;
    }
    
    private static String getActiveEncryptionAlgo() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return "CLEAR";
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof RadioItem && stack.hasTag()) {
                int ch = stack.getTag().contains("channel") ? stack.getTag().getInt("channel") : 0;
                if (stack.getTag().contains("ch" + ch + "_algo")) return stack.getTag().getString("ch" + ch + "_algo");
                return "CLEAR";
            }
        }
        return "CLEAR";
    }

    private static String getActiveEncryptionKey() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return "";
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof RadioItem && stack.hasTag()) {
                int ch = stack.getTag().contains("channel") ? stack.getTag().getInt("channel") : 0;
                if (stack.getTag().contains("ch" + ch + "_key")) return stack.getTag().getString("ch" + ch + "_key");
                return "";
            }
        }
        return "";
    }
    
    private static String getActiveBandwidth() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return "25.0k";
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof RadioItem && stack.hasTag()) {
                int ch = stack.getTag().contains("channel") ? stack.getTag().getInt("channel") : 0;
                if (stack.getTag().contains("ch" + ch + "_bw")) return stack.getTag().getString("ch" + ch + "_bw");
                return "25.0k";
            }
        }
        return "25.0k";
    }
}