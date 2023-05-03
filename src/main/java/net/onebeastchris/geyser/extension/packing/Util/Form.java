package net.onebeastchris.geyser.extension.packing.Util;


import net.onebeastchris.geyser.extension.packing.packing;
import org.geysermc.cumulus.component.ToggleComponent;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import org.geysermc.geyser.api.packs.ResourcePack;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.text.ChatColor;

import java.util.HashMap;
import java.util.Map;

import static net.onebeastchris.geyser.extension.packing.packing.packs;
public class Form {
    public enum Filter {
        APPLIED,
        NOT_APPLIED,
        ALL
    }
    ExtensionLogger logger;
    public Form(ExtensionLogger logger) {
        this.logger = logger;
    }
    public void send(GeyserConnection connection, String... args) {

        boolean transferPacket = true;
        //transfer packets sometimes refuse to work... lets give the user the option to disable them
        for (String arg : args) {
            if (arg.equals("no-transfer") || arg.equals("nt") || arg.equals("n") || arg.equals("kick") || arg.equals("k")) {
                transferPacket = false;
                break;
            }
        }

        ModalForm.Builder form = ModalForm.builder()
                .title("Your applied packs:")
                .content(getPacks(connection.xuid()))
                .button1("Change packs")
                .button2("Back");

        boolean finalTransferPacket = transferPacket;
        form.validResultHandler((modalform, response) -> {
            switch (response.clickedButtonId()) {
                case 0 -> filterForm(connection, finalTransferPacket);
                case 1 -> {
                    //do nothing
                }
            }
        });

        connection.sendForm(form.build());
    }

    public void filterForm(GeyserConnection connection, boolean transferPacket) {
        CustomForm.Builder form = CustomForm.builder()
                .title("Which packs would you like to see?")
                .dropdown("Filter", "all packs", "applied packs", "not applied packs")
                .toggle("Show pack descriptions", true)
                .closedOrInvalidResultHandler((customform, response) -> {
                    //do nothing
                });

        form.validResultHandler((customform, response) -> {
            int filterResult  = response.asDropdown();
            boolean description = response.asToggle();

            switch (filterResult) {
                case 1 -> packsForm(connection, transferPacket, description, Filter.APPLIED);
                case 2 -> packsForm(connection, transferPacket, description, Filter.NOT_APPLIED);
                default -> packsForm(connection, transferPacket, description, Filter.ALL);
            }
        });
        connection.sendForm(form.build());
    }

    public void packsForm(GeyserConnection connection, boolean transferPacket, boolean description, Filter filter) {
        Map<String, String> tempMap = new HashMap<>();
        CustomForm.Builder form = CustomForm.builder()
                .title("Choose your packs");

        form.label("showing " + filter.toString().toLowerCase() + " packs");

        for (Map.Entry<String, String[]> entry : packs.PACKS_INFO.entrySet()) {
            String name = entry.getValue()[0];
            boolean currentlyApplied = packing.storage.hasSpecificPack(connection.xuid(), entry.getKey());
            boolean show = filter.equals(Filter.ALL) || (filter.equals(Filter.APPLIED) && currentlyApplied) || (filter.equals(Filter.NOT_APPLIED) && !currentlyApplied);

            form.optionalToggle(name, currentlyApplied, show);
            if (show) tempMap.put(entry.getValue()[0], entry.getKey());
            if (description) form.label(entry.getValue()[1]);
        }

        form.closedOrInvalidResultHandler((customform, response) -> {
            //do nothing
        });

        form.validResultHandler((customform, response) -> {
            Map<String, ResourcePack> playerPacks = new HashMap<>();
            customform.content().forEach((component) -> {
                if (component instanceof ToggleComponent) {
                    if (Boolean.TRUE.equals(response.next())) {
                        String uuid = tempMap.get(component.text());
                        playerPacks.put(uuid, packs.getPack(uuid));
                    }
                }
            });

            packing.storage.setPacks(connection.xuid(), playerPacks);

            GeyserSession session = (GeyserSession) connection;

            if (transferPacket) {
                int port = 19132;
                String address = "127.0.0.1";
                logger.info("Transferring " + connection.bedrockUsername() + " to " + address + ":" + port);
                connection.transfer(address, port);
            } else {
                session.disconnect(ChatColor.GOLD + "Join back to apply packs!");
            }
        });
        connection.sendForm(form);
    }

    private String getPacks(String xuid) {
        StringBuilder packs = new StringBuilder();
        for (Map.Entry<String, ResourcePack> entry : packing.storage.getPacks(xuid).entrySet()) {
            String name = entry.getValue().getManifest().getHeader().getName();
            packs.append(" - ").append(name).append("\n");
        }
        if (packs.length() == 0) packs.append("You have no packs applied that you could remove!");
        return packs.toString();
    }
}
