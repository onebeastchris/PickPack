package net.onebeastchris.geyser.extension.pickpack.Util;

import net.onebeastchris.geyser.extension.pickpack.PickPack;
import org.geysermc.cumulus.component.ToggleComponent;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.pack.ResourcePack;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.text.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.onebeastchris.geyser.extension.pickpack.PickPack.config;
import static net.onebeastchris.geyser.extension.pickpack.PickPack.loader;

public class Form {
    private final String lang;
    private final GeyserConnection connection;
    public enum Filter {
        APPLIED,
        NOT_APPLIED,
        ALL
    }
    public Form(GeyserConnection connection) {
        this.connection = connection;
        this.lang = connection.locale();
    }

    public void send(String... args) {
        String xuid = connection.xuid();

        if (args != null && args.length > 0) {
            switch (args[0]) {
                case "filter" -> {
                    filterForm();
                    return;
                }
                case "clear" -> {
                    CompletableFuture<Void> future = PickPack.storage.setPacks(xuid, new ArrayList<>(loader.OPT_OUT.values()));
                    future.thenRun(() -> handle(config.useTransferPacket()));
                    return;
                }
            }
        }
        ModalForm.Builder form = ModalForm.builder()
                .title(LanguageManager.getLocaleString(lang, "main.menu.title"))
                .content(getPacks(xuid))
                .button1(LanguageManager.getLocaleString(lang, "main.menu.filter"))
                .button2(LanguageManager.getLocaleString(lang, "main.menu.select"));

        form.validResultHandler((modalform, response) -> {
            switch (response.clickedButtonId()) {
                case 0 -> filterForm();
                case 1 -> packsForm(config.useTransferPacket(), config.showPackDescription(), Filter.ALL);
            }
        });
        connection.sendForm(form.build());
    }

    public void filterForm() {
        CustomForm.Builder form = CustomForm.builder()
                .title(LanguageManager.getLocaleString(lang, "filter.form.title"));

        if (PickPack.storage.getPacks(connection.xuid()).isEmpty()) {
            form.dropdown(LanguageManager.getLocaleString(lang, "filter.button.name"), LanguageManager.getLocaleString(lang, "filter.all.packs"), LanguageManager.getLocaleString(lang, "filter.all.packs"));
        } else {
            form.dropdown(LanguageManager.getLocaleString(lang, "filter.button.name"),
                    LanguageManager.getLocaleString(lang, "filter.all.packs"),
                    LanguageManager.getLocaleString(lang, "filter.not_applied.packs"),
                    LanguageManager.getLocaleString(lang, "filter.applied.packs"));
        }
        form.toggle(LanguageManager.getLocaleString(lang, "filter.description.toggle"), config.showPackDescription());
        form.label(LanguageManager.getLocaleString(lang, "filter.transfer.warning"));
        form.toggle(LanguageManager.getLocaleString(lang, "filter.transfer.toggle"), config.useTransferPacket());

        form.validResultHandler((customform, response) -> {
            int filterResult  = response.asDropdown(0);
            boolean description = response.asToggle(1);
            boolean transfer = response.asToggle(3);

            switch (filterResult) {
                case 1 -> packsForm(transfer, description, Filter.NOT_APPLIED);
                case 2 -> packsForm(transfer, description, Filter.APPLIED);
                default -> packsForm(transfer, description, Filter.ALL);
            }
        });
        connection.sendForm(form.build());
    }

    public void packsForm(boolean transferPacket, boolean description, Filter filter) {
        String xuid = connection.xuid();
        Map<String, String> tempMap = new HashMap<>();
        CustomForm.Builder form = CustomForm.builder()
                .title(LanguageManager.getLocaleString(lang, "pack.form.title"));

        form.label(LanguageManager.getLocaleString(lang, "pack.form.label").replace("%filter%", getFilterType(filter)));

        for (Map.Entry<String, String[]> entry : loader.PACKS_INFO.entrySet()) {
            String name = entry.getValue()[0];
            boolean currentlyApplied = PickPack.storage.hasSpecificPack(xuid, entry.getKey());
            boolean isVisible = filter.equals(Filter.ALL) || (filter.equals(Filter.APPLIED) && currentlyApplied) || (filter.equals(Filter.NOT_APPLIED) && !currentlyApplied);
            if (isVisible) {
                form.toggle(name, currentlyApplied);
                if (description) form.label(ChatColor.ITALIC + entry.getValue()[1] + ChatColor.RESET);
                tempMap.put(entry.getValue()[0], entry.getKey()); //makes it easier to get the uuid from the name later on
            }
        }

        form.closedOrInvalidResultHandler((customform, response) -> {
            filterForm(); //we cant add back buttons. But we can just send the filter form again.
        });

        form.validResultHandler((customform, response) -> {
            List<ResourcePack> playerPacks = new ArrayList<>();
            customform.content().forEach((component) -> {
                if (component instanceof ToggleComponent) {
                    if (Boolean.TRUE.equals(response.next())) {
                        String uuid = tempMap.get(component.text());
                        playerPacks.add(loader.getPack(uuid));
                    }
                }
            });

            if (filter.equals(Filter.NOT_APPLIED)) {
                //keep the old packs if we are filtering for not applied packs
                playerPacks.addAll(PickPack.storage.getPacks(xuid));
            }

            CompletableFuture<Void> future = PickPack.storage.setPacks(xuid, playerPacks);
            future.thenRun(() -> handle(transferPacket));

            tempMap.clear();
        });
        connection.sendForm(form);
    }

    private String getPacks(String xuid) {
        StringBuilder packs = new StringBuilder();
        for (ResourcePack pack : PickPack.storage.getPacks(xuid)) {
            String name = pack.manifest().header().name();
            packs.append(" - ").append(name).append("\n");
        }
        if (packs.length() == 0) packs.append(LanguageManager.getLocaleString(lang, "no_packs.warning"));
        return packs.toString();
    }

    private void handle(boolean transferPacket) {
        GeyserSession session = (GeyserSession) connection;
        if (transferPacket) {
            session.transfer(config.address(), config.port());
        } else {
            session.disconnect(LanguageManager.getLocaleString(lang, "disconnect.message"));
        }
    }

    private String getFilterType(Filter filter) {
        return switch (filter) {
            case ALL -> LanguageManager.getLocaleString(lang, "filter.all.packs");
            case APPLIED -> LanguageManager.getLocaleString(lang, "filter.applied.packs");
            case NOT_APPLIED -> LanguageManager.getLocaleString(lang, "filter.not_applied.packs");
        };
    }
}
