package thpmc.vanilla_source.nms.v1_15_R1;

import thpmc.vanilla_source.api.world.parallel.ParallelChunk;
import thpmc.vanilla_source.api.world.parallel.ParallelUniverse;
import thpmc.vanilla_source.api.world.parallel.ParallelWorld;
import thpmc.vanilla_source.api.nms.IPacketHandler;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.util.SectionLevelArray;
import net.minecraft.server.v1_15_R1.NibbleArray;
import net.minecraft.server.v1_15_R1.PacketPlayOutLightUpdate;
import org.bukkit.World;

import java.lang.reflect.Field;
import java.util.*;

public class LightUpdatePacketHandler implements IPacketHandler {
    
    private static Field a;
    private static Field b;
    private static Field c;
    private static Field d;
    private static Field e;
    private static Field f;
    private static Field g;
    private static Field h;
    
    static {
        try {
            a = PacketPlayOutLightUpdate.class.getDeclaredField("a");
            b = PacketPlayOutLightUpdate.class.getDeclaredField("b");
            c = PacketPlayOutLightUpdate.class.getDeclaredField("c");
            d = PacketPlayOutLightUpdate.class.getDeclaredField("d");
            e = PacketPlayOutLightUpdate.class.getDeclaredField("e");
            f = PacketPlayOutLightUpdate.class.getDeclaredField("f");
            g = PacketPlayOutLightUpdate.class.getDeclaredField("g");
            h = PacketPlayOutLightUpdate.class.getDeclaredField("h");
            
            a.setAccessible(true);
            b.setAccessible(true);
            c.setAccessible(true);
            d.setAccessible(true);
            e.setAccessible(true);
            f.setAccessible(true);
            g.setAccessible(true);
            h.setAccessible(true);
        }catch (Exception e){e.printStackTrace();}
    }
    
    @Override
    public Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting) {
        
        ParallelUniverse universe = EnginePlayer.getUniverse();
        if(universe == null) return packet;
    
        World world = EnginePlayer.getBukkitPlayer().getWorld();
        String worldName = world.getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);
    
        try{
            
            int chunkX = a.getInt(packet);
            int chunkZ = b.getInt(packet);
    
            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkX, chunkZ);
            if (parallelChunk == null) return packet;
            if (!parallelChunk.hasBlockLightLevelDifferenceData() && !parallelChunk.hasSkyLightLevelDifferenceData()) {
                return packet;
            }
            
            Object cachedPacket = parallelChunk.getCachedLightUpdatePacket();
            if (cachedPacket != null) return cachedPacket;
            
            int cValue = c.getInt(packet);
            int dValue = d.getInt(packet);
            int eValue = e.getInt(packet);
            int fValue = f.getInt(packet);
            Deque<byte[]> gValue = new ArrayDeque<>((List<byte[]>) g.get(packet));
            Deque<byte[]> hValue = new ArrayDeque<>((List<byte[]>) h.get(packet));
            
            int newC = 0;
            int newD = 0;
            int newE = 0;
            int newF = 0;
            List<byte[]> newG = new ArrayList<>();
            List<byte[]> newH = new ArrayList<>();
            
            boolean edited = false;
    
            for(int index = 0; index < 18; index++){
                int sectionIndex = index - 1;
    
                int cSectionBit = cValue & (1 << index);
                newC |= cSectionBit;
                newE |= eValue & (1 << index);
    
                int dSectionBit = dValue & (1 << index);
                newD |= dSectionBit;
                newF |= fValue & (1 << index);
                
                if(index == 0 || index == 17){
                    
                    if(cSectionBit != 0){
                        newG.add(gValue.removeFirst());
                    }
                    
                    if(dSectionBit != 0){
                        newH.add(hValue.removeFirst());
                    }
                    continue;
                }
    
    
                SectionLevelArray skyLevelArray = parallelChunk.getSkyLightSectionLevelArray(sectionIndex);
                SectionLevelArray blockLevelArray = parallelChunk.getBlockLightSectionLevelArray(sectionIndex);
                
                if(skyLevelArray == null){
                    if(cSectionBit != 0){
                        newG.add(gValue.removeFirst());
                    }
                }else {
                    if(cSectionBit == 0){
                        NibbleArray nibbleArray = new NibbleArray();
                        
                        boolean notEmpty = skyLevelArray.threadsafeIteration(nibbleArray::a);
                        if(notEmpty) edited = true;
                        
                        newG.add(nibbleArray.asBytes());
                        
                        if(notEmpty) {
                            newC |= 1 << index;
                            newE &= ~(1 << index);
                        }else{
                            newE |= 1 << index;
                        }
                    }else{
                        NibbleArray nibbleArray = new NibbleArray(gValue.removeFirst().clone());
    
                        boolean notEmpty = skyLevelArray.threadsafeIteration(nibbleArray::a);
                        if(notEmpty) edited = true;
                        
                        newG.add(nibbleArray.asBytes());
    
                        if(notEmpty) {
                            newE &= ~(1 << index);
                        }
                    }
                }
    
                if(blockLevelArray == null){
                    if(dSectionBit != 0){
                        newH.add(hValue.removeFirst());
                    }
                }else {
                    if(dSectionBit == 0){
                        NibbleArray nibbleArray = new NibbleArray();
            
                        boolean notEmpty = blockLevelArray.threadsafeIteration(nibbleArray::a);
                        if(notEmpty) edited = true;
            
                        newH.add(nibbleArray.asBytes());
            
                        if(notEmpty) {
                            newD |= 1 << index;
                            newF &= ~(1 << index);
                        }else{
                            newF |= 1 << index;
                        }
                    }else{
                        NibbleArray nibbleArray = new NibbleArray(hValue.removeFirst().clone());
            
                        boolean notEmpty = blockLevelArray.threadsafeIteration(nibbleArray::a);
                        if(notEmpty) edited = true;
            
                        newH.add(nibbleArray.asBytes());
            
                        if(notEmpty) {
                            newF &= ~(1 << index);
                        }
                    }
                }
            }
            
            if(!edited) return packet;
            
            
            PacketPlayOutLightUpdate newPacket = new PacketPlayOutLightUpdate();
            a.set(newPacket, chunkX);
            b.set(newPacket, chunkZ);
            c.set(newPacket, newC);
            d.set(newPacket, newD);
            e.set(newPacket, newE);
            f.set(newPacket, newF);
            g.set(newPacket, newG);
            h.set(newPacket, newH);
            
            if(cacheSetting) parallelChunk.setLightUpdatePacketCache(newPacket);
            
            return newPacket;
            
        }catch (Exception e){e.printStackTrace();}
        
        return packet;
    }
}
