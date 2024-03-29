package com.stevenlowes.edahorror.events

import com.stevenlowes.edahorror.ModController
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.IOException
import javax.imageio.ImageIO

@Mod.EventBusSubscriber(modid = ModController.MODID)
object CreeperOverlay {
    var show = false

    private var VIGNETTE_TEX_PATH: ResourceLocation? = null

    @SubscribeEvent
    @JvmStatic
    fun onRenderGui(event: RenderGameOverlayEvent.Pre) {
        val mc = Minecraft.getMinecraft()
        val scaled = ScaledResolution(mc)
        if (show) {
            renderVignette(mc, scaled, 1f, 1f, 1f, 1f)
        }
    }

    init {
        try {
            VIGNETTE_TEX_PATH = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(
                    "creeperImage", DynamicTexture(
                    ImageIO.read(
                            CreeperOverlay::class.java.classLoader.getResource(
                                    "creeperface.png")!!)))
        }
        catch (e: IOException) {
            VIGNETTE_TEX_PATH = null
            FMLCommonHandler.instance().exitJava(1, true)
        }

    }

    private fun renderVignette(mc: Minecraft, scaledRes: ScaledResolution, r: Float, g: Float, b: Float, a: Float) {
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.depthMask(false)
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO)
        GlStateManager.color(r, g, b, a)
        GlStateManager.disableAlpha()
        mc.textureManager.bindTexture(VIGNETTE_TEX_PATH!!)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        bufferbuilder.pos(0.0, scaledRes.scaledHeight.toDouble(), -90.0).tex(0.0, 1.0).endVertex()
        bufferbuilder.pos(scaledRes.scaledWidth.toDouble(), scaledRes.scaledHeight.toDouble(), -90.0).tex(
                1.0, 1.0).endVertex()
        bufferbuilder.pos(scaledRes.scaledWidth.toDouble(), 0.0, -90.0).tex(1.0, 0.0).endVertex()
        bufferbuilder.pos(0.0, 0.0, -90.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableAlpha()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }
}
