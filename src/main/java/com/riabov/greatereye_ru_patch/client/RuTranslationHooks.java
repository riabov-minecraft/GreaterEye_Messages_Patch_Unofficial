package com.riabov.greatereye_ru_patch.client;

import com.riabov.greatereye_ru_patch.GreaterEyeRuPatch;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Mod.EventBusSubscriber(modid = GreaterEyeRuPatch.MOD_ID, value = Dist.CLIENT)
public final class RuTranslationHooks {

    private static final Map<String, String> EN_LABEL_TO_RU = new HashMap<>();
    private static final Map<String, String> STRUCTURE_ID_TO_RU = new HashMap<>();

    static {
        putLabel("Villages", "Деревни");
        putLabel("Mineshafts", "Заброшенные шахты");
        putLabel("Ships", "Кораблекрушения");
        putLabel("Ruins", "Руины океана");
        putLabel("Twilight", "Сумеречный лес");
        putLabel("Igloos", "Иглу");
        putLabel("Huts", "Хижины ведьм");
        putLabel("Graveyards", "Кладбища");
        putLabel("Strongholds", "Крепости");
        putLabel("Buildings", "Постройки");
        putLabel("Fortresses", "Адские крепости");
        putLabel("Outposts", "Форпосты разбойников");
        putLabel("Temples", "Храмы");
        putLabel("Buried Treasures", "Зарытые сокровища");
        putLabel("Pyramids", "Пирамиды");
        putLabel("Fossils", "Окаменелости");
        putLabel("Nether Buildings", "Постройки Нижнего мира");
        putLabel("Bastions", "Бастионы");
        putLabel("Cities", "Города Края");
        putLabel("Monuments", "Океанические монументы");
        putLabel("Mansions", "Лесные особняки");
        putLabel("Dungeons", "Подземелья");
        putLabel("End Buildings", "Постройки Края");

        putId("minecraft:village", "Деревня");
        putId("minecraft:mineshaft", "Заброшенная шахта");
        putId("minecraft:shipwreck", "Кораблекрушение");
        putId("minecraft:ruined_portal", "Разрушенный портал");
        putId("minecraft:ocean_ruin_warm", "Тёплые руины океана");
        putId("minecraft:ocean_ruin_cold", "Холодные руины океана");
        putId("minecraft:igloo", "Иглу");
        putId("minecraft:swamp_hut", "Хижина ведьмы");
        putId("minecraft:pillager_outpost", "Форпост разбойников");
        putId("minecraft:stronghold", "Крепость");
        putId("minecraft:desert_pyramid", "Пустынный храм");
        putId("minecraft:jungle_pyramid", "Храм в джунглях");
        putId("minecraft:buried_treasure", "Зарытое сокровище");
        putId("minecraft:nether_fossil", "Окаменелость Нижнего мира");
        putId("minecraft:fortress", "Адская крепость");
        putId("minecraft:bastion_remnant", "Бастион");
        putId("minecraft:monument", "Океанический монумент");
        putId("minecraft:mansion", "Лесной особняк");
        putId("minecraft:end_city", "Город Края");
        putId("minecraft:ancient_city", "Древний город");
    }

    private static void putLabel(String en, String ru) {
        EN_LABEL_TO_RU.put(en, ru);
    }

    private static void putId(String id, String ru) {
        STRUCTURE_ID_TO_RU.put(id, ru);
    }

    private static boolean isRussianSelected() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getLanguageManager() == null || mc.getLanguageManager().getSelected() == null) return false;
        return "ru_ru".equals(mc.getLanguageManager().getSelected().getCode());
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!isRussianSelected()) return;

        var key = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (key == null) return;
        String itemId = key.toString();
        if (!itemId.startsWith("greater_eye:")) return;

        var list = event.getToolTip();
        for (int i = 0; i < list.size(); i++) {
            list.set(i, rewriteComponent(list.get(i)));
        }
    }

    @SubscribeEvent
    public static void onChatSystem(ClientChatReceivedEvent.System event) {
        if (!isRussianSelected()) return;

        Component msg = event.getMessage();
        if (!shouldRewriteChatMessage(msg)) return;

        event.setMessage(rewriteComponent(msg));
    }


    private static boolean shouldRewriteChatMessage(Component msg) {
        if (msg == null) return false;

        if (msg.getContents() instanceof TranslatableContents tc) {
            String key = tc.getKey();
            if (key != null) {
                if (key.startsWith("item.greater_eye.") || key.equals("itemGroup.greater_eye")) {
                    return true;
                }
            }
            return false;
        }

        if (msg.getContents() instanceof LiteralContents lc) {
            String text = lc.text();
            if (text == null || text.isEmpty()) return false;

            return isKnownTokenOrId(text);
        }

        return false;
    }

    private static boolean isKnownTokenOrId(String s) {
        if (s == null || s.isEmpty()) return false;

        if (EN_LABEL_TO_RU.containsKey(s)) return true;

        String id = s.toLowerCase(Locale.ROOT);
        return STRUCTURE_ID_TO_RU.containsKey(id);
    }

    private static Component rewriteComponent(Component in) {
        if (in == null) return Component.empty();

        if (in.getContents() instanceof TranslatableContents tc) {
            Object[] args = tc.getArgs();
            Object[] newArgs = new Object[args.length];
            boolean changed = false;

            for (int i = 0; i < args.length; i++) {
                Object a = args[i];
                Object b = a;

                if (a instanceof String s) {
                    b = translateToken(s);
                } else if (a instanceof Component c) {
                    b = rewriteComponent(c);
                }

                newArgs[i] = b;
                if (b != a && (b == null || !b.equals(a))) changed = true;
            }

            if (changed) {
                MutableComponent rebuilt = Component.translatable(tc.getKey(), newArgs);
                rebuilt.setStyle(in.getStyle());
                for (Component sib : in.getSiblings()) rebuilt.append(rewriteComponent(sib));
                return rebuilt;
            }
        }

        if (in.getContents() instanceof LiteralContents lc) {
            String text = lc.text();
            String newText = translateToken(text);
            if (!newText.equals(text)) {
                MutableComponent rebuilt = Component.literal(newText);
                rebuilt.setStyle(in.getStyle());
                for (Component sib : in.getSiblings()) rebuilt.append(rewriteComponent(sib));
                return rebuilt;
            }
        }

        return in;
    }

    private static String translateToken(String s) {
        if (s == null || s.isEmpty()) return s;

        String direct = EN_LABEL_TO_RU.get(s);
        if (direct != null) return direct;

        String id = s.toLowerCase(Locale.ROOT);
        String byId = STRUCTURE_ID_TO_RU.get(id);
        if (byId != null) return byId;

        return s;
    }
}