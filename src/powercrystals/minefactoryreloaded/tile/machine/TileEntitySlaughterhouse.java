package powercrystals.minefactoryreloaded.tile.machine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.liquids.LiquidDictionary;

import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryPowered;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryPowered;
import powercrystals.minefactoryreloaded.setup.Machine;

public class TileEntitySlaughterhouse extends TileEntityGrinder
{
	public TileEntitySlaughterhouse()
	{
		super(Machine.Slaughterhouse);
	}

	@Override
	public String getInvName()
	{
		return "Slaughterhouse";
	}

	@Override
	public String getGuiBackground()
	{
		return "slaughterhouse.png";
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
	public boolean activateMachine()
	{
		grindingWorld.cleanReferences();
		List<?> entities = worldObj.getEntitiesWithinAABB(EntityLiving.class, _areaManager.getHarvestArea().toAxisAlignedBB());

		for (Object o : entities)
		{
			EntityLiving e = (EntityLiving)o;
			if (e instanceof EntityPlayer ||
					(e instanceof EntityAgeable && ((EntityAgeable)e).getGrowingAge() < 0) ||
					e.getHealth() <= 0 ||
					!grindingWorld.addEntityForGrinding(e))
			{
				continue;
			}
			double massFound = Math.pow(e.boundingBox.getAverageEdgeLength(), 2);
			damageEntity(e);
			if (e.getHealth() <= 0)
			{
				_tank.fill(LiquidDictionary.getLiquid(
						_rand.nextInt(8) == 0 ? "pinkslime" : "meat",
								(int)(100 * massFound)), true);
			}
			setIdleTicks(10);
			return true;
		}

		setIdleTicks(getIdleTicksMax());
		return false;
	}

	@Override
	protected void damageEntity(EntityLiving entity)
	{
		setRecentlyHit(entity, 0);
		entity.attackEntityFrom(new GrindingDamage("mfr.slaughterhouse"), Integer.MAX_VALUE);
	}

	@Override
	public boolean manageSolids()
	{
		return false;
	}
}
