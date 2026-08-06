package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.TaticalSuit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class RadioNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TaticalSuit.MODID, "radio_channel"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        CHANNEL.messageBuilder(SyncRadioFrequencyPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .decoder(SyncRadioFrequencyPacket::new)
                .encoder(SyncRadioFrequencyPacket::toBytes)
                .consumerMainThread(SyncRadioFrequencyPacket::handle)
                .add();

        CHANNEL.messageBuilder(VoicePacket.class, 1)
                .decoder(VoicePacket::new)
                .encoder(VoicePacket::toBytes)
                .consumerMainThread(VoicePacket::handle)
                .add();
    }

    public static class VoicePacket {
        public final byte[] audioData;
        public final String frequency;
        public final String algo;
        public final String key;
        public final boolean isScrambled; 

        // Server inbound constructor
        public VoicePacket(byte[] audioData, String frequency, String algo, String key) {
            this.audioData = audioData;
            this.frequency = frequency;
            this.algo = algo;
            this.key = key;
            this.isScrambled = false; 
        }

        // Client inbound constructor
        public VoicePacket(byte[] audioData, String frequency, String algo, String key, boolean isScrambled) {
            this.audioData = audioData;
            this.frequency = frequency;
            this.algo = algo;
            this.key = key;
            this.isScrambled = isScrambled;
        }

        public VoicePacket(FriendlyByteBuf buf) {
            this.audioData = buf.readByteArray();
            this.frequency = buf.readUtf();
            this.algo = buf.readUtf();
            this.key = buf.readUtf();
            this.isScrambled = buf.readBoolean();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeByteArray(this.audioData);
            buf.writeUtf(this.frequency);
            buf.writeUtf(this.algo);
            buf.writeUtf(this.key);
            buf.writeBoolean(this.isScrambled);
        }

        public boolean handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                if (context.getDirection().getReceptionSide().isServer()) {
                    ServerPlayer sender = context.getSender();
                    if (sender == null) return;
                    
                    int routedPlayers = 0;
                    
                    for (ServerPlayer receiver : sender.server.getPlayerList().getPlayers()) {
                        if (receiver.getId() == sender.getId()) continue; 
                        
                        boolean freqMatch = false;
                        String receiverAlgo = "CLEAR";
                        String receiverKey = "";
                        
                        for (int i = 0; i < receiver.getInventory().getContainerSize(); i++) {
                            ItemStack stack = receiver.getInventory().getItem(i);
                            if (stack.getItem() instanceof com.k1ngtle.taticalsuit.item.RadioItem && stack.hasTag()) {
                                int ch = stack.getTag().contains("channel") ? stack.getTag().getInt("channel") : 0;
                                String f = stack.getTag().contains("ch" + ch + "_freq") ? stack.getTag().getString("ch" + ch + "_freq") : 
                                          (stack.getTag().contains("frequency") ? stack.getTag().getString("frequency") : "145.0");
                                
                                if (frequency.equals(f)) {
                                    freqMatch = true;
                                    receiverAlgo = stack.getTag().contains("ch" + ch + "_algo") ? stack.getTag().getString("ch" + ch + "_algo") : "CLEAR";
                                    receiverKey = stack.getTag().contains("ch" + ch + "_key") ? stack.getTag().getString("ch" + ch + "_key") : "";
                                    break;
                                }
                            }
                        }
                        
                        if (freqMatch) {
                            // If algorithms and keys match exactly, it's clear. Otherwise, strictly scramble it!
                            boolean encMatch = algo.equals(receiverAlgo) && key.equals(receiverKey);
                            
                            // Send to client
                            RadioNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> receiver), new VoicePacket(audioData, frequency, algo, key, !encMatch));
                            routedPlayers++;
                        }
                    }
                    
                } else {
                    net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        if (mc.player != null) {
                            if (isScrambled) {
                                mc.player.displayClientMessage(Component.literal("§c[RX] KEY MISMATCH - DECRYPTION FAILED ON " + frequency + " MHz"), true);
                                byte[] staticData = new byte[audioData.length];
                                for (int j = 0; j < staticData.length; j += 2) {
                                    short noise = (short) ((Math.random() - 0.5) * 1500); 
                                    staticData[j] = (byte) (noise & 0xFF);
                                    staticData[j + 1] = (byte) ((noise >> 8) & 0xFF);
                                }
                                com.k1ngtle.taticalsuit.client.audio.VoiceManager.playAudio(staticData);
                            } else {
                                mc.player.displayClientMessage(Component.literal("§a[RX] Receiving " + algo + " audio on " + frequency + " MHz"), true);
                                byte[] decrypted = com.k1ngtle.taticalsuit.client.audio.VoiceManager.processCrypto(audioData, audioData.length, algo, key, 2); // 2 = DECRYPT_MODE
                                com.k1ngtle.taticalsuit.client.audio.VoiceManager.playAudio(decrypted);
                            }
                        }
                    });
                }
            });
            context.setPacketHandled(true);
            return true;
        }
    }

    public static class SyncRadioFrequencyPacket {
        public final int channel;
        public final String[] freqs;
        public final String[] algos;
        public final String[] keys;
        public final float volume;
        public final boolean isMainHand;

        public SyncRadioFrequencyPacket(int channel, String[] freqs, String[] algos, String[] keys, float volume, boolean isMainHand) {
            this.channel = channel;
            this.freqs = freqs;
            this.algos = algos;
            this.keys = keys;
            this.volume = volume;
            this.isMainHand = isMainHand;
        }

        public SyncRadioFrequencyPacket(FriendlyByteBuf buf) {
            this.channel = buf.readInt();
            this.freqs = new String[5];
            this.algos = new String[5];
            this.keys = new String[5];
            for (int i = 0; i < 5; i++) this.freqs[i] = buf.readUtf();
            for (int i = 0; i < 5; i++) this.algos[i] = buf.readUtf();
            for (int i = 0; i < 5; i++) this.keys[i] = buf.readUtf();
            this.volume = buf.readFloat();
            this.isMainHand = buf.readBoolean();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeInt(this.channel);
            for (int i = 0; i < 5; i++) buf.writeUtf(this.freqs[i]);
            for (int i = 0; i < 5; i++) buf.writeUtf(this.algos[i]);
            for (int i = 0; i < 5; i++) buf.writeUtf(this.keys[i]);
            buf.writeFloat(this.volume);
            buf.writeBoolean(this.isMainHand);
        }

        public boolean handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    InteractionHand hand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                    ItemStack stack = player.getItemInHand(hand);
                    if (stack.getItem() instanceof com.k1ngtle.taticalsuit.item.RadioItem) {
                        stack.getOrCreateTag().putInt("channel", channel);
                        for (int i = 0; i < 5; i++) {
                            stack.getOrCreateTag().putString("ch" + i + "_freq", freqs[i]);
                            stack.getOrCreateTag().putString("ch" + i + "_algo", algos[i]);
                            stack.getOrCreateTag().putString("ch" + i + "_key", keys[i]);
                        }
                        stack.getOrCreateTag().putFloat("volume", volume);
                    }
                }
            });
            context.setPacketHandled(true);
            return true;
        }
    }
}