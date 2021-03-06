package mekanism.common.tile;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntitySalinationValve extends TileEntitySalinationTank implements IFluidHandler
{
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return master == null ? 0 : master.waterTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(master != null && resource.getFluid() == FluidRegistry.getFluid("brine"))
		{
			return master.brineTank.drain(resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(master != null)
		{
			return master.brineTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return master != null && fluid == FluidRegistry.getFluid("water");
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return master != null && fluid == FluidRegistry.getFluid("brine");
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(master == null)
		{
			return new FluidTankInfo[0];
		}
		
		return new FluidTankInfo[] {new FluidTankInfo(master.waterTank), new FluidTankInfo(master.brineTank)};
	}
}
