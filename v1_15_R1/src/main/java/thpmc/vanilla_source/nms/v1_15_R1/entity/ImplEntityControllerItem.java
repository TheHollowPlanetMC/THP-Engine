package thpmc.vanilla_source.nms.v1_15_R1.entity;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.nms.entity.NMSItemEntityController;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.collision.EngineBoundingBox;
import thpmc.vanilla_source.api.util.collision.EngineEntityBoundingBox;
import thpmc.vanilla_source.api.util.math.Vec2f;

public class ImplEntityControllerItem extends EntityItem implements NMSItemEntityController {
    
    public ImplEntityControllerItem(World world, double d0, double d1, double d2, net.minecraft.server.v1_15_R1.ItemStack itemstack) {
        super(world, d0, d1, d2, itemstack);
    }
    
    @Override
    public void setRotation(float yaw, float pitch) {
        //None
    }
    
    @Override
    public Vector getPosition() {
        return new Vector(locX(), locY(), locZ());
    }
    
    @Override
    public Vec2f getYawPitch() {
        return new Vec2f(yaw, pitch);
    }
    
    @Override
    public EngineEntityBoundingBox getEngineBoundingBox(EngineEntity entity) {
        AxisAlignedBB aabb = super.getBoundingBox();
        return new EngineEntityBoundingBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, entity);
    }
    
    @Override
    public void resetBoundingBoxForMovement(EngineBoundingBox boundingBox) {
        super.a(new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()));
    }
    
    
    private boolean isMetadataChanged = false;
    
    @Override
    public void playTickResult(EngineEntity engineEntity, EnginePlayer player, boolean absolute) {
        if (absolute) {
            player.sendPacket(new PacketPlayOutEntityTeleport(this));
        } else {
            Vector moveDelta = engineEntity.getMoveDelta();
            player.sendPacket(new PacketPlayOutEntityVelocity(this.getId(), new Vec3D(moveDelta.getX(), moveDelta.getY(), moveDelta.getZ())));
        }
        
        if (isMetadataChanged) {
            DataWatcher dataWatcher = super.getDataWatcher();
            player.sendPacket(new PacketPlayOutEntityMetadata(this.getId(), dataWatcher, true));
            isMetadataChanged = false;
        }
    }
    
    @Override
    public void show(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutSpawnEntity(this));
        DataWatcher dataWatcher = super.getDataWatcher();
        player.sendPacket(new PacketPlayOutEntityMetadata(this.getId(), dataWatcher, true));
    }
    
    @Override
    public void hide(EngineEntity engineEntity, EnginePlayer player) {
        player.sendPacket(new PacketPlayOutEntityDestroy(this.getId()));
    }
    
    @Override
    public void setItemStack(ItemStack itemStack) {
        super.setItemStack(CraftItemStack.asNMSCopy(itemStack));
        isMetadataChanged = true;
    }
    
}
