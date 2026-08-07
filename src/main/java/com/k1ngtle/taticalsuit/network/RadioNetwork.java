package com.k1ngtle.taticalsuit.network;

import com.k1ngtle.taticalsuit.TaticalSuit;
import com.k1ngtle.taticalsuit.block.RadioStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class RadioNetwork {
    private static final String PROTOCOL_VERSION = "3";

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
                
        CHANNEL.messageBuilder(UpdateStationPacket.class, 2, NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateStationPacket::new)
                .encoder(UpdateStationPacket::toBytes)
                .consumerMainThread(UpdateStationPacket::handle)
                .add();
    }

    public static class UpdateStationPacket {
        public final BlockPos pos;
        public final boolean isOn;
        public final String freq;
        public final String algo;
        public final String key;
        public final boolean isIntercepting;

        public UpdateStationPacket(BlockPos pos, boolean isOn, String freq, String algo, String key, boolean isIntercepting) {
            this.pos = pos;
            this.isOn = isOn;
            this.freq = freq;
            this.algo = algo;
            this.key = key;
            this.isIntercepting = isIntercepting;
        }

        public UpdateStationPacket(FriendlyByteBuf buf) {
            this.pos = buf.readBlockPos();
            this.isOn = buf.readBoolean();
            this.freq = buf.readUtf();
            this.algo = buf.readUtf();
            this.key = buf.readUtf();
            this.isIntercepting = buf.readBoolean();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeBlockPos(this.pos);
            buf.writeBoolean(this.isOn);
            buf.writeUtf(this.freq);
            buf.writeUtf(this.algo);
            buf.writeUtf(this.key);
            buf.writeBoolean(this.isIntercepting);
        }

        public boolean handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    BlockEntity be = player.level().getBlockEntity(pos);
                    if (be instanceof RadioStationBlockEntity station) {
                        station.setStationData(isOn, freq, algo, key, isIntercepting);
                    }
                }
            });
            context.setPacketHandled(true);
            return true;
        }
    }

    public static class VoicePacket {
        public final byte[] audioData;
        public final String frequency;
        public final String algo;
        public final String key;
        public final boolean isScrambled; 
        public final BlockPos sourcePos;
        public final String bandwidth; 
        public final double distance; 

        public VoicePacket(byte[] audioData, String frequency, String algo, String key, boolean isScrambled, BlockPos sourcePos, String bandwidth, double distance) {
            this.audioData = audioData;
            this.frequency = frequency;
            this.algo = algo;
            this.key = key;
            this.isScrambled = isScrambled;
            this.sourcePos = sourcePos;
            this.bandwidth = bandwidth;
            this.distance = distance;
        }

        public VoicePacket(FriendlyByteBuf buf) {
            this.audioData = buf.readByteArray();
            this.frequency = buf.readUtf();
            this.algo = buf.readUtf();
            this.key = buf.readUtf();
            this.isScrambled = buf.readBoolean();
            if (buf.readBoolean()) {
                this.sourcePos = buf.readBlockPos();
            } else {
                this.sourcePos = null;
            }
            this.bandwidth = buf.readUtf();
            this.distance = buf.readDouble();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeByteArray(this.audioData);
            buf.writeUtf(this.frequency);
            buf.writeUtf(this.algo);
            buf.writeUtf(this.key);
            buf.writeBoolean(this.isScrambled);
            buf.writeBoolean(this.sourcePos != null);
            if (this.sourcePos != null) {
                buf.writeBlockPos(this.sourcePos);
            }
            buf.writeUtf(this.bandwidth);
            buf.writeDouble(this.distance);
        }

        public boolean handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                if (context.getDirection().getReceptionSide().isServer()) {
                    ServerPlayer sender = context.getSender();
                    if (sender == null) return;
                    
                    for (ServerPlayer receiver : sender.server.getPlayerList().getPlayers()) {
                        
                        double dist = Double.MAX_VALUE;
                        if (sender.level() == receiver.level()) {
                            dist = sender.position().distanceTo(receiver.position());
                        }

                        boolean hasInEar = false;
                        String inEarAlgo = "CLEAR";
                        String inEarKey = "";
                        
                        for (int i = 0; i < receiver.getInventory().getContainerSize(); i++) {
                            ItemStack stack = receiver.getInventory().getItem(i);
                            if (stack.getItem() instanceof com.k1ngtle.taticalsuit.item.RadioItem && stack.hasTag()) {
                                int ch = stack.getTag().contains("channel") ? stack.getTag().getInt("channel") : 0;
                                String f = stack.getTag().contains("ch" + ch + "_freq") ? stack.getTag().getString("ch" + ch + "_freq") : 
                                          (stack.getTag().contains("frequency") ? stack.getTag().getString("frequency") : "446.000");
                                
                                if (frequency.equals(f)) {
                                    hasInEar = true;
                                    inEarAlgo = stack.getTag().contains("ch" + ch + "_algo") ? stack.getTag().getString("ch" + ch + "_algo") : "CLEAR";
                                    inEarKey = stack.getTag().contains("ch" + ch + "_key") ? stack.getTag().getString("ch" + ch + "_key") : "";
                                    break;
                                }
                            }
                        }

                        boolean hasStation = false;
                        String stationAlgo = "CLEAR";
                        String stationKey = "";
                        boolean isIntercepting = false;
                        BlockPos stationPos = null;
                        
                        for (RadioStationBlockEntity station : RadioStationBlockEntity.ACTIVE_STATIONS) {
                            if (station.isOn() && station.getLevel() == receiver.level()) {
                                if (station.getBlockPos().distToCenterSqr(receiver.position()) <= 400) {
                                    
                                    if (station.isIntercepting() && (station.getFrequency().equals("0.000") || station.getFrequency().equals("0.0"))) {
                                        double detectChance = 0.1; 
                                        if (bandwidth.equals("25.0k")) detectChance = 0.4;
                                        if (bandwidth.equals("50.0k")) detectChance = 1.0; 
                                        
                                        if (Math.random() <= detectChance && receiver.tickCount % 30 == 0) {
                                            receiver.displayClientMessage(Component.literal("§e[RT-1694D SCANNER] Signal Found: " + frequency + " MHz | Enc: " + algo + " | BW: " + bandwidth), false);
                                        }
                                    } 
                                    else if (station.getFrequency().equals(frequency)) {
                                        hasStation = true;
                                        stationPos = station.getBlockPos();
                                        stationAlgo = station.getAlgo();
                                        stationKey = station.getKey();
                                        isIntercepting = station.isIntercepting();
                                        break;
                                    }
                                }
                            }
                        }
                        
                        boolean isSender = receiver.getId() == sender.getId();
                        
                        if (hasInEar && !isSender) {
                            boolean encMatch = algo.equals(inEarAlgo) && key.equals(inEarKey);
                            RadioNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> receiver), 
                                new VoicePacket(audioData, frequency, algo, key, !encMatch, null, bandwidth, dist));
                        }
                        
                        if (hasStation) {
                            boolean encMatch = algo.equals(stationAlgo) && key.equals(stationKey);
                            boolean scrambled = !encMatch && !isIntercepting;
                            double statDist = Math.sqrt(stationPos.distToCenterSqr(receiver.position()));
                            
                            RadioNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> receiver), 
                                new VoicePacket(audioData, frequency, algo, key, scrambled, stationPos, bandwidth, statDist));
                        }
                    }
                    
                } else {
                    net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        if (mc.player != null) {
                            
                            double MAX_RANGE = 2000.0; 
                            double noiseLevel = distance / MAX_RANGE;
                            
                            if (noiseLevel >= 0.8 && !com.k1ngtle.taticalsuit.client.audio.VoiceManager.isSquelchOverrideHeld) {
                                return; 
                            }

                            byte[] decrypted = com.k1ngtle.taticalsuit.client.audio.VoiceManager.processCrypto(audioData, audioData.length, algo, key, 2); 

                            if (noiseLevel > 0.05) {
                                double staticIntensity = Math.min(0.95, noiseLevel); 
                                for (int j = 0; j < decrypted.length; j += 2) {
                                    short sample = (short) ((decrypted[j + 1] << 8) | (decrypted[j] & 0xFF));
                                    short staticNoise = (short) ((Math.random() - 0.5) * 32767); 
                                    
                                    short finalSample = (short) ((sample * (1.0 - staticIntensity)) + (staticNoise * staticIntensity));
                                    
                                    decrypted[j] = (byte) (finalSample & 0xFF);
                                    decrypted[j + 1] = (byte) ((finalSample >> 8) & 0xFF);
                                }
                            }

                            if (bandwidth != null) {
                                if (bandwidth.equals("12.5k")) {
                                    short lastSample = 0;
                                    for (int j = 0; j < decrypted.length; j += 2) {
                                        short sample = (short) ((decrypted[j + 1] << 8) | (decrypted[j] & 0xFF));
                                        sample = (short) ((sample + lastSample) / 2);
                                        lastSample = sample;
                                        decrypted[j] = (byte) (sample & 0xFF);
                                        decrypted[j + 1] = (byte) ((sample >> 8) & 0xFF);
                                    }
                                } else if (bandwidth.equals("50.0k")) {
                                    for (int j = 0; j < decrypted.length; j += 2) {
                                        short sample = (short) ((decrypted[j + 1] << 8) | (decrypted[j] & 0xFF));
                                        sample = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sample * 1.25));
                                        decrypted[j] = (byte) (sample & 0xFF);
                                        decrypted[j + 1] = (byte) ((sample >> 8) & 0xFF);
                                    }
                                }
                            }

                            if (isScrambled) {
                                if (mc.player.tickCount % 30 == 0) {
                                    mc.player.displayClientMessage(Component.literal("§e[RX] UNSTABLE ENCRYPTED SIGNAL (" + frequency + " MHz)"), true);
                                }
                                for (int j = 0; j < decrypted.length; j += 2) {
                                    short sample = (short) ((decrypted[j + 1] << 8) | (decrypted[j] & 0xFF));
                                    double phase = (j / 2.0) / 480.0; 
                                    double modulator = Math.sin(phase * Math.PI * 2.0);
                                    short robotSample = (short) (sample * Math.abs(modulator)); 
                                    short noise = (short) ((Math.random() - 0.5) * 3500); 
                                    short finalSample = (short) (robotSample + noise);
                                    decrypted[j] = (byte) (finalSample & 0xFF);
                                    decrypted[j + 1] = (byte) ((finalSample >> 8) & 0xFF);
                                }
                            }
                            com.k1ngtle.taticalsuit.client.audio.VoiceManager.playAudio(decrypted, sourcePos);
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
        public final String[] bws;
        public final float volume;
        public final boolean isMainHand;
        
        public SyncRadioFrequencyPacket(int channel, String[] freqs, String[] algos, String[] keys, String[] bws, float volume, boolean isMainHand) {
            this.channel = channel; this.freqs = freqs; this.algos = algos; this.keys = keys; this.bws = bws; this.volume = volume; this.isMainHand = isMainHand;
        }
        
        public SyncRadioFrequencyPacket(FriendlyByteBuf buf) {
            this.channel = buf.readInt();
            this.freqs = new String[5]; this.algos = new String[5]; this.keys = new String[5]; this.bws = new String[5];
            for (int i = 0; i < 5; i++) this.freqs[i] = buf.readUtf();
            for (int i = 0; i < 5; i++) this.algos[i] = buf.readUtf();
            for (int i = 0; i < 5; i++) this.keys[i] = buf.readUtf();
            for (int i = 0; i < 5; i++) this.bws[i] = buf.readUtf();
            this.volume = buf.readFloat(); this.isMainHand = buf.readBoolean();
        }
        
        public void toBytes(FriendlyByteBuf buf) {
            buf.writeInt(this.channel);
            for (int i = 0; i < 5; i++) buf.writeUtf(this.freqs[i]);
            for (int i = 0; i < 5; i++) buf.writeUtf(this.algos[i]);
            for (int i = 0; i < 5; i++) buf.writeUtf(this.keys[i]);
            for (int i = 0; i < 5; i++) buf.writeUtf(this.bws[i]);
            buf.writeFloat(this.volume); buf.writeBoolean(this.isMainHand);
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
                            stack.getOrCreateTag().putString("ch" + i + "_bw", bws[i]);
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