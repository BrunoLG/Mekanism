package mekanism.client.render.tileentity;

import mekanism.client.model.ModelLaserAmplifier;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class RenderLaserTractorBeam extends TileEntitySpecialRenderer
{
	private ModelLaserAmplifier model = new ModelLaserAmplifier();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick, int damage)
	{
		renderAModelAt((TileEntityLaserTractorBeam)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityLaserTractorBeam tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);

		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LaserTractorBeam.png"));

		switch(tileEntity.getFacing())
		{
			case DOWN:
				GL11.glTranslatef(0.0F, -2.0F, 0.0F);
				GL11.glRotatef(180F, 1.0F, 0.0F, 0.0F);
				break;
			case EAST:
				GL11.glTranslatef(0.0F, -1.0F, 0.0F);
				GL11.glTranslatef(1.0F, 0.0F, 0.0F);
				GL11.glRotatef(90, 0.0F, 0.0F, -1.0F);
				break;
			case WEST:
				GL11.glTranslatef(0.0F, -1.0F, 0.0F);
				GL11.glTranslatef(-1.0F, 0.0F, 0.0F);
				GL11.glRotatef(90, 0.0F, 0.0F, 1.0F);
				break;
			case NORTH:
				GL11.glTranslatef(0.0F, -1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, -1.0F);
				GL11.glRotatef(90, -1.0F, 0.0F, 0.0F);
				break;
			case SOUTH:
				GL11.glTranslatef(0.0F, -1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, 1.0F);
				GL11.glRotatef(90, 1.0F, 0.0F, 0.0F);
				break;
		}

		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		MekanismRenderer.blendOn();
		model.render(0.0625F);
		MekanismRenderer.blendOff();
		GL11.glPopMatrix();
	}
}