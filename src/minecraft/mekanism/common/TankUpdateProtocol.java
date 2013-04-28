package mekanism.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.common.SynchronizedTankData.ValveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class TankUpdateProtocol 
{
	/** The dynamic tank nodes that have already been iterated over. */
	public Set<TileEntityDynamicTank> iteratedNodes = new HashSet<TileEntityDynamicTank>();
	
	/** The structures found, all connected by some nodes to the pointer. */
	public List<SynchronizedTankData> structuresFound = new ArrayList<SynchronizedTankData>();
	
	/** The original block the calculation is getting run from. */
	public TileEntity pointer;
	
	/** If the pointer is not a part of any actual dynamic tank. */
	public boolean pointerNotPartOf;
	
	public TankUpdateProtocol(TileEntity tileEntity)
	{
		pointer = tileEntity;
	}
	
	/**
	 * Recursively loops through each node connected to the given TileEntity.
	 * @param tile - the TileEntity to loop over
	 */
	public void loopThrough(TileEntity tile)
	{
    	World worldObj = tile.worldObj;
    	
		int origX = tile.xCoord, origY = tile.yCoord, origZ = tile.zCoord;
		
		boolean isCorner = true;
		boolean isHollowPrism = true;
		 
		Set<Object3D> locations = new HashSet<Object3D>();
		 
		int xmin = 0, xmax = 0, ymin = 0, ymax = 0, zmin = 0, zmax = 0;
		 
		int x = 0, y = 0, z = 0;
		 
		int volume = 0;
		 
		if((isViableNode(origX + 1, origY, origZ) && isViableNode(origX - 1, origY, origZ)) ||
				(isViableNode(origX, origY + 1, origZ) && isViableNode(origX, origY - 1, origZ)) ||
				(isViableNode(origX, origY, origZ + 1) && isViableNode(origX, origY, origZ - 1)))
		{
		    isCorner = false;
		}
		
		if(isCorner)
		{
		    if(isViableNode(origX+1, origY, origZ))
		    {
		        xmin = 0;
		        
		        while(isViableNode(origX+x+1, origY, origZ))
        		{
		            x++;
		        }
		        
		        xmax = x;
		    }
		    else {
		        xmax = 0;
		        
		        while(isViableNode(origX+x-1, origY, origZ))
		        {
		            x--;
		        }
		        
		        xmin = x;
		    }
		   
		    if(isViableNode(origX, origY+1, origZ))
		    {
		        ymin = 0;
		        
		        while(isViableNode(origX, origY+y+1, origZ))
		        {
		            y++;
		        }
		        
		        ymax = y;
		    } 
		    else {
		        ymax = 0;
		        
		        while(isViableNode(origX, origY+y-1 ,origZ))
		        {
		            y--;
		        }
		        
		        ymin = y;
		    }
		   
		    if(isViableNode(origX, origY, origZ+1))
		    {
		        zmin = 0;
		        
		        while(isViableNode(origX, origY, origZ+z+1))
		        {
		            z++;
		        }
		        
		        zmax = z;
		    } 
		    else {
		        zmax = 0;
		        
		        while(isViableNode(origX, origY, origZ+z-1))
		        {
		            z--;
		        }
		        
		        zmin = z;
		    }
		   
		    for(x = xmin; x <= xmax; x++)
		    {
		        for(y = ymin; y <= ymax; y++)
		        {
		            for(z = zmin; z <= zmax; z++)
		            {
		                if(x == xmin || x == xmax || y == ymin || y == ymax || z == zmin || z == zmax)
		                {
		                    if(!isViableNode(origX+x, origY+y, origZ+z))
		                    {
		                        isHollowPrism = false;
		                        break;
		                    }
		                    else if(isFrame(new Object3D(origX+x, origY+y, origZ+z), origX+xmin, origX+xmax, origY+ymin, origY+ymax, origZ+zmin, origZ+zmax) && !isValidFrame(origX+x, origY+y, origZ+z))
		                    {
		                    	isHollowPrism = false;
		                        break;
		                    }
		                    else {
		                        locations.add(new Object3D(origX+x, origY+y, origZ+z));
		                    }
		                }
		                else {
		                    if(!isAir(origX+x, origY+y, origZ+z))
		                    {
		                        isHollowPrism = false;
		                        break;
		                    }
		                    
		                    volume++;
		                }
		            }
		            if(!isHollowPrism)
		            {
		                break;
		            }
		        }
		        if(!isHollowPrism)
		        {
		        	break;
		        }
		    }
		}
		
		if(isHollowPrism && isCorner && volume > 0 && volume <= 5832 && locations.size() >= 9)
		{
			SynchronizedTankData structure = new SynchronizedTankData();
			structure.locations = locations;
			structure.volLength = Math.abs(xmax-xmin)+1;
			structure.volHeight = Math.abs(ymax-ymin)+1;
			structure.volWidth = Math.abs(zmax-zmin)+1;
			structure.volume = volume;
			structure.renderLocation = new Object3D(origX+1, origY+1, origZ+1);
			
			for(Object3D obj : structure.locations)
			{
				if(obj.getTileEntity(pointer.worldObj) instanceof TileEntityDynamicValve)
				{
					ValveData data = new ValveData();
					data.location = obj;
					data.side = getSide(obj, origX+xmin, origX+xmax, origY+ymin, origY+ymax, origZ+zmin, origZ+zmax);
							
					structure.valves.add(data);
				}
			}
			
			if(!structuresFound.contains(structure))
			{
				if(structure.locations.contains(Object3D.get(pointer)) && isCorrectCorner(new Object3D(origX, origY, origZ), origX+xmin, origY+ymin, origZ+zmin))
				{
					if(!entitiesInside(structure))
					{
						structuresFound.add(structure);
					}
				}
				else {
					pointerNotPartOf = true;
				}
			}
		}
		
		iteratedNodes.add((TileEntityDynamicTank)tile);
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = Object3D.get(tile).getFromSide(side).getTileEntity(tile.worldObj);
			
			if(tileEntity instanceof TileEntityDynamicTank)
			{
				if(!iteratedNodes.contains(tileEntity))
				{
					loopThrough(tileEntity);
				}
			}
		}
	}
	
	public ForgeDirection getSide(Object3D obj, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
		if(obj.xCoord == xmin)
		{
			return ForgeDirection.WEST;
		}
		else if(obj.xCoord == xmax)
		{
			return ForgeDirection.EAST;
		}
		else if(obj.yCoord == ymin)
		{
			return ForgeDirection.DOWN;
		}
		else if(obj.yCoord == ymax)
		{
			return ForgeDirection.UP;
		}
		else if(obj.zCoord == zmin)
		{
			return ForgeDirection.NORTH;
		}
		else if(obj.zCoord == zmax)
		{
			return ForgeDirection.SOUTH;
		}
		
		return ForgeDirection.UNKNOWN;
	}
	
	/**
	 * Checks whether or not there are entities inside this dynamic tank.
	 */
	public boolean entitiesInside(SynchronizedTankData structure)
	{
		int x = structure.renderLocation.xCoord;
		int y = structure.renderLocation.yCoord;
		int z = structure.renderLocation.zCoord;
		
		AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(x, y, z, x+structure.volLength, y+structure.volHeight, z+structure.volWidth);
		
		for(Object obj : pointer.worldObj.getEntitiesWithinAABB(Entity.class, boundingBox))
		{
			if(obj instanceof Entity)
			{
				Entity entity = (Entity)obj;
				
				if(entity instanceof EntityPlayer)
				{
					return true;
				}
				else {
					pointer.worldObj.removeEntity(entity);
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Whether or not the block at the specified location is an air block.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	private boolean isAir(int x, int y, int z)
	{
		return pointer.worldObj.getBlockId(x, y, z) == 0;
	}
	
	/**
	 * Whether or not the block at the specified location is a viable node for a dynamic tank.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	private boolean isViableNode(int x, int y, int z)
	{
		TileEntity tileEntity = pointer.worldObj.getBlockTileEntity(x, y, z);
		
		if(tileEntity != null)
		{
			if(tileEntity instanceof TileEntityDynamicTank)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * If the block at the specified location is on the minimum of all angles of this dynamic tank, and the one to use for the
	 * actual calculation.
	 * @param obj - location to check
	 * @param xmin - minimum x value
	 * @param ymin - minimum y value
	 * @param zmin - minimum z value
	 * @return
	 */
	private boolean isCorrectCorner(Object3D obj, int xmin, int ymin, int zmin)
	{
		if(obj.xCoord == xmin && obj.yCoord == ymin && obj.zCoord == zmin)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Whether or not the block at the specified location is considered a frame on the dynamic tank.
	 * @param obj - location to check
	 * @param xmin - minimum x value
	 * @param xmax - maximum x value
	 * @param ymin - minimum y value
	 * @param ymax - maximum y value
	 * @param zmin - minimum z value
	 * @param zmax - maximum z value
	 * @return
	 */
	private boolean isFrame(Object3D obj, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
		if(obj.xCoord == xmin && obj.yCoord == ymin)
			return true;
		if(obj.xCoord == xmax && obj.yCoord == ymin)
			return true;
		if(obj.xCoord == xmin && obj.yCoord == ymax)
			return true;
		if(obj.xCoord == xmax && obj.yCoord == ymax)
			return true;
		
		if(obj.xCoord == xmin && obj.zCoord == zmin)
			return true;
		if(obj.xCoord == xmax && obj.zCoord == zmin)
			return true;
		if(obj.xCoord == xmin && obj.zCoord == zmax)
			return true;
		if(obj.xCoord == xmax && obj.zCoord == zmax)
			return true;
		
		if(obj.yCoord == ymin && obj.zCoord == zmin)
			return true;
		if(obj.yCoord == ymax && obj.zCoord == zmin)
			return true;
		if(obj.yCoord == ymin && obj.zCoord == zmax)
			return true;
		if(obj.yCoord == ymax && obj.zCoord == zmax)
			return true;
		
		return false;
	}
	
	/**
	 * Whether or not the block at the specified location serves as a frame for a dynamic tank.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return
	 */
	private boolean isValidFrame(int x, int y, int z)
	{
		return pointer.worldObj.getBlockId(x, y, z) == Mekanism.basicBlockID && pointer.worldObj.getBlockMetadata(x, y, z) == 9;
	}
	
	/**
	 * Runs the protocol and updates all tanks that make a part of the dynamic tank.
	 */
	public void updateTanks()
	{
		loopThrough(pointer);
		
		if(structuresFound.size() == 1)
		{
			SynchronizedTankData structureFound = structuresFound.get(0);
			
			int idFound = -1;
			
			for(Object3D obj : structureFound.locations)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(pointer.worldObj);
				
				if(tileEntity.inventoryID != -1)
				{
					idFound = tileEntity.inventoryID;
					break;
				}
			}
			
			DynamicTankCache cache = new DynamicTankCache();
			
			if(idFound != -1)
			{
				if(Mekanism.dynamicInventories.get(idFound) != null)
				{
					cache = MekanismUtils.pullInventory(pointer.worldObj, idFound);
				}
			}
			else {
				idFound = MekanismUtils.getUniqueInventoryID();
			}
			
			structureFound.liquidStored = cache.liquid;
			structureFound.inventory = cache.inventory;
			
			for(Object3D obj : structureFound.locations)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(pointer.worldObj);
				
				tileEntity.inventoryID = idFound;
				tileEntity.structure = structureFound;
				tileEntity.cachedLiquid = cache.liquid;
				tileEntity.inventory = cache.inventory;
			}
		}
		else if(!pointerNotPartOf)
		{
			for(TileEntity tileEntity : iteratedNodes)
			{
				((TileEntityDynamicTank)tileEntity).structure = null;
			}
		}
	}
}
