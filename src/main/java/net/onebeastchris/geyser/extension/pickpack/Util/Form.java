package net.onebeastchris.geyser.extension.pickpack.Util;


import net.onebeastchris.geyser.extension.pickpack.packing;
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
import java.util.concurrent.CompletableFuture;

import static net.onebeastchris.geyser.extension.pickpack.packing.loader;
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
        String xuid = connection.xuid();

        if (args != null && args.length > 0) {
            switch (args[0]) {
                case "filter" -> {
                    filterForm(connection);
                    return;
                }
                case "remove", "clear" -> {
                    CompletableFuture<Void> future = packing.storage.setPacks(xuid, new HashMap<>());
                    future.thenRun(() -> {
                        handle(connection, true);
                    });
                    return;
                }
            }
        }
        ModalForm.Builder form = ModalForm.builder()
                .title("Your current packs:")
                .content(getPacks(xuid))
                .button1("Change packs")
                .button2("Back");

        form.validResultHandler((modalform, response) -> {
            switch (response.clickedButtonId()) {
                case 0 -> filterForm(connection);
                case 1 -> {
                    //do nothing... they clicked back
                }
            }
        });
        connection.sendForm(form.build());
    }

    public void filterForm(GeyserConnection connection) {
        CustomForm.Builder form = CustomForm.builder()
                .title("Which packs would you like to see?");

        if (packing.storage.getPacks(connection.xuid()).isEmpty()) {
            form.dropdown("Filter", "all packs");
        } else {
            form.dropdown("Filter", "all packs", "not applied packs", "applied packs");
        }
        form.toggle("Show pack descriptions", true);
        form.label("If the transfer packet never works for you, you can disable it here.");
        form.toggle("Use transfer packet", true);

        form.validResultHandler((customform, response) -> {
            int filterResult  = response.asDropdown(0);
            boolean description = response.asToggle(1);
            boolean transfer = response.asToggle(3);

            switch (filterResult) {
                case 1 -> packsForm(connection, transfer, description, Filter.NOT_APPLIED);
                case 2 -> packsForm(connection, transfer, description, Filter.APPLIED);
                default -> packsForm(connection, transfer, description, Filter.ALL);
            }
        });
        connection.sendForm(form.build());
    }

    public void packsForm(GeyserConnection connection, boolean transferPacket, boolean description, Filter filter) {
        String xuid = connection.xuid();
        Map<String, String> tempMap = new HashMap<>();
        CustomForm.Builder form = CustomForm.builder()
                .title("Choose your packs");

        form.label(ChatColor.BOLD + ChatColor.DARK_GREEN + "showing " +
                ChatColor.RESET + ChatColor.BOLD + ChatColor.GOLD + filter.toString().toLowerCase().replace("_", " ") +
                ChatColor.RESET + ChatColor.BOLD + ChatColor.DARK_GREEN + " packs");

        for (Map.Entry<String, String[]> entry : loader.PACKS_INFO.entrySet()) {
            String name = entry.getValue()[0];
            boolean currentlyApplied = packing.storage.hasSpecificPack(xuid, entry.getKey());
            boolean isVisible = filter.equals(Filter.ALL) || (filter.equals(Filter.APPLIED) && currentlyApplied) || (filter.equals(Filter.NOT_APPLIED) && !currentlyApplied);
            if (isVisible) {
                form.toggle(name, currentlyApplied);
                if (description) form.label(ChatColor.ITALIC + entry.getValue()[1] + ChatColor.RESET);
                tempMap.put(entry.getValue()[0], entry.getKey()); //makes it easier to get the uuid from the name later on
            }
        }

        form.closedOrInvalidResultHandler((customform, response) -> {
            filterForm(connection); //we cant add back buttons. But we can just send the filter form again.
        });

        form.validResultHandler((customform, response) -> {
            Map<String, ResourcePack> playerPacks = new HashMap<>();
            customform.content().forEach((component) -> {
                if (component instanceof ToggleComponent) {
                    if (Boolean.TRUE.equals(response.next())) {
                        String uuid = tempMap.get(component.text());
                        playerPacks.put(uuid, loader.getPack(uuid));
                    }
                }
            });

            if (filter.equals(Filter.NOT_APPLIED)) {
                //keep the old packs if we are filtering for not applied packs
                playerPacks.putAll(packing.storage.getPacks(xuid));
            }

            CompletableFuture<Void> future = packing.storage.setPacks(xuid, playerPacks);
            future.thenRun(() -> {
                handle(connection, transferPacket);
            });

            tempMap.clear();
        });
        connection.sendForm(form);
    }

    private String getPacks(String xuid) {
        StringBuilder packs = new StringBuilder();
        for (Map.Entry<String, ResourcePack> entry : packing.storage.getPacks(xuid).entrySet()) {
            String name = entry.getValue().manifest().header().name();
            packs.append(" - ").append(name).append("\n");
        }
        if (packs.length() == 0) packs.append("You have no packs applied that you could remove!");
        return packs.toString();
    }

    private void handle(GeyserConnection connection, boolean transferPacket) {
        GeyserSession session = (GeyserSession) connection;
        if (transferPacket) {
            int port = 19132;
            String address = "127.0.0.1"; //TODO: dont hardcode
            logger.debug("Transferring " + connection.bedrockUsername() + " to " + address + ":" + port);
            connection.transfer(address, port);
        } else {
            session.disconnect(ChatColor.GOLD + "Join back to apply the changes!");
        }
    }
}