package powercrystals.minefactoryreloaded.tile.machine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import powercrystals.core.util.UtilInventory;
import powercrystals.minefactoryreloaded.core.ITankContainerBucketable;
import powercrystals.minefactoryreloaded.gui.client.GuiBlockSmasher;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.container.ContainerBlockSmasher;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;
import powercrystals.minefactoryreloaded.world.SmashingWorld;

public class TileEntityBlockSmasher extends TileEntityFactoryPowered implements ITankContainerBucketable
{
	private int _fortune = 0;
	
	private ItemStack _lastInput;
	private List<ItemStack> _lastOutput;
	
	private SmashingWorld _smashingWorld;
	private boolean _shouldWork = true;
	
	public TileEntityBlockSmasher()
	{
		super(Machine.BlockSmasher);
		setManageSolids(true);
	}
	
	@Override
	protected float getMinMJ()
	{
		return 10f;
	}
	
	@Override
	public void setWorldObj(World world)
	{
		super.setWorldObj(world);
		_smashingWorld = new SmashingWorld(this.worldObj);
	}
	
	@Override
	public int getSizeInventory()
	{
		return 2;
	}
	
	@Override
	public ContainerBlockSmasher getContainer(InventoryPlayer inventoryPlayer)
	{
		return new ContainerBlockSmasher(this, inventoryPlayer);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer inventoryPlayer)
	{
		return new GuiBlockSmasher(getContainer(inventoryPlayer), this);
	}
	
	@Override
	protected boolean activateMachine()
	{
		if(_shouldWork && _inventory[0] == null)
		{
			setWorkDone(0);
			return false;
		}
		if(_inventory[0] != null && (_lastInput == null || !UtilInventory.stacksEqual(_lastInput, _inventory[0])))
		{
			_lastInput = _inventory[0].copy(); // protect against amorphous itemstacks
			_lastOutput = getOutput(_lastInput);
		}
		if(_lastOutput == null)
		{
			setWorkDone(0);
			return false;
		}
		if(_shouldWork & _fortune > 0 && (drain(_tanks[0], _fortune, false) != _fortune))
		{
			return false;
		}
		ItemStack outSlot = _inventory[1];
		ItemStack output = getEqualStack(outSlot, _lastOutput);
		// TODO: ^ inefficient
		if(output == null)
		{
			if (_shouldWork)
				setWorkDone(0);
			return false;
		}
		if(outSlot != null && outSlot.getMaxStackSize() - outSlot.stackSize < output.stackSize)
		{
			return false;
		}
		
		if(getWorkDone() >= getWorkMax())
		{
			if (_shouldWork)
			{
				_inventory[0].stackSize--;
				if(_inventory[0].stackSize == 0)
				{
					_inventory[0] = null;
				}
			}
			_shouldWork = false;
			if(_inventory[1] == null)
			{
				_inventory[1] = output.copy();
			}
			else
			{
				_inventory[1].stackSize += output.stackSize;
			}
			_lastOutput.remove(output);
			if (_lastOutput.size() == 0)
			{
				setWorkDone(0);
				_shouldWork = true;
				_lastInput = null;
				_lastOutput = null;
			}
		}
		else
		{
			setWorkDone(getWorkDone() + 1);
			drain(_tanks[0], _fortune, true);
		}
		return true;
	}
	
	private static ItemStack getEqualStack(ItemStack a, List<ItemStack> b)
	{
		if (a != null & b != null && a.stackSize > 0 && b.size() > 0)
			for (ItemStack i : b)
				if (UtilInventory.stacksEqual(a, i)) return i;
		return a == null && b.size() > 0 ? b.get(0) : null;
	}
	
	@SuppressWarnings("unchecked")
	private List<ItemStack> getOutput(ItemStack input)
	{
		if(!(input.getItem() instanceof ItemBlock))
		{
			return null;
		}
		int blockId = ((ItemBlock)input.getItem()).getBlockID();
		Block b = Block.blocksList[blockId];
		if(b == null)
		{
			return null;
		}
		
		@SuppressWarnings("rawtypes")
		ArrayList drops = _smashingWorld.smashBlock(input, b, blockId, input.getItemDamage(), _fortune);
		if (drops != null && drops.size() > 0)
		{
			return drops;
		}
		return null;
	}
	
	public int getFortune()
	{
		return _fortune;
	}
	
	public void setFortune(int fortune)
	{
		if(fortune >= 0 && fortune <= 3)
		{
			if(_fortune < fortune)
			{
				setWorkDone(0);
			}
			_fortune = fortune;
		}
	}
	
	@Override
	public int getWorkMax()
	{
		return 60;
	}
	
	@Override
	public int getIdleTicksMax()
	{
		return 1;
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int sideordinal)
	{
		if(slot == 0) return true;
		return false;
	}
	
	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int sideordinal)
	{
		if(slot == 1) return true;
		return false;
	}
	
	@Override
	public boolean allowBucketFill()
	{
		return true;
	}
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(resource == null || !resource.isFluidEqual(FluidRegistry.getFluidStack("mobessence", 1)))
		{
			return 0;
		}
		
		return _tanks[0].fill(resource, doFill);
	}
	
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}
	
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}
	
	@Override
	protected FluidTank[] createTanks()
	{
		return new FluidTank[]{new FluidTank(4 * FluidContainerRegistry.BUCKET_VOLUME)};
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setInteger("fortune", _fortune);
		tag.setBoolean("shouldWork", _shouldWork);
		if (_lastInput != null)
			tag.setTag("stack", _lastInput.writeToNBT(new NBTTagCompound()));
		
		if (_lastOutput != null)
		{
			NBTTagList nbttaglist = new NBTTagList();
			for (ItemStack item : _lastOutput)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				item.writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
			tag.setTag("SmashedItems", nbttaglist);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		_fortune = tag.getInteger("fortune");
		_shouldWork = tag.hasKey("shouldWork") ? tag.getBoolean("shouldWork") : true;
		if (tag.hasKey("stack"))
			_lastInput = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("stack"));

		if (tag.hasKey("SmashedItems"))
		{
			List<ItemStack> drops = new ArrayList<ItemStack>();
			NBTTagList nbttaglist = tag.getTagList("SmashedItems");
			for (int i = nbttaglist.tagCount(); i --> 0; )
			{
				NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
				ItemStack item = ItemStack.loadItemStackFromNBT(nbttagcompound1);
				if (item != null && item.stackSize > 0)
				{
					drops.add(item);
				}
			}
			if (drops.size() != 0)
			{
				_lastOutput = drops;
			}
		}
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}
}
