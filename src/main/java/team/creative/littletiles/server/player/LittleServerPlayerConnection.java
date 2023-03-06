package team.creative.littletiles.server.player;

import java.util.HashMap;
import java.util.function.Consumer;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.common.level.little.LittleLevel;

public class LittleServerPlayerConnection {
    
    private LittleServerPlayerConnection() {}
    
    static {
        MinecraftForge.EVENT_BUS.register(LittleServerPlayerConnection.class);
    }
    
    private static final HashMap<ServerPlayer, LittleServerPlayerHandler> LISTENERS = new HashMap<>();
    
    public static void remove(ServerPlayer player) {
        LISTENERS.remove(player);
    }
    
    private static LittleServerPlayerHandler getOrCreate(ServerPlayer player) {
        LittleServerPlayerHandler listener = LISTENERS.get(player);
        if (listener == null)
            LISTENERS.put(player, listener = new LittleServerPlayerHandler(player));
        return listener;
    }
    
    public static void send(LittleLevel level, ServerPlayer player, Packet packet) {
        LittleServerPlayerHandler listener = getOrCreate(player);
        synchronized (listener) {
            Level previous = listener.level;
            listener.level = level.asLevel();
            listener.send(packet);
            listener.level = previous;
        }
    }
    
    public static void runInContext(LittleLevel level, ServerPlayer player, Consumer<LittleServerPlayerHandler> consumer) {
        LittleServerPlayerHandler listener = getOrCreate(player);
        synchronized (listener) {
            Level previous = listener.level;
            listener.level = level.asLevel();
            consumer.accept(listener);
            listener.level = previous;
        }
    }
    
    @SubscribeEvent
    public static void tick(ServerTickEvent event) {
        for (LittleServerPlayerHandler handler : LISTENERS.values())
            handler.tick();
    }
    
    @SubscribeEvent
    public static void playerLoggedOut(PlayerLoggedOutEvent event) {
        LISTENERS.remove(event.getEntity());
    }
    
}
