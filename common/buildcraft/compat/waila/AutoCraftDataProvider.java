package buildcraft.compat.waila;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import buildcraft.compat.CompatUtils;
import buildcraft.factory.util.IAutoCraft;

import static buildcraft.compat.waila.HWYLAPlugin.WAILA_MOD_ID;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.SpecialChars;

class AutoCraftDataProvider extends BaseWailaDataProvider {
    @Nonnull
    @Override
    @Optional.Method(modid = WAILA_MOD_ID)
    public List<String> getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity tile = accessor.getTileEntity();
        if (tile instanceof IAutoCraft) {
            NBTTagCompound nbt = accessor.getNBTData();
            if (nbt.hasKey("recipe_result", Constants.NBT.TAG_COMPOUND)) {
                ItemStack result = new ItemStack(nbt.getCompoundTag("recipe_result"));
                currentTip.add(TextFormatting.WHITE + "Making: " + SpecialChars.WailaSplitter + HWYLAPlugin.getItemStackString(result));

                if (nbt.hasKey("recipe_inputs", Constants.NBT.TAG_LIST)) {
                    NBTTagList list = nbt.getTagList("recipe_inputs", Constants.NBT.TAG_COMPOUND);
                    StringBuilder inputs = new StringBuilder(TextFormatting.WHITE + "From: " + SpecialChars.WailaSplitter);
                    for (int index = 0; index < list.tagCount(); index++) {
                        NBTTagCompound compound = NBTTagCompound.class.cast(list.get(index));
                        inputs.append(HWYLAPlugin.getItemStackString(new ItemStack(compound)));
                    }
                    currentTip.add(inputs.toString());
                }
            } else {
                currentTip.add(TextFormatting.GRAY + "No recipe");
            }
        } else {
            currentTip.add(TextFormatting.RED + "{wrong tile entity}");
        }
        return currentTip;
    }

    @Nonnull
    @Override
    @Optional.Method(modid = WAILA_MOD_ID)
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        NBTTagCompound nbt = super.getNBTData(player, te, tag, world, pos);

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IAutoCraft) {
            IAutoCraft auto = IAutoCraft.class.cast(tile);
            IRecipe recipe = auto.getCurrentRecipe();
            if (recipe != null) {
                nbt.setTag("recipe_result", recipe.getRecipeOutput().serializeNBT());

                List<ItemStack> stacks = CompatUtils.compactInventory(auto.getInvBlueprint());
                NBTTagList list = new NBTTagList();
                for (int index = 0; index < stacks.size(); index++) {
                    list.appendTag(stacks.get(index).serializeNBT());
                }
                nbt.setTag("recipe_inputs", list);
            }
        }

        return nbt;
    }
}
