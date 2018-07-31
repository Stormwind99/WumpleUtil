package com.wumple.util;

import java.util.ArrayList;

import com.wumple.util.config.MatchingConfigBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class TestDebug
{
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onItemTooltip(ItemTooltipEvent event)
    {
        if (ModConfig.zdebugging.debug == true)
        {
            ItemStack itemStack = event.getItemStack();
            ArrayList<String> nameKeys = MatchingConfigBase.getNameKeys(itemStack);

            if (nameKeys != null)
            {
                for (String nameKey : nameKeys)
                {
                    event.getToolTip().add(new TextComponentTranslation("misc.wumpleutil.debug.namekey", nameKey).getUnformattedText());
                }
            }
        }
    }

    /*
     * Draw debug screen extras
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    @SideOnly(Side.CLIENT)
    static public void onDrawOverlay(final RenderGameOverlayEvent.Text e)
    {
        if (ModConfig.zdebugging.debug == true)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.gameSettings.showDebugInfo == true)
            {
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY && mc.objectMouseOver.entityHit != null)
                {
                    Entity entity = mc.objectMouseOver.entityHit;

                    ArrayList<String> nameKeys = MatchingConfigBase.getNameKeys(entity);

                    if (nameKeys != null)
                    {
                        for (String nameKey : nameKeys)
                        {
                            e.getRight().add(I18n.format("misc.wumpleutil.debug.namekey", nameKey));
                        }
                    }
                }
            }
        }
    }

}