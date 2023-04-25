package net.onebeastchris.geyser.extension.packing.Util;


import net.onebeastchris.geyser.extension.packing.packing;
import org.geysermc.cumulus.component.ToggleComponent;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;

import java.util.HashMap;
import java.util.Map;

public class choosePacksForm {

    ExtensionLogger logger;
    public choosePacksForm(ExtensionLogger logger) {
        this.logger = logger;
    }
    private Map<String, String> packs = packing.packs.packNamesMap;

    public void sendForm(GeyserConnection connection) {
        CustomForm.Builder form = CustomForm.builder()
                .title("Choose your packs");

        for (Map.Entry<String, String> entry : packs.entrySet()) {
            form.optionalToggle(entry.getKey(), true);
            logger.info("Added " + entry.getKey() + " to form");
        }

        form.closedOrInvalidResultHandler((customform, response) -> {
            //do nothing
        });

        form.validResultHandler((customform, response) -> {
            Map<String, ResourcePack> playerPacks = new HashMap<>();
            packing.storage.cache.remove(connection.xuid());
            customform.content().forEach((component) -> {
                if (component instanceof ToggleComponent) {
                    if (Boolean.TRUE.equals(response.next())) {
                        logger.info("Adding " + component.text() + " to player packs");
                        String packUUID = packing.packs.packNamesMap.get(component.text());
                        playerPacks.put(packUUID, packing.packs.packsmap.get(packUUID));
                    }
                }
            });
            packing.storage.setPacks(connection.xuid(), playerPacks);
            connection.transfer(packing.address, packing.port);
        });
        connection.sendForm(form.build());
    }
}
