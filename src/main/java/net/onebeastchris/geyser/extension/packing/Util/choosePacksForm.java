package net.onebeastchris.geyser.extension.packing.Util;


import net.onebeastchris.geyser.extension.packing.packing;
import org.geysermc.cumulus.component.ToggleComponent;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;

import java.util.HashMap;
import java.util.Map;

import static net.onebeastchris.geyser.extension.packing.packing.address;
import static net.onebeastchris.geyser.extension.packing.packing.port;

import static net.onebeastchris.geyser.extension.packing.packing.packs;
public class choosePacksForm {

    ExtensionLogger logger;
    public choosePacksForm(ExtensionLogger logger) {
        this.logger = logger;
    }
    public void sendForm(GeyserConnection connection) {

        //get current packs!

        Map<String, String> tempMap = new HashMap<>();

        CustomForm.Builder form = CustomForm.builder()
                .title("Choose your packs");

        for (Map.Entry<String, String[]> entry : packs.PACKS_INFO.entrySet()) {
            form.toggle(entry.getValue()[0], isApplied(connection.xuid(), entry.getKey()));
            tempMap.put(entry.getValue()[0], entry.getKey());
            form.label(entry.getValue()[1]);
        }

        form.closedOrInvalidResultHandler((customform, response) -> {
            //do nothing
        });

        form.validResultHandler((customform, response) -> {
            Map<String, ResourcePack> playerPacks = new HashMap<>();
            customform.content().forEach((component) -> {
                if (component instanceof ToggleComponent) {
                    if (Boolean.TRUE.equals(response.next())) {
                        logger.info("Adding " + component.text() + " to player packs");
                        String uuid = tempMap.get(component.text());

                        if (packs.PACKS_INFO.get(uuid)[3].equals("true")) {
                            playerPacks.put(uuid, packs.OPT_OUT.get(uuid));
                        } else {
                            playerPacks.put(uuid, packs.OPT_IN.get(uuid));
                        }
                    }
                }
            });
            packing.storage.setPacks(connection.xuid(), playerPacks);
            connection.transfer(address, port);
        });

        connection.sendForm(form.build());
    }

    public boolean isApplied(String xuid, String uuid) {
        return packing.storage.getPacks(xuid).get(uuid) != null;
    }
}
