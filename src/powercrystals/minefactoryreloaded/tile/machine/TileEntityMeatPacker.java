package powercrystals.minefactoryreloaded.tile.machine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;

import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryPowered;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryPowered;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

public class TileEntityMeatPacker extends TileEntityFactoryPowered implements IFluidHandler
{
	public TileEntityMeatPacker()
	{
		super(Machine.MeatPacker);
		setManageSolids(true);
	}
	
	@Override
	public int getSizeInventory()
	{
		return 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer inventoryPlayer)
	{
		return new GuiFactoryPowered(getContainer(inventoryPlayer), this);
	}
	
	@Override
	public ContainerFactoryPowered getContainer(InventoryPlayer inventoryPlayer)
	{
		return new ContainerFactoryPowered(this, inventoryPlayer);
	}
	
	@Override
	protected boolean activateMachine()
	{
		if(drain(_tanks[0], 2, false) == 2)
		{
			setWorkDone(getWorkDone() + 1);
			
			if(getWorkDone() >= getWorkMax())
			{
				ItemStack item;
				if(_tanks[0].getFluid().equals(FluidRegistry.getFluidStack("meat", 1)))
				{
					item = new ItemStack(MineFactoryReloadedCore.meatIngotRawItem);
				}
				else
				{
					item = new ItemStack(MineFactoryReloadedCore.meatNuggetRawItem);
				}
				
				doDrop(item);
				
				setWorkDone(0);
			}
			drain(_tanks[0], 2, true);
			return true;
		}
		return false;
	}
	
	@Override
	public int getWorkMax()
	{
		return 50;
	}
	
	@Override
	public int getIdleTicksMax()
	{
		return 0;
	}
	
	@Override
	public boolean allowBucketFill()
	{
		return true;
	}
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(resource == null || !(resource.isFluidEqual(FluidRegistry.getFluidStack("meat", 1)) ||
				resource.isFluidEqual(FluidRegistry.getFluidStack("pinkslime", 1))))
		{
			return 0;
		}
		else
		{
			if(drain(_tanks[0], 2, false) == 1)
			{
				drain(_tanks[0], 1, true);
			}
			return _tanks[0].fill(resource, doFill);
		}
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
