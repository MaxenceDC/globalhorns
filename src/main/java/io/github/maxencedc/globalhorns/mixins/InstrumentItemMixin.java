package io.github.maxencedc.globalhorns.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(InstrumentItem.class)
public class InstrumentItemMixin {
    @WrapMethod(method = "play")
    private static void playSound(Level level, Player player, Instrument instrument, Operation<Void> original) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer receiver : serverLevel.getServer().getPlayerList().getPlayers()) {
            if (receiver.level().dimension() != level.dimension()) continue;

            double distance = receiver.getEyePosition().distanceTo(player.getEyePosition());
            Vec3 difference = player.getEyePosition().subtract(receiver.getEyePosition());

            if (distance > 220) difference = difference.scale(220 / distance);
            Vec3 end = receiver.getEyePosition().add(difference);

            receiver.connection.send(new ClientboundSoundPacket(instrument.soundEvent(), SoundSource.RECORDS, end.x(), end.y(), end.z(), instrument.range() / 16.0F, 1, receiver.getRandom().nextLong()));
        }
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.getEyePosition(), GameEvent.Context.of(player));
    }
}