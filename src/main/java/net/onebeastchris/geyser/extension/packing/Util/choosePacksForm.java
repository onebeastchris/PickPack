package net.onebeastchris.geyser.extension.packing.Util;


import net.onebeastchris.geyser.extension.packing.packing;
import org.geysermc.cumulus.component.ToggleComponent;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.packs.ResourcePack;
import org.geysermc.geyser.session.GeyserSession;

import java.util.HashMap;
import java.util.Map;

public class choosePacksForm {
    private Map<String, String> packs = packing.packs.packNamesMap;

    public void sendForm(GeyserConnection connection, GeyserSession session) {
        CustomForm.Builder form = CustomForm.builder()
                .title("Choose your packs");

        for (Map.Entry<String, String> entry : packs.entrySet()) {
            form.optionalToggle(entry.getKey(),true);
        }

        form.closedOrInvalidResultHandler((customform, response) -> {
            //do nothing
        });

        form.validResultHandler((customform, response) -> {
            Map<String, ResourcePack> playerPacks = new HashMap<>();
            while (response.hasNext()) {
                ToggleComponent toggleComponent = (ToggleComponent) response.next();
                String name = toggleComponent.text();
                if (response.asToggle() && packs.containsKey(name)) {
                    //add pack to player
                    String packUUID = packs.get(name);
                    playerPacks.put(packUUID, packing.packs.packsmap.get(packUUID));
                }
            }
            packing.storage.cache.put(connection.xuid(), playerPacks);

            session.transfer(packing.address, packing.port);
        });
    }
}
