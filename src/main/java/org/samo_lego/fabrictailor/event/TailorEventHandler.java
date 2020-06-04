package org.samo_lego.fabrictailor.event;

import net.minecraft.server.network.ServerPlayerEntity;

public class TailorEventHandler {

    // Pretty self explanatory
    public static void onPlayerJoin(ServerPlayerEntity player) {
        // todo get skin data from player nbt with ccapi
        // random skin for testing
        String value = "ewogICJ0aW1lc3RhbXAiIDogMTU5MTI1NjU1MDA2NiwKICAicHJvZmlsZUlkIiA6ICI3NzI3ZDM1NjY5Zjk0MTUxODAyM2Q2MmM2ODE3NTkxOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJsaWJyYXJ5ZnJlYWsiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDk5NmEzMzRhM2I3OWIzY2I5ZjhiYTdhMTM1MGZlMTcxYmVlN2U3MWJjZDRiNjc5MDBmYmVmNjk2YmU0ODlmYyIKICAgIH0KICB9Cn0=";
        String signature = "G6vgcsgixDjU3h4nc23uaUeRDTMranSDZtOV/KpmETnpcFAQABc+dy8aKh0VvJSzAZxsk8r2/CA5jGulJZyC6Q1Wtec5srFdkcF3jDc3rTMy2pXVN3HSucojGICAmonCNsMX4HhcLWAWZ/14uSzSjo2srDvI5KIfdSQgvh+L4PiLlW6gwfzAzqftrPJXWJOuZJqFi11nVal4tbEDkpAAVMgJ8GonbFXMq1QqQ8gAv9OnI3Nm2sNdae1RD5yjtH+DQ+50f9UHavRldYCDdYa1q3wZb9FrMpCIZ/32LbFuZQH5HNPOhTRwJbViARj8L3js+pOC9VlNN9ekZP6W1RfbjPlaesNunAk4HbYwWI0uuIdQSpmZnsd0Gn6iIGnXGp/tufek7JUF3CyY+9AFCw5ps2poUxyUx8JLHdx4QzYk+cg6fVCibwSH1c+bTaMysbosYim44pkhgM/kEb1cN+14Mwty2CeOy3P5JMSukGIhdUX9H34Gl2hUMQK1TCtpsqq6FVlB0izG97AFK14KHFHMLJjq6CRCcWAuG7ORKXoevTNYMUa5YNkmAFDZUGjLm3CEhuLxjPLQNdevN+WKYr0QM4ES7ipaYz+aJdx+EgZkvuYqO8ksK2X1PT5rz18SBUqYvweoMf1Jzo3NJvIEBPlb7Ym2tct6yDjpzIzdrmk085w=";

        /*skinClient.generateUser(player.getUuid(), skin -> setPlayerSkin(
                player, skin.data.texture.value, skin.data.texture.signature
        ));*/
        // Puts the saved skindata to player's profile
        //setPlayerSkin(player, value, signature);
    }
}
