package dev.yuanyu.enderscapeexpansion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ShelfBlockEntity extends BlockEntity implements Container {
   private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "enderscape");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShelfBlockEntity>> TYPE = TYPES.register(
      "storage_shelf",
      () -> Builder.of(
            ShelfBlockEntity::new,
            new Block[]{
               (Block)ExpansionBlocks.CELESTIAL_SHELF.get(), (Block)ExpansionBlocks.VEILED_SHELF.get(), (Block)ExpansionBlocks.MURUBLIGHT_STORAGE_SHELF.get()
            }
         )
         .build(null)
   );
   private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

   public ShelfBlockEntity(BlockPos p, BlockState s) {
      super((BlockEntityType)TYPE.get(), p, s);
   }

   public static void register(IEventBus b) {
      TYPES.register(b);
   }

   public int getContainerSize() {
      return 3;
   }

   public boolean isEmpty() {
      return this.items.stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getItem(int slot) {
      return (ItemStack)this.items.get(slot);
   }

   public ItemStack removeItem(int slot, int amount) {
      ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
      this.setChanged();
      return stack;
   }

   public ItemStack removeItemNoUpdate(int slot) {
      return ContainerHelper.takeItem(this.items, slot);
   }

   public void setItem(int slot, ItemStack stack) {
      this.items.set(slot, stack);
      this.setChanged();
   }

   public ItemStack swap(int slot, ItemStack stack) {
      return (ItemStack)this.items.set(slot, stack);
   }

   public boolean stillValid(Player p) {
      return Container.stillValidBlockEntity(this, p);
   }

   public void clearContent() {
      this.items.clear();
      this.setChanged();
   }

   public void setChanged() {
      super.setChanged();
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
      }
   }

   protected void saveAdditional(CompoundTag n, Provider r) {
      super.saveAdditional(n, r);
      ContainerHelper.saveAllItems(n, this.items, r);
   }

   protected void loadAdditional(CompoundTag n, Provider r) {
      super.loadAdditional(n, r);
      this.items.clear();
      ContainerHelper.loadAllItems(n, this.items, r);
   }

   public CompoundTag getUpdateTag(Provider r) {
      return this.saveWithoutMetadata(r);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
