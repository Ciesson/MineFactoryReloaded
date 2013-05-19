package powercrystals.minefactoryreloaded.tile.rednet;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import cpw.mods.fml.common.network.PacketDispatcher;

import powercrystals.core.net.PacketWrapper;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.net.Packets;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactory;

public class TileEntityRedNetHistorian extends TileEntityFactory
{
	private class HistorianData
	{
		int value;
		long worldTime;
		
		public HistorianData(int value, long worldTime)
		{
			this.value = value;
			this.worldTime = worldTime;
		}
	}
	
	private Queue<Integer> _valuesClient = new ArrayBlockingQueue<Integer>(100);
	private int _currentValueClient = 0;
	private int _currentSubnet = 0;
	
	private int[] _lastValues = new int[16];
	private Map<Integer, Queue<HistorianData>> _data = new HashMap<Integer, Queue<HistorianData>>();
	
	public TileEntityRedNetHistorian()
	{
		for(int i = 0; i < 16; i++)
		{
			_data.put(i, new ArrayBlockingQueue<HistorianData>(100));
		}
		
		for(int i = 0; i < 100; i++)
		{
			_valuesClient.add(0);
		}
	}
	
	@Override
	public void validate()
	{
		for(int i = 0; i < 16; i++)
		{
			if(_data.get(i).isEmpty())
			{
				_data.get(i).add(new HistorianData(0, worldObj.getTotalWorldTime()));
			}
		}
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if(worldObj.isRemote)
		{
			_valuesClient.poll();
			_valuesClient.add(_currentValueClient);
		}
	}
	
	public void valuesChanged(int[] values)
	{
		for(int i = 0; i < 16; i++)
		{
			if(values[i] != _lastValues[i])
			{
				//_data.get(i).add(new HistorianData(values[i], worldObj.getWorldTime()));
				_lastValues[i] = values[i];
				if(i == _currentSubnet)
				{
					PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, PacketWrapper.createPacket(
							MineFactoryReloadedCore.modNetworkChannel, Packets.HistorianValueChanged, new Object[] { xCoord, yCoord, zCoord, values[i] }));
				}
			}
		}
	}
	
	public Integer[] getValues()
	{
		Integer[] values = new Integer[_valuesClient.size()];
		return _valuesClient.toArray(values);
	}
	
	public int getSelectedSubnet()
	{
		return _currentSubnet;
	}
	
	public void setClientValue(int value)
	{
		_currentValueClient = value;
	}
	
	@Override
	public boolean canRotate()
	{
		return true;
	}
}