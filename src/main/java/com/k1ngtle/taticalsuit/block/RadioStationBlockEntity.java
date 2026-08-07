package com.k1ngtle.taticalsuit.block;

import com.k1ngtle.taticalsuit.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RadioStationBlockEntity extends BlockEntity {

    // Server-side tracking of all loaded, active base stations for audio routing
    public static final Set<RadioStationBlockEntity> ACTIVE_STATIONS = ConcurrentHashMap.newKeySet();

    private boolean isOn = false;
    private String frequency = "145.0";
    private String algo = "CLEAR";
    private String key = "";
    private boolean isIntercepting = false;

    public RadioStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIO_STATION_BE.get(), pos, state);
    }

    public boolean isOn() { return isOn; }
    public String getFrequency() { return frequency; }
    public String getAlgo() { return algo; }
    public String getKey() { return key; }
    public boolean isIntercepting() { return isIntercepting; }

    public void setStationData(boolean isOn, String frequency, String algo, String key, boolean isIntercepting) {
        this.isOn = isOn;
        this.frequency = frequency;
        this.algo = algo;
        this.key = key;
        this.isIntercepting = isIntercepting;
        this.setChanged();
        
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            if (this.isOn) ACTIVE_STATIONS.add(this);
            else ACTIVE_STATIONS.remove(this);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide && this.isOn) {
            ACTIVE_STATIONS.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null && !this.level.isClientSide) {
            ACTIVE_STATIONS.remove(this);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsOn", isOn);
        tag.putString("Frequency", frequency);
        tag.putString("Algo", algo);
        tag.putString("Key", key);
        tag.putBoolean("IsIntercepting", isIntercepting);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.isOn = tag.getBoolean("IsOn");
        this.frequency = tag.getString("Frequency");
        this.algo = tag.getString("Algo");
        this.key = tag.getString("Key");
        this.isIntercepting = tag.getBoolean("IsIntercepting");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}